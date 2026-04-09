package com.example.smartstock.data.repository

import com.example.smartstock.data.api.AuthApiService
import com.example.smartstock.data.model.AuthLoginRequestDto
import com.example.smartstock.data.session.SessionManager
import com.example.smartstock.core.normalizeRoleName
import com.example.smartstock.domain.model.SessionUser
import com.example.smartstock.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl
    @Inject
    constructor(
        private val authApiService: AuthApiService,
        private val sessionManager: SessionManager
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): SessionUser {
            val response =
                authApiService.login(
                    AuthLoginRequestDto(
                        email = email.trim(),
                        password = password,
                    ),
                )

            val sessionUser =
                SessionUser(
                    token = response.token,
                    userId = response.usuarioId,
                    roleId = response.rolId,
                    roleName = normalizeRoleName(response.rolNombre) ?: response.rolNombre.trim().lowercase(),
                    name = response.nombre,
                    email = response.email,
                )
            sessionManager.saveSession(sessionUser)
            return sessionUser
        }

        override fun logout() {
            sessionManager.clearSession()
        }

        override fun observeSession(): StateFlow<SessionUser?> = sessionManager.session

        override fun currentSession(): SessionUser? = sessionManager.currentSession()
    }
