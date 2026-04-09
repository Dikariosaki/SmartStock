package com.example.smartstock.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstock.domain.model.CreateReportRequest
import com.example.smartstock.domain.model.Report
import com.example.smartstock.domain.model.UpdateReportRequest
import com.example.smartstock.domain.usecase.ActivateReportUseCase
import com.example.smartstock.domain.usecase.CreateReportUseCase
import com.example.smartstock.domain.usecase.DeactivateReportUseCase
import com.example.smartstock.domain.usecase.GetReportByIdUseCase
import com.example.smartstock.domain.usecase.GetReportsUseCase
import com.example.smartstock.domain.usecase.UpdateReportEvidenceUseCase
import com.example.smartstock.domain.usecase.UpdateReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ReportSubmitResult {
    data class Success(val report: Report) : ReportSubmitResult

    data class PartialSuccess(
        val report: Report,
        val message: String,
    ) : ReportSubmitResult

    data class Failure(val message: String) : ReportSubmitResult
}

@HiltViewModel
class ReportsViewModel
    @Inject
    constructor(
        private val getReportsUseCase: GetReportsUseCase,
        private val getReportByIdUseCase: GetReportByIdUseCase,
        private val createReportUseCase: CreateReportUseCase,
        private val updateReportUseCase: UpdateReportUseCase,
        private val updateReportEvidenceUseCase: UpdateReportEvidenceUseCase,
        private val activateReportUseCase: ActivateReportUseCase,
        private val deactivateReportUseCase: DeactivateReportUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReportsUiState())
        val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

        init {
            loadReports()
        }

        fun loadReports() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getReportsUseCase() }
                    .onSuccess { reports -> _uiState.update { it.copy(isLoading = false, reports = reports) } }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar los reportes"
                            )
                        }
                    }
            }
        }

        fun getReportById(id: Int) {
            viewModelScope.launch {
                runCatching { getReportByIdUseCase(id) }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo cargar el reporte") }
                    }
            }
        }

        fun createReport(request: CreateReportRequest) {
            viewModelScope.launch {
                runCatching { createReportUseCase(request) }
                    .onSuccess { loadReports() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo crear el reporte") }
                    }
            }
        }

        fun updateReport(
            id: Int,
            request: UpdateReportRequest
        ) {
            viewModelScope.launch {
                runCatching { updateReportUseCase(id, request) }
                    .onSuccess { loadReports() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el reporte") }
                }
            }
        }

        fun submitReport(
            currentReport: Report?,
            title: String,
            description: String?,
            type: String,
            observation: String?,
            retainedImageUrls: List<String>,
            newImageFiles: List<File>,
            onResult: (ReportSubmitResult) -> Unit,
        ) {
            viewModelScope.launch {
                val normalizedObservation = observation?.trim()?.ifBlank { null }
                val shouldSyncEvidence =
                    currentReport?.evidence != null ||
                        normalizedObservation != null ||
                        retainedImageUrls.isNotEmpty() ||
                        newImageFiles.isNotEmpty()

                runCatching {
                    val savedReport =
                        if (currentReport == null) {
                            createReportUseCase(
                                CreateReportRequest(
                                    title = title,
                                    description = description,
                                    type = type,
                                ),
                            )
                        } else {
                            updateReportUseCase(
                                currentReport.id,
                                UpdateReportRequest(
                                    title = title,
                                    description = description,
                                    type = type,
                                ),
                            )
                        }

                    if (!shouldSyncEvidence) {
                        ReportSubmitResult.Success(savedReport)
                    } else {
                        runCatching {
                            updateReportEvidenceUseCase(
                                savedReport.id,
                                normalizedObservation,
                                retainedImageUrls,
                                newImageFiles,
                            )
                        }.fold(
                            onSuccess = { updatedReport -> ReportSubmitResult.Success(updatedReport) },
                            onFailure = { throwable ->
                                ReportSubmitResult.PartialSuccess(
                                    savedReport,
                                    throwable.message
                                        ?: "El reporte se guardó, pero la evidencia no pudo sincronizarse.",
                                )
                            },
                        )
                    }
                }.onSuccess { result ->
                    loadReports()
                    onResult(result)
                }.onFailure { throwable ->
                    val message = throwable.message ?: "No se pudo guardar el reporte"
                    _uiState.update { it.copy(errorMessage = message) }
                    onResult(ReportSubmitResult.Failure(message))
                }
            }
        }

        fun toggleReportStatus(
            id: Int,
            isActive: Boolean
        ) {
            viewModelScope.launch {
                runCatching {
                    if (isActive) deactivateReportUseCase(id) else activateReportUseCase(id)
                }.onSuccess {
                    loadReports()
                }.onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el estado") }
                }
            }
        }
    }
