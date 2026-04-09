package com.example.smartstock.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstock.domain.model.MovementStockRequest
import com.example.smartstock.domain.model.UpdateInventoryRequest
import com.example.smartstock.domain.usecase.ActivateInventoryUseCase
import com.example.smartstock.domain.usecase.DeactivateInventoryUseCase
import com.example.smartstock.domain.usecase.GetInventoryByIdUseCase
import com.example.smartstock.domain.usecase.GetInventoriesUseCase
import com.example.smartstock.domain.usecase.GetUsersUseCase
import com.example.smartstock.domain.usecase.RegisterInventoryDamageUseCase
import com.example.smartstock.domain.usecase.RegisterInventoryEntryUseCase
import com.example.smartstock.domain.usecase.RegisterInventoryExitUseCase
import com.example.smartstock.domain.usecase.UpdateInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class InventoryViewModel
    @Inject
    constructor(
        private val getInventoriesUseCase: GetInventoriesUseCase,
        private val getInventoryByIdUseCase: GetInventoryByIdUseCase,
        private val getUsersUseCase: GetUsersUseCase,
        private val updateInventoryUseCase: UpdateInventoryUseCase,
        private val activateInventoryUseCase: ActivateInventoryUseCase,
        private val deactivateInventoryUseCase: DeactivateInventoryUseCase,
        private val registerInventoryEntryUseCase: RegisterInventoryEntryUseCase,
        private val registerInventoryExitUseCase: RegisterInventoryExitUseCase,
        private val registerInventoryDamageUseCase: RegisterInventoryDamageUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(InventoryUiState())
        val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

        init {
            loadInventories()
            loadUsers()
        }

        fun loadInventories() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getInventoriesUseCase() }
                    .onSuccess { inventories -> _uiState.update { it.copy(isLoading = false, inventories = inventories) } }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "No se pudo cargar el inventario"
                            )
                        }
                    }
            }
        }

        fun getInventoryById(id: Int) {
            viewModelScope.launch {
                runCatching { getInventoryByIdUseCase(id) }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo cargar el inventario") }
                    }
            }
        }

        fun updateInventory(
            id: Int,
            request: UpdateInventoryRequest
        ) {
            viewModelScope.launch {
                runCatching { updateInventoryUseCase(id, request) }
                    .onSuccess { loadInventories() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el inventario") }
                    }
            }
        }

        fun toggleInventoryStatus(
            id: Int,
            isActive: Boolean
        ) {
            viewModelScope.launch {
                runCatching {
                    if (isActive) deactivateInventoryUseCase(id) else activateInventoryUseCase(id)
                }.onSuccess {
                    loadInventories()
                }.onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el estado") }
                }
            }
        }

        fun registerEntry(request: MovementStockRequest) {
            viewModelScope.launch {
                runCatching { registerInventoryEntryUseCase(request) }
                    .onSuccess { loadInventories() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo registrar la entrada") }
                    }
            }
        }

        fun registerExit(request: MovementStockRequest) {
            viewModelScope.launch {
                runCatching { registerInventoryExitUseCase(request) }
                    .onSuccess { loadInventories() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo registrar la salida") }
                    }
            }
        }

        fun registerDamage(request: MovementStockRequest) {
            viewModelScope.launch {
                runCatching { registerInventoryDamageUseCase(request) }
                    .onSuccess { loadInventories() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo registrar la averia") }
                    }
            }
        }

        private fun loadUsers() {
            viewModelScope.launch {
                runCatching { getUsersUseCase() }
                    .onSuccess { users ->
                        _uiState.update { it.copy(users = users) }
                    }
                    .onFailure {
                        // Si el rol no tiene permiso para consultar usuarios, se usa el usuario de sesión en UI.
                    }
            }
        }
    }
