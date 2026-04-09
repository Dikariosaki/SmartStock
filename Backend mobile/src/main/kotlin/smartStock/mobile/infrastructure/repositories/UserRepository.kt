package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.IUserRepository
import smartStock.mobile.domain.entities.User
import java.util.Optional

@Repository
class UserRepository(private val jpaRepository: UserJpaRepository) : IUserRepository {
    override fun findAll(): List<User> {
        return jpaRepository.findAll()
    }

    override fun findById(id: Int): Optional<User> {
        return jpaRepository.findById(id)
    }

    override fun findByEmail(email: String): Optional<User> {
        return jpaRepository.findByEmailIgnoreCase(email)
    }

    override fun save(user: User): User {
        return jpaRepository.save(user)
    }

    override fun deleteById(id: Int) {
        jpaRepository.deleteById(id)
    }
}
