package com.example.smartstock.core

import java.text.Normalizer

fun normalizeRoleName(roleName: String?): String? {
    val rawRole = roleName?.trim().orEmpty()
    if (rawRole.isBlank()) return null

    val normalized =
        Normalizer
            .normalize(rawRole, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()
            .removePrefix("role_")
            .removePrefix("role ")
            .removePrefix("rol_")
            .removePrefix("rol ")
            .trim()

    return when {
        normalized.contains("admin") -> "administrador"
        normalized.contains("super") -> "supervisor"
        normalized.contains("aux") -> "auxiliar"
        else -> normalized
    }
}
