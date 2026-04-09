package com.example.smartstock.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.User
import com.example.smartstock.domain.usecase.ActivateUserUseCase
import com.example.smartstock.domain.usecase.CreateUserUseCase
import com.example.smartstock.domain.usecase.DeactivateUserUseCase
import com.example.smartstock.domain.usecase.DeleteUserUseCase
import com.example.smartstock.domain.usecase.GetRolesUseCase
import com.example.smartstock.domain.usecase.GetUsersUseCase
import com.example.smartstock.domain.usecase.UpdateUserUseCase
import com.example.smartstock.ui.xml.normalizeForSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class UsersViewModel
    @Inject
    constructor(
        private val getUsersUseCase: GetUsersUseCase,
        private val getRolesUseCase: GetRolesUseCase,
        private val createUserUseCase: CreateUserUseCase,
        private val updateUserUseCase: UpdateUserUseCase,
        private val deleteUserUseCase: DeleteUserUseCase,
        private val activateUserUseCase: ActivateUserUseCase,
        private val deactivateUserUseCase: DeactivateUserUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(UsersUiState())
        val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

        init {
            loadUsers()
            loadRoles()
        }

        fun loadUsers() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getUsersUseCase() }
                    .onSuccess { users ->
                        val search = _uiState.value.searchText
                        val filtered = filterUsers(users, search)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                users = users,
                                filteredUsers = filtered
                            )
                        }
                    }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Error cargando usuarios"
                            )
                        }
                    }
            }
        }

        fun onSearchChange(value: String) {
            _uiState.update {
                it.copy(
                    searchText = value,
                    filteredUsers = filterUsers(it.users, value)
                )
            }
        }

        fun toggleUserStatus(userId: Int, isActive: Boolean) {
            viewModelScope.launch {
                runCatching {
                    if (isActive) deactivateUserUseCase(userId) else activateUserUseCase(userId)
                }.onSuccess {
                    loadUsers()
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el estado")
                    }
                }
            }
        }

        fun createUser(request: CreateUserRequest) {
            viewModelScope.launch {
                runCatching { createUserUseCase(request) }
                    .onSuccess { loadUsers() }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(errorMessage = throwable.message ?: "No se pudo crear el usuario")
                        }
                    }
            }
        }

        fun loadRoles() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingRoles = true) }
                runCatching { getRolesUseCase() }
                    .onSuccess { roles ->
                        _uiState.update {
                            it.copy(
                                isLoadingRoles = false,
                                roles = roles
                            )
                        }
                    }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoadingRoles = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar los roles"
                            )
                        }
                    }
            }
        }

        fun updateUser(
            id: Int,
            request: CreateUserRequest
        ) {
            viewModelScope.launch {
                runCatching { updateUserUseCase(id, request) }
                    .onSuccess { loadUsers() }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el usuario")
                        }
                    }
            }
        }

        fun deleteUser(id: Int) {
            viewModelScope.launch {
                runCatching { deleteUserUseCase(id) }
                    .onSuccess { loadUsers() }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(errorMessage = throwable.message ?: "No se pudo eliminar el usuario")
                        }
                    }
            }
        }

        private fun filterUsers(
            users: List<User>,
            query: String
        ): List<User> {
            if (query.isBlank()) return users
            val q = query.normalizeForSearch()
            return users.filter { user ->
                user.name.normalizeForSearch().contains(q) ||
                    user.email.normalizeForSearch().contains(q) ||
                    user.cedula.toString().contains(q)
            }
        }
    }
