package com.example.smartstock.domain.repository

import com.example.smartstock.domain.model.Movement

interface MovementRepository {
    suspend fun getMovements(): List<Movement>
    suspend fun getMovementsByInventory(inventoryId: Int): List<Movement>
}
