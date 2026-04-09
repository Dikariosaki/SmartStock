package com.example.smartstock.data.model

data class MovementStockRequestDto(
    val productoId: Int,
    val cantidad: Int,
    val usuarioId: Int,
    val tipo: String,
    val lote: String? = null
)

data class MovementDto(
    val movimientoId: Int?,
    val inventarioId: Int,
    val usuarioId: Int,
    val usuarioNombre: String?,
    val productoNombre: String?,
    val inventarioUbicacion: String?,
    val tipo: String,
    val cantidad: Int,
    val fechaMovimiento: String?,
    val lote: String?,
    val estado: Boolean
)
