package com.example.smartstock.ui.screens.movements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstock.domain.usecase.GetMovementsByInventoryUseCase
import com.example.smartstock.domain.usecase.GetMovementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MovementsViewModel
    @Inject
    constructor(
        private val getMovementsUseCase: GetMovementsUseCase,
        private val getMovementsByInventoryUseCase: GetMovementsByInventoryUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MovementsUiState())
        val uiState: StateFlow<MovementsUiState> = _uiState.asStateFlow()

        init {
            loadMovements()
        }

        fun loadMovements() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getMovementsUseCase() }
                    .onSuccess { movements -> _uiState.update { it.copy(isLoading = false, movements = movements) } }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar los movimientos"
                            )
                        }
                    }
            }
        }

        fun loadMovementsByInventory(inventoryId: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getMovementsByInventoryUseCase(inventoryId) }
                    .onSuccess { movements ->
                        _uiState.update { it.copy(isLoading = false, movements = movements) }
                    }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar los movimientos del inventario"
                            )
                        }
                    }
            }
        }
    }
