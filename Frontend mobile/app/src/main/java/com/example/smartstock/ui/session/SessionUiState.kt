package com.example.smartstock.ui.session

import com.example.smartstock.domain.model.SessionUser

data class SessionUiState(
    val session: SessionUser? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null,
)
