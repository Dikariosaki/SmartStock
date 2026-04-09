package com.example.smartstock.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstock.domain.usecase.LoginUseCase
import com.example.smartstock.domain.usecase.LogoutUseCase
import com.example.smartstock.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class SessionViewModel
    @Inject
    constructor(
        observeSessionUseCase: ObserveSessionUseCase,
        private val loginUseCase: LoginUseCase,
        private val logoutUseCase: LogoutUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SessionUiState())
        val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                observeSessionUseCase().collect { session ->
                    _uiState.update {
                        it.copy(
                            session = session,
                            isLoading = false,
                        )
                    }
                }
            }
        }

        fun login(email: String, password: String) {
            val safeEmail = email.trim()
            val safePassword = password.trim()
            if (safeEmail.isBlank() || safePassword.isBlank()) {
                _uiState.update { it.copy(loginError = "Completa correo y contrasena") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, loginError = null) }
                runCatching { loginUseCase(safeEmail, safePassword) }
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                loginError = null,
                            )
                        }
                    }
                    .onFailure { throwable ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                loginError = toReadableError(throwable),
                            )
                        }
                    }
            }
        }

        fun clearLoginError() {
            _uiState.update { it.copy(loginError = null) }
        }

        fun logout() {
            logoutUseCase()
        }

        private fun toReadableError(throwable: Throwable): String {
            if (throwable is HttpException && throwable.code() == 404) {
                return "Endpoint de login no encontrado. Verifica API_BASE_URL."
            }
            if (throwable is HttpException && throwable.code() == 401) {
                return "Credenciales invalidas"
            }
            return throwable.message ?: "No se pudo iniciar sesion"
        }
    }
