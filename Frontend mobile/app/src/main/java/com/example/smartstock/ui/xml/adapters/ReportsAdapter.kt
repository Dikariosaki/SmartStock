package com.example.smartstock.ui.xml.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartstock.R
import com.example.smartstock.domain.model.Report

class ReportsAdapter(
    private val onEdit: (Report) -> Unit,
    private val onToggle: (Report) -> Unit,
    private val onOpenEvidenceViewer: (Report, Int) -> Unit,
) : ListAdapter<Report, ReportsAdapter.ReportViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view, onEdit, onToggle, onOpenEvidenceViewer)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReportViewHolder(
        itemView: View,
        private val onEdit: (Report) -> Unit,
        private val onToggle: (Report) -> Unit,
        private val onOpenEvidenceViewer: (Report, Int) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvReportTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvReportDescription)
        private val tvType: TextView = itemView.findViewById(R.id.tvReportType)
        private val tvDate: TextView = itemView.findViewById(R.id.tvReportDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvReportStatus)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditReport)
        private val btnToggle: Button = itemView.findViewById(R.id.btnToggleReport)
        private val evidenceLayout: LinearLayout = itemView.findViewById(R.id.layoutEvidenceSummary)
        private val rvEvidenceImages: RecyclerView = itemView.findViewById(R.id.rvReportEvidencePreview)
        private val imageCount: TextView = itemView.findViewById(R.id.tvReportImagesCount)
        private val observation: TextView = itemView.findViewById(R.id.tvReportObservation)
        private val galleryAdapter = ReportEvidenceGalleryAdapter(::handleImageClick)
        private var currentReport: Report? = null

        init {
            rvEvidenceImages.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            rvEvidenceImages.adapter = galleryAdapter
        }

        fun bind(item: Report) {
            currentReport = item

            tvTitle.text = item.title
            tvDescription.text = item.description ?: "Sin descripcion"
            tvType.text = "Tipo: ${item.type}"
            tvDate.text = "Fecha: ${item.createdAt}"
            tvStatus.text = if (item.status) "ACTIVO" else "INACTIVO"
            btnToggle.text = if (item.status) "Desactivar" else "Activar"

            val evidence = item.evidence
            val hasObservation = !evidence?.observation.isNullOrBlank()
            val imageUrls = evidence?.imageUrls.orEmpty()
            val hasImages = imageUrls.isNotEmpty()

            if (hasObservation || hasImages) {
                evidenceLayout.visibility = View.VISIBLE
                imageCount.text =
                    if (hasImages) {
                        "${imageUrls.size} imagen(es) cargadas"
                    } else {
                        "Sin imagenes adjuntas"
                    }
                observation.text =
                    evidence?.observation ?: "Sin observacion adicional para la evidencia."

                if (hasImages) {
                    rvEvidenceImages.visibility = View.VISIBLE
                    galleryAdapter.submitList(imageUrls)
                } else {
                    rvEvidenceImages.visibility = View.GONE
                    galleryAdapter.submitList(emptyList())
                }
            } else {
                evidenceLayout.visibility = View.GONE
                galleryAdapter.submitList(emptyList())
            }

            btnEdit.setOnClickListener { onEdit(item) }
            btnToggle.setOnClickListener { onToggle(item) }
        }

        private fun handleImageClick(index: Int) {
            currentReport?.let { report ->
                onOpenEvidenceViewer(report, index)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean = oldItem == newItem
    }
}
