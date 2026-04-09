package com.example.smartstock.domain.model

data class MovementStockRequest(
    val productId: Int,
    val quantity: Int,
    val userId: Int,
    val type: String,
    val batch: String? = null
)

data class Movement(
    val id: Int,
    val inventoryId: Int,
    val userId: Int,
    val userName: String?,
    val productName: String?,
    val inventoryLocation: String?,
    val type: String,
    val quantity: Int,
    val date: String?,
    val batch: String?,
    val status: Boolean
)
