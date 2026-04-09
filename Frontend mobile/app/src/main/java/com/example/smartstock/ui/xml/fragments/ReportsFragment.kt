package com.example.smartstock.ui.xml.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.smartstock.BuildConfig
import com.example.smartstock.R
import com.example.smartstock.core.report.ReportImageCompressor
import com.example.smartstock.core.report.ReportImageCompressor.CompressionResult
import com.example.smartstock.domain.model.Report
import com.example.smartstock.ui.screens.reports.ReportSubmitResult
import com.example.smartstock.ui.screens.reports.ReportsViewModel
import com.example.smartstock.ui.xml.adapters.ReportEvidenceDraftAdapter
import com.example.smartstock.ui.xml.adapters.ReportEvidenceDraftItem
import com.example.smartstock.ui.xml.adapters.ReportEvidenceViewerPagerAdapter
import com.example.smartstock.ui.xml.adapters.ReportsAdapter
import com.example.smartstock.ui.xml.dropdown.DropdownOption
import com.example.smartstock.ui.xml.dropdown.bindSearchableDropdown
import com.example.smartstock.ui.xml.normalizeForSearch
import com.example.smartstock.ui.xml.toast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ReportsFragment : Fragment(R.layout.fragment_reports) {
    private val viewModel: ReportsViewModel by viewModels()

    private lateinit var adapter: ReportsAdapter

    private var pendingCameraFile: File? = null
    private var activeEvidenceItems: MutableList<ReportEvidenceDraftItem>? = null
    private var activeEvidenceAdapter: ReportEvidenceDraftAdapter? = null
    private var activeEvidenceCounter: TextView? = null
    private var activeCaptureButton: Button? = null

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraCapture()
            } else {
                requireContext().toast("Se necesita permiso de camara para capturar evidencias.")
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
            val capturedFile = pendingCameraFile
            pendingCameraFile = null

            if (!captured || capturedFile == null) {
                capturedFile?.delete()
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                compressCapturedPhoto(capturedFile)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.rvReports)
        val btnCreate: Button = view.findViewById(R.id.btnCreateReport)
        val btnRefresh: Button = view.findViewById(R.id.btnRefreshReports)
        val progress: ProgressBar = view.findViewById(R.id.progressReports)
        val tvError: TextView = view.findViewById(R.id.tvReportsError)

        adapter =
            ReportsAdapter(
                onEdit = { report -> showReportDialog(report) },
                onToggle = { report -> viewModel.toggleReportStatus(report.id, report.status) },
                onOpenEvidenceViewer = { report, initialIndex ->
                    showEvidenceViewer(report.evidence?.imageUrls.orEmpty(), initialIndex)
                },
            )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnCreate.setOnClickListener { showReportDialog(null) }
        btnRefresh.setOnClickListener { viewModel.loadReports() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    adapter.submitList(state.reports)

                    if (state.errorMessage.isNullOrBlank()) {
                        tvError.visibility = View.GONE
                    } else {
                        tvError.visibility = View.VISIBLE
                        tvError.text = state.errorMessage
                    }
                }
            }
        }
    }

    private fun showReportDialog(reportToEdit: Report?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_form, null)

        val etTitle: EditText = dialogView.findViewById(R.id.etTitle)
        val etDescription: EditText = dialogView.findViewById(R.id.etDescription)
        val etType: AppCompatAutoCompleteTextView = dialogView.findViewById(R.id.etType)
        val etObservation: EditText = dialogView.findViewById(R.id.etObservation)
        val btnTakeEvidencePhoto: Button = dialogView.findViewById(R.id.btnTakeEvidencePhoto)
        val tvEvidenceCounter: TextView = dialogView.findViewById(R.id.tvEvidenceCounter)
        val rvEvidenceImages: RecyclerView = dialogView.findViewById(R.id.rvEvidenceImages)
        val initialType =
            REPORT_TYPE_OPTIONS.firstOrNull { option ->
                option.value.normalizeForSearch() == reportToEdit?.type.orEmpty().normalizeForSearch()
            } ?: REPORT_TYPE_OPTIONS.first()
        var selectedType: DropdownOption<String>? = initialType

        val draftItems =
            reportToEdit?.evidence?.imageUrls.orEmpty()
                .map(ReportEvidenceDraftItem::remote)
                .toMutableList()

        val evidenceAdapter =
            ReportEvidenceDraftAdapter { item ->
                val currentItems = activeEvidenceItems ?: return@ReportEvidenceDraftAdapter
                currentItems.removeAll { it.id == item.id }
                item.localFile?.delete()
                activeEvidenceAdapter?.submitList(currentItems.toList())
                activeEvidenceCounter?.let { updateEvidenceCounter(it, currentItems.size) }
                activeCaptureButton?.isEnabled = currentItems.size < MAX_EVIDENCE_IMAGES
            }

        rvEvidenceImages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvEvidenceImages.adapter = evidenceAdapter
        evidenceAdapter.submitList(draftItems.toList())

        activeEvidenceItems = draftItems
        activeEvidenceAdapter = evidenceAdapter
        activeEvidenceCounter = tvEvidenceCounter
        activeCaptureButton = btnTakeEvidencePhoto

        updateEvidenceCounter(tvEvidenceCounter, draftItems.size)
        btnTakeEvidencePhoto.isEnabled = draftItems.size < MAX_EVIDENCE_IMAGES
        etType.bindSearchableDropdown(REPORT_TYPE_OPTIONS, initialType) { option ->
            selectedType = option
        }

        if (reportToEdit != null) {
            etTitle.setText(reportToEdit.title)
            etDescription.setText(reportToEdit.description.orEmpty())
            etObservation.setText(reportToEdit.evidence?.observation.orEmpty())
        }

        btnTakeEvidencePhoto.setOnClickListener {
            val currentCount = activeEvidenceItems?.size ?: 0
            if (currentCount >= MAX_EVIDENCE_IMAGES) {
                requireContext().toast("Solo puedes adjuntar hasta 10 imagenes por reporte.")
            } else {
                requestCameraAccess()
            }
        }

        var shouldCleanupDrafts = true

        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle(if (reportToEdit == null) "Crear reporte" else "Editar reporte")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim().ifBlank { null }
                val type = selectedType?.value
                val observation = etObservation.text.toString().trim().ifBlank { null }

                if (title.isBlank() || type.isNullOrBlank()) {
                    requireContext().toast("Titulo y tipo son obligatorios.")
                    return@setOnClickListener
                }

                val evidenceItems = activeEvidenceItems.orEmpty().toList()
                val retainedImageUrls = evidenceItems.mapNotNull { it.remoteUrl }
                val newImageFiles = evidenceItems.mapNotNull { it.localFile }

                positiveButton.isEnabled = false
                btnTakeEvidencePhoto.isEnabled = false

                viewModel.submitReport(
                    currentReport = reportToEdit,
                    title = title,
                    description = description,
                    type = type,
                    observation = observation,
                    retainedImageUrls = retainedImageUrls,
                    newImageFiles = newImageFiles,
                ) { result ->
                    when (result) {
                        is ReportSubmitResult.Success -> {
                            cleanupLocalDrafts(evidenceItems)
                            shouldCleanupDrafts = false
                            dialog.dismiss()
                            requireContext().toast("Reporte guardado correctamente.")
                        }

                        is ReportSubmitResult.PartialSuccess -> {
                            cleanupLocalDrafts(evidenceItems)
                            shouldCleanupDrafts = false
                            dialog.dismiss()
                            requireContext().toast(result.message)
                        }

                        is ReportSubmitResult.Failure -> {
                            positiveButton.isEnabled = true
                            btnTakeEvidencePhoto.isEnabled =
                                (activeEvidenceItems?.size ?: 0) < MAX_EVIDENCE_IMAGES
                            requireContext().toast(result.message)
                        }
                    }
                }
            }
        }

        dialog.setOnDismissListener {
            if (shouldCleanupDrafts) {
                cleanupLocalDrafts(activeEvidenceItems.orEmpty())
            }
            pendingCameraFile?.delete()
            pendingCameraFile = null
            activeEvidenceItems = null
            activeEvidenceAdapter = null
            activeEvidenceCounter = null
            activeCaptureButton = null
        }

        dialog.show()
    }

    private fun requestCameraAccess() {
        val permissionState =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permissionState == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCameraCapture()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraCapture() {
        val currentItems = activeEvidenceItems ?: return
        if (currentItems.size >= MAX_EVIDENCE_IMAGES) {
            requireContext().toast("Solo puedes adjuntar hasta 10 imagenes por reporte.")
            return
        }

        val cameraDirectory = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val captureFile = File(cameraDirectory, "report-${UUID.randomUUID()}.jpg")

        val contentUri =
            FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                captureFile,
            )

        pendingCameraFile = captureFile
        takePictureLauncher.launch(contentUri)
    }

    private suspend fun compressCapturedPhoto(capturedFile: File) {
        val result =
            withContext(Dispatchers.IO) {
                ReportImageCompressor.compressToWebp(requireContext(), capturedFile)
            }

        when (result) {
            is CompressionResult.Success -> {
                val currentItems = activeEvidenceItems
                if (currentItems == null) {
                    result.file.delete()
                    return
                }

                if (currentItems.size >= MAX_EVIDENCE_IMAGES) {
                    result.file.delete()
                    requireContext().toast("Ya alcanzaste el maximo de 10 imagenes.")
                    return
                }

                currentItems.add(ReportEvidenceDraftItem.local(result.file))
                activeEvidenceAdapter?.submitList(currentItems.toList())
                activeEvidenceCounter?.let { updateEvidenceCounter(it, currentItems.size) }
                activeCaptureButton?.isEnabled = currentItems.size < MAX_EVIDENCE_IMAGES
            }

            is CompressionResult.Failure -> {
                requireContext().toast(result.message)
            }
        }
    }

    private fun updateEvidenceCounter(
        counterView: TextView,
        currentCount: Int,
    ) {
        counterView.text = "$currentCount / 10 imagenes"
    }

    private fun showEvidenceViewer(
        imageUrls: List<String>,
        initialIndex: Int,
    ) {
        if (imageUrls.isEmpty()) {
            return
        }

        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_image_viewer, null)
        val backdrop: View = dialogView.findViewById(R.id.layoutEvidenceViewerBackdrop)
        val content: View = dialogView.findViewById(R.id.layoutEvidenceViewerContent)
        val closeButton: ImageButton = dialogView.findViewById(R.id.btnCloseEvidenceViewer)
        val counter: TextView = dialogView.findViewById(R.id.tvEvidenceViewerCounter)
        val viewPager: ViewPager2 = dialogView.findViewById(R.id.vpEvidenceViewer)
        val safeInitialIndex = initialIndex.coerceIn(0, imageUrls.lastIndex)

        viewPager.adapter = ReportEvidenceViewerPagerAdapter(imageUrls)
        updateViewerCounter(counter, safeInitialIndex, imageUrls.size)
        viewPager.setCurrentItem(safeInitialIndex, false)

        val pageChangeCallback =
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateViewerCounter(counter, position, imageUrls.size)
                }
            }

        viewPager.registerOnPageChangeCallback(pageChangeCallback)
        closeButton.setOnClickListener { dialog.dismiss() }
        backdrop.setOnClickListener { dialog.dismiss() }
        content.setOnClickListener { }

        dialog.setContentView(dialogView)
        dialog.setOnDismissListener {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        }
        dialog.show()
    }

    private fun updateViewerCounter(
        counterView: TextView,
        currentIndex: Int,
        totalImages: Int,
    ) {
        counterView.text = "${currentIndex + 1} / $totalImages"
    }

    private fun cleanupLocalDrafts(items: List<ReportEvidenceDraftItem>) {
        items.forEach { draftItem ->
            draftItem.localFile?.delete()
        }
    }

    companion object {
        private const val MAX_EVIDENCE_IMAGES = 10
        private val REPORT_TYPE_OPTIONS =
            listOf(
                DropdownOption("General", "General"),
                DropdownOption("Inventario", "Inventario"),
                DropdownOption("Movimientos", "Movimientos"),
                DropdownOption("Tareas", "Tareas"),
            )
    }
}
