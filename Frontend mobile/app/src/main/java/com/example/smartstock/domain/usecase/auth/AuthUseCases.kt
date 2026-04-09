package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.SessionUser
import com.example.smartstock.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class LoginUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): SessionUser {
        return authRepository.login(email, password)
    }
}

class LogoutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke() {
        authRepository.logout()
    }
}

class ObserveSessionUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(): StateFlow<SessionUser?> = authRepository.observeSession()
}

class GetCurrentSessionUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(): SessionUser? = authRepository.currentSession()
}
