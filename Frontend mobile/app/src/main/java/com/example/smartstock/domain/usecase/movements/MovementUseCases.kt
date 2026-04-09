package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.Movement
import com.example.smartstock.domain.repository.MovementRepository
import javax.inject.Inject

class GetMovementsUseCase @Inject constructor(private val movementRepository: MovementRepository) {
    suspend operator fun invoke(): List<Movement> = movementRepository.getMovements()
}

class GetMovementsByInventoryUseCase @Inject constructor(private val movementRepository: MovementRepository) {
    suspend operator fun invoke(inventoryId: Int): List<Movement> = movementRepository.getMovementsByInventory(inventoryId)
}
