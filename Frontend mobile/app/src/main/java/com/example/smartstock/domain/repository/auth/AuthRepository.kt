package com.example.smartstock.domain.repository

import com.example.smartstock.domain.model.SessionUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    suspend fun login(email: String, password: String): SessionUser

    fun logout()

    fun observeSession(): StateFlow<SessionUser?>

    fun currentSession(): SessionUser?
}
