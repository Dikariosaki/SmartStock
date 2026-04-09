package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ReportEvidenceDto(
    @JsonProperty("imageUrls") val imageUrls: List<String> = emptyList(),
    @JsonProperty("observation") val observation: String? = null,
)

data class ReportResponse(
    @JsonProperty("reporteId") val id: Int?,
    @JsonProperty("titulo") val title: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("evidencia") val evidence: ReportEvidenceDto? = null,
    @JsonProperty("fechaCreado") val createdAt: LocalDateTime,
    @JsonProperty("tipoReporte") val type: String,
    @JsonProperty("estado") val status: Boolean,
)

data class ReportCreateRequest(
    @JsonProperty("titulo") val title: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("tipoReporte") val type: String,
)

data class ReportUpdateRequest(
    @JsonProperty("titulo") val title: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("tipoReporte") val type: String,
)
