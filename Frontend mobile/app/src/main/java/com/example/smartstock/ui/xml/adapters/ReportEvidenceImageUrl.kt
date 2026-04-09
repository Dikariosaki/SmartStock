package com.example.smartstock.ui.xml.adapters

import com.example.smartstock.BuildConfig

internal fun String.toReportEvidenceMobileUrl(): String =
    resolveEvidenceUrl()
        .replace("://localhost", "://10.0.2.2")
        .replace("://127.0.0.1", "://10.0.2.2")

private fun String.resolveEvidenceUrl(): String {
    val normalizedValue = trim()
    if (normalizedValue.isBlank()) {
        return normalizedValue
    }

    if (normalizedValue.startsWith("http://", ignoreCase = true) || normalizedValue.startsWith("https://", ignoreCase = true)) {
        return normalizedValue
    }

    val normalizedBaseUrl = BuildConfig.API_BASE_URL.removeSuffix("/")
    val normalizedPath = if (normalizedValue.startsWith("/")) normalizedValue else "/$normalizedValue"
    return normalizedBaseUrl + normalizedPath
}
