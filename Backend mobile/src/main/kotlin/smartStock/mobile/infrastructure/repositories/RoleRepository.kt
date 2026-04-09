package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.IRoleRepository
import smartStock.mobile.domain.entities.Role
import java.util.Optional

@Repository
class RoleRepository(private val jpaRepository: RoleJpaRepository) : IRoleRepository {
    override fun findAll(): List<Role> = jpaRepository.findAll()

    override fun findById(id: Int): Optional<Role> = jpaRepository.findById(id)

    override fun findByName(name: String): Optional<Role> = jpaRepository.findByName(name)
}
