package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.User
import java.util.Optional

interface IUserRepository {
    fun findAll(): List<User>

    fun findById(id: Int): Optional<User>

    fun findByEmail(email: String): Optional<User>

    fun save(user: User): User

    fun deleteById(id: Int)
}
