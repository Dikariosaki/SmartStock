package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.Role
import java.util.Optional

interface IRoleRepository {
    fun findAll(): List<Role>

    fun findById(id: Int): Optional<Role>

    fun findByName(name: String): Optional<Role>
}
