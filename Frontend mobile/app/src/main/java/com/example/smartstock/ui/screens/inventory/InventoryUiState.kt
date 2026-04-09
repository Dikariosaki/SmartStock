package com.example.smartstock.ui.screens.inventory

import com.example.smartstock.domain.model.User
import com.example.smartstock.domain.model.Inventory

data class InventoryUiState(
    val isLoading: Boolean = false,
    val inventories: List<Inventory> = emptyList(),
    val users: List<User> = emptyList(),
    val errorMessage: String? = null
)
