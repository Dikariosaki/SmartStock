package com.example.smartstock.data.model

data class InventoryDto(
    val inventarioId: Int?,
    val productoId: Int,
    val productoNombre: String?,
    val ubicacion: String?,
    val cantidad: Int,
    val puntoReorden: Int,
    val estado: Boolean
)

data class InventoryUpdateRequestDto(
    val ubicacion: String?,
    val puntoReorden: Int
)
