package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.Movement
import java.util.Optional

interface IMovementRepository {
    fun findAll(): List<Movement>

    fun findById(id: Int): Optional<Movement>

    fun findByInventoryId(inventoryId: Int): List<Movement>

    fun save(movement: Movement): Movement

    fun deleteById(id: Int)
}
