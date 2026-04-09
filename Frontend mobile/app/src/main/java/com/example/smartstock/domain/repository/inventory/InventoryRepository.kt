package com.example.smartstock.domain.repository

import com.example.smartstock.domain.model.Inventory
import com.example.smartstock.domain.model.MovementStockRequest
import com.example.smartstock.domain.model.UpdateInventoryRequest

interface InventoryRepository {
    suspend fun getInventories(): List<Inventory>
    suspend fun getInventoryById(id: Int): Inventory
    suspend fun updateInventory(id: Int, request: UpdateInventoryRequest): Inventory
    suspend fun activateInventory(id: Int)
    suspend fun deactivateInventory(id: Int)
    suspend fun registerEntry(request: MovementStockRequest)
    suspend fun registerExit(request: MovementStockRequest)
    suspend fun registerDamage(request: MovementStockRequest)
}
