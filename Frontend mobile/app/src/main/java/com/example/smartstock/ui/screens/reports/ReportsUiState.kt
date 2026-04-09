package com.example.smartstock.ui.screens.reports

import com.example.smartstock.domain.model.Report

data class ReportsUiState(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val errorMessage: String? = null
)
