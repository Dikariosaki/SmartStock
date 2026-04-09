package smartStock.mobile.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import smartStock.mobile.domain.entities.Inventory
import java.util.Optional

@Repository
interface InventoryJpaRepository : JpaRepository<Inventory, Int> {
    fun findByProductId(productId: Int): Optional<Inventory>
}
