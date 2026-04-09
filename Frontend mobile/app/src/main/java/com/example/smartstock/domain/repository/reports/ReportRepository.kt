package com.example.smartstock.domain.repository

import com.example.smartstock.domain.model.CreateReportRequest
import com.example.smartstock.domain.model.Report
import com.example.smartstock.domain.model.UpdateReportRequest
import java.io.File

interface ReportRepository {
    suspend fun getReports(): List<Report>
    suspend fun getReportById(id: Int): Report
    suspend fun createReport(request: CreateReportRequest): Report
    suspend fun updateReport(id: Int, request: UpdateReportRequest): Report
    suspend fun updateReportEvidence(
        id: Int,
        observation: String?,
        retainedImageUrls: List<String>,
        newImageFiles: List<File>,
    ): Report
    suspend fun activateReport(id: Int)
    suspend fun deactivateReport(id: Int)
}
