package com.example.smartstock.ui.screens.tasks

import com.example.smartstock.domain.model.User
import com.example.smartstock.domain.model.Task

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val users: List<User> = emptyList(),
    val errorMessage: String? = null
)
