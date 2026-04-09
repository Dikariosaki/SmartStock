package smartStock.mobile.application.services

import org.springframework.stereotype.Service
import smartStock.mobile.application.dtos.RoleResponse
import smartStock.mobile.application.interfaces.IRoleRepository

@Service
class RoleService(private val roleRepository: IRoleRepository) {
    fun getAll(): List<RoleResponse> {
        return roleRepository.findAll()
            .mapNotNull { role ->
                role.id?.let { roleId ->
                    RoleResponse(
                        id = roleId,
                        name = role.name,
                    )
                }
            }
            .sortedBy { it.id }
    }
}
