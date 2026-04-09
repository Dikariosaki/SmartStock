package smartStock.mobile.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import smartStock.mobile.domain.entities.User

@Repository
interface UserJpaRepository : JpaRepository<User, Int> {
    fun findByEmailIgnoreCase(email: String): java.util.Optional<User>
}
