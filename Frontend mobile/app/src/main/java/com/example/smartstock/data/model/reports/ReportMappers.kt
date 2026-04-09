package com.example.smartstock.data.model

import com.example.smartstock.domain.model.CreateReportRequest
import com.example.smartstock.domain.model.Report
import com.example.smartstock.domain.model.ReportEvidence
import com.example.smartstock.domain.model.UpdateReportRequest

fun ReportDto.toDomain(): Report =
    Report(
        id = reporteId ?: 0,
        title = titulo,
        description = descripcion,
        evidence = evidencia?.toDomain(),
        createdAt = fechaCreado,
        type = tipoReporte,
        status = estado,
    )

fun CreateReportRequest.toDto(): ReportCreateRequestDto =
    ReportCreateRequestDto(
        titulo = title,
        descripcion = description,
        tipoReporte = type,
    )

fun UpdateReportRequest.toDto(): ReportUpdateRequestDto =
    ReportUpdateRequestDto(
        titulo = title,
        descripcion = description,
        tipoReporte = type,
    )

private fun ReportEvidenceDto.toDomain(): ReportEvidence =
    ReportEvidence(
        imageUrls = imageUrls,
        observation = observation,
    )
