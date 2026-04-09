package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.CreateReportRequest
import com.example.smartstock.domain.model.Report
import com.example.smartstock.domain.model.UpdateReportRequest
import com.example.smartstock.domain.repository.ReportRepository
import java.io.File
import javax.inject.Inject

class GetReportsUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(): List<Report> = reportRepository.getReports()
}

class GetReportByIdUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(id: Int): Report = reportRepository.getReportById(id)
}

class CreateReportUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(request: CreateReportRequest): Report = reportRepository.createReport(request)
}

class UpdateReportUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(id: Int, request: UpdateReportRequest): Report = reportRepository.updateReport(id, request)
}

class UpdateReportEvidenceUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(
        id: Int,
        observation: String?,
        retainedImageUrls: List<String>,
        newImageFiles: List<File>,
    ): Report = reportRepository.updateReportEvidence(id, observation, retainedImageUrls, newImageFiles)
}

class ActivateReportUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(id: Int) = reportRepository.activateReport(id)
}

class DeactivateReportUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(id: Int) = reportRepository.deactivateReport(id)
}
