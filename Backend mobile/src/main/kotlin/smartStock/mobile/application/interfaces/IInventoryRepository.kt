package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.Inventory
import java.util.Optional

interface IInventoryRepository {
    fun findAll(): List<Inventory>

    fun findById(id: Int): Optional<Inventory>

    fun findByProductId(productId: Int): Optional<Inventory>

    fun save(inventory: Inventory): Inventory

    fun deleteById(id: Int)
}
