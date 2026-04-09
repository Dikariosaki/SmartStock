package com.example.smartstock.data.model

import com.example.smartstock.domain.model.Inventory
import com.example.smartstock.domain.model.UpdateInventoryRequest

fun InventoryDto.toDomain(): Inventory =
    Inventory(
        id = inventarioId ?: 0,
        productId = productoId,
        productName = productoNombre,
        location = ubicacion,
        quantity = cantidad,
        reorderPoint = puntoReorden,
        status = estado
    )

fun UpdateInventoryRequest.toDto(): InventoryUpdateRequestDto =
    InventoryUpdateRequestDto(
        ubicacion = location,
        puntoReorden = reorderPoint
    )
