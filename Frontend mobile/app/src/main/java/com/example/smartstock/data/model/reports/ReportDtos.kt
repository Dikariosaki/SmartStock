package com.example.smartstock.data.model

data class ReportEvidenceDto(
    val imageUrls: List<String> = emptyList(),
    val observation: String? = null,
)

data class ReportDto(
    val reporteId: Int?,
    val titulo: String,
    val descripcion: String?,
    val evidencia: ReportEvidenceDto? = null,
    val fechaCreado: String,
    val tipoReporte: String,
    val estado: Boolean,
)

data class ReportCreateRequestDto(
    val titulo: String,
    val descripcion: String?,
    val tipoReporte: String,
)

data class ReportUpdateRequestDto(
    val titulo: String,
    val descripcion: String?,
    val tipoReporte: String,
)
