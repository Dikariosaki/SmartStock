package com.example.smartstock.data.repository

import com.example.smartstock.data.api.InventoryApiService
import com.example.smartstock.data.model.toDomain
import com.example.smartstock.data.model.toDto
import com.example.smartstock.domain.model.Inventory
import com.example.smartstock.domain.model.MovementStockRequest
import com.example.smartstock.domain.model.UpdateInventoryRequest
import com.example.smartstock.domain.repository.InventoryRepository
import javax.inject.Inject

class InventoryRepositoryImpl
    @Inject
    constructor(
        private val inventoryApiService: InventoryApiService
    ) : InventoryRepository {
        override suspend fun getInventories(): List<Inventory> = inventoryApiService.getInventories().map { it.toDomain() }

        override suspend fun getInventoryById(id: Int): Inventory = inventoryApiService.getInventoryById(id).toDomain()

        override suspend fun updateInventory(
            id: Int,
            request: UpdateInventoryRequest
        ): Inventory = inventoryApiService.updateInventory(id, request.toDto()).toDomain()

        override suspend fun activateInventory(id: Int) {
            inventoryApiService.activateInventory(id)
        }

        override suspend fun deactivateInventory(id: Int) {
            inventoryApiService.deactivateInventory(id)
        }

        override suspend fun registerEntry(request: MovementStockRequest) {
            inventoryApiService.registerEntry(request.toDto())
        }

        override suspend fun registerExit(request: MovementStockRequest) {
            inventoryApiService.registerExit(request.toDto())
        }

        override suspend fun registerDamage(request: MovementStockRequest) {
            inventoryApiService.registerDamage(request.toDto())
        }
    }
