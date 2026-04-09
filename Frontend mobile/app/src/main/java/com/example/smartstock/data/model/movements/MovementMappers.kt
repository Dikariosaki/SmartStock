package com.example.smartstock.data.model

import com.example.smartstock.domain.model.Movement
import com.example.smartstock.domain.model.MovementStockRequest

fun MovementDto.toDomain(): Movement =
    Movement(
        id = movimientoId ?: 0,
        inventoryId = inventarioId,
        userId = usuarioId,
        userName = usuarioNombre,
        productName = productoNombre,
        inventoryLocation = inventarioUbicacion,
        type = tipo,
        quantity = cantidad,
        date = fechaMovimiento,
        batch = lote,
        status = estado
    )

fun MovementStockRequest.toDto(): MovementStockRequestDto =
    MovementStockRequestDto(
        productoId = productId,
        cantidad = quantity,
        usuarioId = userId,
        tipo = type,
        lote = batch
    )
