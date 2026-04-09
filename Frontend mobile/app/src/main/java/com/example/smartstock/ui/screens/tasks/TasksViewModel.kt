package com.example.smartstock.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstock.domain.model.CreateTaskRequest
import com.example.smartstock.domain.model.UpdateTaskRequest
import com.example.smartstock.domain.usecase.ActivateTaskUseCase
import com.example.smartstock.domain.usecase.CreateTaskUseCase
import com.example.smartstock.domain.usecase.DeactivateTaskUseCase
import com.example.smartstock.domain.usecase.DeleteTaskUseCase
import com.example.smartstock.domain.usecase.GetTaskByIdUseCase
import com.example.smartstock.domain.usecase.GetTasksByUserUseCase
import com.example.smartstock.domain.usecase.GetTasksUseCase
import com.example.smartstock.domain.usecase.GetUsersUseCase
import com.example.smartstock.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TasksViewModel
    @Inject
    constructor(
        private val getTasksUseCase: GetTasksUseCase,
        private val getTaskByIdUseCase: GetTaskByIdUseCase,
        private val getTasksByUserUseCase: GetTasksByUserUseCase,
        private val getUsersUseCase: GetUsersUseCase,
        private val createTaskUseCase: CreateTaskUseCase,
        private val updateTaskUseCase: UpdateTaskUseCase,
        private val activateTaskUseCase: ActivateTaskUseCase,
        private val deactivateTaskUseCase: DeactivateTaskUseCase,
        private val deleteTaskUseCase: DeleteTaskUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TasksUiState())
        val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

        init {
            loadTasks()
        }

        fun loadTasks() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getTasksUseCase() }
                    .onSuccess { tasks -> _uiState.update { it.copy(isLoading = false, tasks = tasks) } }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar las tareas"
                            )
                        }
                    }
            }
        }

        fun getTaskById(id: Int) {
            viewModelScope.launch {
                runCatching { getTaskByIdUseCase(id) }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo cargar la tarea") }
                    }
            }
        }

        fun loadTasksByUser(userId: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getTasksByUserUseCase(userId) }
                    .onSuccess { tasks -> _uiState.update { it.copy(isLoading = false, tasks = tasks) } }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar las tareas del usuario"
                            )
                        }
                    }
            }
        }

        fun createTask(request: CreateTaskRequest) {
            viewModelScope.launch {
                runCatching { createTaskUseCase(request) }
                    .onSuccess { loadTasks() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo crear la tarea") }
                    }
            }
        }

        fun updateTask(
            id: Int,
            request: UpdateTaskRequest
        ) {
            viewModelScope.launch {
                runCatching { updateTaskUseCase(id, request) }
                    .onSuccess { loadTasks() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar la tarea") }
                    }
            }
        }

        fun toggleTaskStatus(
            id: Int,
            isActive: Boolean
        ) {
            viewModelScope.launch {
                runCatching {
                    if (isActive) deactivateTaskUseCase(id) else activateTaskUseCase(id)
                }.onSuccess {
                    loadTasks()
                }.onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el estado") }
                }
            }
        }

        fun deleteTask(id: Int) {
            viewModelScope.launch {
                runCatching { deleteTaskUseCase(id) }
                    .onSuccess { loadTasks() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo eliminar la tarea") }
                    }
            }
        }

        fun loadUsers() {
            viewModelScope.launch {
                runCatching { getUsersUseCase() }
                    .onSuccess { users ->
                        _uiState.update { it.copy(users = users) }
                    }
                    .onFailure {
                        // Si no hay permiso para leer usuarios, se mantiene la UI con los nombres que llegan en tareas.
                    }
            }
        }
    }
