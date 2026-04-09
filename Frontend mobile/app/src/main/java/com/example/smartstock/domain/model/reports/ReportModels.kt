package com.example.smartstock.domain.model

data class ReportEvidence(
    val imageUrls: List<String> = emptyList(),
    val observation: String? = null,
)

data class Report(
    val id: Int,
    val title: String,
    val description: String?,
    val evidence: ReportEvidence? = null,
    val createdAt: String,
    val type: String,
    val status: Boolean,
)

data class CreateReportRequest(
    val title: String,
    val description: String?,
    val type: String,
)

data class UpdateReportRequest(
    val title: String,
    val description: String?,
    val type: String,
)
