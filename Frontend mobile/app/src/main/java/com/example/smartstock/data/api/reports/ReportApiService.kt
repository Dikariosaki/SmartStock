package com.example.smartstock.data.api

import com.example.smartstock.data.model.ReportCreateRequestDto
import com.example.smartstock.data.model.ReportDto
import com.example.smartstock.data.model.ReportUpdateRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Part

interface ReportApiService {
    @GET("api/reportes")
    suspend fun getReports(): List<ReportDto>

    @GET("api/reportes/{id}")
    suspend fun getReportById(@Path("id") id: Int): ReportDto

    @POST("api/reportes")
    suspend fun createReport(@Body request: ReportCreateRequestDto): ReportDto

    @PUT("api/reportes/{id}")
    suspend fun updateReport(
        @Path("id") id: Int,
        @Body request: ReportUpdateRequestDto
    ): ReportDto

    @Multipart
    @PUT("api/reportes/{id}/evidencia")
    suspend fun updateReportEvidence(
        @Path("id") id: Int,
        @Part("observation") observation: RequestBody?,
        @Part retainedImageUrls: List<MultipartBody.Part>,
        @Part newImages: List<MultipartBody.Part>,
    ): ReportDto

    @PUT("api/reportes/{id}/activate")
    suspend fun activateReport(@Path("id") id: Int)

    @PUT("api/reportes/{id}/deactivate")
    suspend fun deactivateReport(@Path("id") id: Int)
}
