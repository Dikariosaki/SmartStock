package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.Inventory
import com.example.smartstock.domain.model.MovementStockRequest
import com.example.smartstock.domain.model.UpdateInventoryRequest
import com.example.smartstock.domain.repository.InventoryRepository
import javax.inject.Inject

class GetInventoriesUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(): List<Inventory> = inventoryRepository.getInventories()
}

class GetInventoryByIdUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(id: Int): Inventory = inventoryRepository.getInventoryById(id)
}

class UpdateInventoryUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(id: Int, request: UpdateInventoryRequest): Inventory = inventoryRepository.updateInventory(id, request)
}

class ActivateInventoryUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(id: Int) = inventoryRepository.activateInventory(id)
}

class DeactivateInventoryUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(id: Int) = inventoryRepository.deactivateInventory(id)
}

class RegisterInventoryEntryUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(request: MovementStockRequest) = inventoryRepository.registerEntry(request)
}

class RegisterInventoryExitUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(request: MovementStockRequest) = inventoryRepository.registerExit(request)
}

class RegisterInventoryDamageUseCase @Inject constructor(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(request: MovementStockRequest) = inventoryRepository.registerDamage(request)
}
