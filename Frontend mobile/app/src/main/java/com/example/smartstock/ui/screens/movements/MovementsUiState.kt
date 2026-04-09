package com.example.smartstock.ui.screens.movements

import com.example.smartstock.domain.model.Movement

data class MovementsUiState(
    val isLoading: Boolean = false,
    val movements: List<Movement> = emptyList(),
    val errorMessage: String? = null
)
