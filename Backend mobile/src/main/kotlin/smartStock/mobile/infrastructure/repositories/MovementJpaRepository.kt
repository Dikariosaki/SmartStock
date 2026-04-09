package smartStock.mobile.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import smartStock.mobile.domain.entities.Movement

@Repository
interface MovementJpaRepository : JpaRepository<Movement, Int> {
    fun findByInventoryId(inventoryId: Int): List<Movement>
}
