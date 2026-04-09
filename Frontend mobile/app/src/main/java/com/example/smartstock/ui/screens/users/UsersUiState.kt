package com.example.smartstock.ui.screens.users

import com.example.smartstock.domain.model.RoleOption
import com.example.smartstock.domain.model.User

data class UsersUiState(
    val isLoading: Boolean = false,
    val isLoadingRoles: Boolean = false,
    val users: List<User> = emptyList(),
    val roles: List<RoleOption> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val searchText: String = "",
    val errorMessage: String? = null
)
