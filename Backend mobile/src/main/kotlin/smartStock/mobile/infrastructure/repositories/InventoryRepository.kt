package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.IInventoryRepository
import smartStock.mobile.domain.entities.Inventory
import java.util.Optional

@Repository
class InventoryRepository(private val jpaRepository: InventoryJpaRepository) : IInventoryRepository {
    override fun findAll(): List<Inventory> = jpaRepository.findAll()

    override fun findById(id: Int): Optional<Inventory> = jpaRepository.findById(id)

    override fun findByProductId(productId: Int): Optional<Inventory> = jpaRepository.findByProductId(productId)

    override fun save(inventory: Inventory): Inventory = jpaRepository.save(inventory)

    override fun deleteById(id: Int) = jpaRepository.deleteById(id)
}
