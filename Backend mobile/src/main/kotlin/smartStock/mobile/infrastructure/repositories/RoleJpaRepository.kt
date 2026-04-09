package smartStock.mobile.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import smartStock.mobile.domain.entities.Role
import java.util.Optional

@Repository
interface RoleJpaRepository : JpaRepository<Role, Int> {
    fun findByName(name: String): Optional<Role>
}
