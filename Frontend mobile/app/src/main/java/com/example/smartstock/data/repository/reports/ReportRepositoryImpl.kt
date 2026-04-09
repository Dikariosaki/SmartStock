package com.example.smartstock.data.repository

import com.example.smartstock.data.api.ReportApiService
import com.example.smartstock.data.model.toDomain
import com.example.smartstock.data.model.toDto
import com.example.smartstock.domain.model.CreateReportRequest
import com.example.smartstock.domain.model.Report
import com.example.smartstock.domain.model.UpdateReportRequest
import com.example.smartstock.domain.repository.ReportRepository
import java.io.File
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ReportRepositoryImpl
    @Inject
    constructor(
        private val reportApiService: ReportApiService
    ) : ReportRepository {
        override suspend fun getReports(): List<Report> = reportApiService.getReports().map { it.toDomain() }

        override suspend fun getReportById(id: Int): Report = reportApiService.getReportById(id).toDomain()

        override suspend fun createReport(request: CreateReportRequest): Report = reportApiService.createReport(request.toDto()).toDomain()

        override suspend fun updateReport(
            id: Int,
            request: UpdateReportRequest
        ): Report = reportApiService.updateReport(id, request.toDto()).toDomain()

        override suspend fun updateReportEvidence(
            id: Int,
            observation: String?,
            retainedImageUrls: List<String>,
            newImageFiles: List<File>,
        ): Report {
            val observationBody = observation?.toRequestBody("text/plain".toMediaType())
            val retainedParts =
                retainedImageUrls.map { url ->
                    MultipartBody.Part.createFormData("retainedImageUrls", url)
                }
            val imageParts =
                newImageFiles.map { file ->
                    MultipartBody.Part.createFormData(
                        "newImages",
                        file.name,
                        file.asRequestBody("image/webp".toMediaType()),
                    )
                }

            return reportApiService
                .updateReportEvidence(
                    id = id,
                    observation = observationBody,
                    retainedImageUrls = retainedParts,
                    newImages = imageParts,
                ).toDomain()
        }

        override suspend fun activateReport(id: Int) {
            reportApiService.activateReport(id)
        }

        override suspend fun deactivateReport(id: Int) {
            reportApiService.deactivateReport(id)
        }
    }
