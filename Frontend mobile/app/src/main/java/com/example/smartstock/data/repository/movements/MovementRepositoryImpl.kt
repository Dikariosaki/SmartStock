package com.example.smartstock.data.repository

import com.example.smartstock.data.api.MovementApiService
import com.example.smartstock.data.model.toDomain
import com.example.smartstock.domain.model.Movement
import com.example.smartstock.domain.repository.MovementRepository
import javax.inject.Inject

class MovementRepositoryImpl
    @Inject
    constructor(
        private val movementApiService: MovementApiService
    ) : MovementRepository {
        override suspend fun getMovements(): List<Movement> = movementApiService.getMovements().map { it.toDomain() }

        override suspend fun getMovementsByInventory(inventoryId: Int): List<Movement> =
            movementApiService.getMovementsByInventory(inventoryId).map { it.toDomain() }
    }
