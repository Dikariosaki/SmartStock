package com.example.smartstock.domain.model

data class Inventory(
    val id: Int,
    val productId: Int,
    val productName: String?,
    val location: String?,
    val quantity: Int,
    val reorderPoint: Int,
    val status: Boolean
)

data class UpdateInventoryRequest(
    val location: String?,
    val reorderPoint: Int
)
