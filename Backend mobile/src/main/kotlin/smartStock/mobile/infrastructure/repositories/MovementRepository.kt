package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.IMovementRepository
import smartStock.mobile.domain.entities.Movement
import java.util.Optional

@Repository
class MovementRepository(private val jpaRepository: MovementJpaRepository) : IMovementRepository {
    override fun findAll(): List<Movement> = jpaRepository.findAll()

    override fun findById(id: Int): Optional<Movement> = jpaRepository.findById(id)

    override fun findByInventoryId(inventoryId: Int): List<Movement> = jpaRepository.findByInventoryId(inventoryId)

    override fun save(movement: Movement): Movement = jpaRepository.save(movement)

    override fun deleteById(id: Int) = jpaRepository.deleteById(id)
}
