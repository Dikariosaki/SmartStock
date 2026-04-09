package smartStock.mobile.application.services

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import smartStock.mobile.application.dtos.UserCreateRequest
import smartStock.mobile.application.dtos.UserResponse
import smartStock.mobile.application.dtos.UserUpdateRequest
import smartStock.mobile.application.interfaces.IRoleRepository
import smartStock.mobile.application.interfaces.IUserRepository
import smartStock.mobile.application.interfaces.IUserService
import smartStock.mobile.domain.entities.User
import java.util.Optional

@Service
class UserService(
    private val userRepository: IUserRepository,
    private val roleRepository: IRoleRepository,
    private val passwordEncoder: PasswordEncoder,
) : IUserService {
    override fun getAll(): List<UserResponse> {
        val users = userRepository.findAll()
        val rolesById =
            roleRepository.findAll()
                .associateBy({ it.id }, { it.name })
        return users.map { user -> user.toResponse(rolesById[user.roleId]) }
    }

    override fun getById(id: Int): Optional<UserResponse> {
        val rolesById =
            roleRepository.findAll()
                .associateBy({ it.id }, { it.name })
        return userRepository.findById(id).map { user ->
            user.toResponse(rolesById[user.roleId])
        }
    }

    override fun create(request: UserCreateRequest): UserResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val existingEmail = userRepository.findByEmail(normalizedEmail)
        if (existingEmail.isPresent) {
            throw RuntimeException("El email ya está en uso")
        }

        val role =
            roleRepository.findById(request.roleId).orElseThrow {
                RuntimeException("Rol no encontrado con id ${request.roleId}")
            }
        val roleName =
            role.name
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: throw RuntimeException("Rol no valido con id ${request.roleId}")
        val isInternalRole = roleName in setOf("administrador", "supervisor", "auxiliar")

        val rawPassword = request.password?.trim()
        if (isInternalRole && rawPassword.isNullOrBlank()) {
            throw RuntimeException("La contraseña es obligatoria para usuarios internos")
        }

        val user =
            User(
                roleId = request.roleId,
                name = request.name,
                cedula = request.cedula,
                email = normalizedEmail,
                passwordHash =
                    if (rawPassword.isNullOrBlank()) {
                        ""
                    } else {
                        passwordEncoder.encode(rawPassword)
                            ?: throw RuntimeException("No se pudo proteger la contrasena")
                    },
                status = request.status,
                phone = request.phone,
            )
        val savedUser = userRepository.save(user)
        return savedUser.toResponse(role.name)
    }

    override fun update(
        id: Int,
        request: UserUpdateRequest,
    ): UserResponse {
        val userOptional = userRepository.findById(id)
        if (!userOptional.isPresent) {
            throw RuntimeException("Usuario no encontrado con id $id")
        }

        val role =
            roleRepository.findById(request.roleId).orElseThrow {
                RuntimeException("Rol no encontrado con id ${request.roleId}")
            }
        val roleName =
            role.name
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: throw RuntimeException("Rol no valido con id ${request.roleId}")
        val isInternalRole = roleName in setOf("administrador", "supervisor", "auxiliar")

        val normalizedEmail = request.email.trim().lowercase()
        val existingEmail = userRepository.findByEmail(normalizedEmail)
        if (existingEmail.isPresent && existingEmail.get().id != id) {
            throw RuntimeException("El email ya está en uso")
        }

        val existingUser = userOptional.get()
        val incomingPassword = request.password?.trim()
        val existingPasswordHash = existingUser.passwordHash ?: ""
        val passwordHash: String =
            if (incomingPassword.isNullOrBlank()) {
                existingPasswordHash
            } else {
                passwordEncoder.encode(incomingPassword)
                    ?: throw RuntimeException("No se pudo proteger la contrasena")
            }

        if (isInternalRole && passwordHash.isBlank()) {
            throw RuntimeException("La contraseña es obligatoria para usuarios internos")
        }

        val updatedUser =
            existingUser.copy(
                roleId = request.roleId,
                name = request.name,
                cedula = request.cedula,
                email = normalizedEmail,
                passwordHash = passwordHash,
                status = request.status,
                phone = request.phone,
            )
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toResponse(role.name)
    }

    override fun delete(id: Int) {
        val userOptional = userRepository.findById(id)
        if (!userOptional.isPresent) {
            throw RuntimeException("Usuario no encontrado con id $id")
        }
        userRepository.deleteById(id)
    }

    override fun activate(id: Int) {
        val userOptional = userRepository.findById(id)
        if (userOptional.isPresent) {
            val user = userOptional.get()
            val updatedUser = user.copy(status = true)
            userRepository.save(updatedUser)
        } else {
            throw RuntimeException("Usuario no encontrado con id $id")
        }
    }

    override fun deactivate(id: Int) {
        val userOptional = userRepository.findById(id)
        if (userOptional.isPresent) {
            val user = userOptional.get()
            val updatedUser = user.copy(status = false)
            userRepository.save(updatedUser)
        } else {
            throw RuntimeException("Usuario no encontrado con id $id")
        }
    }

    private fun User.toResponse(roleName: String?) =
        UserResponse(
            id = id,
            roleId = roleId,
            roleName = roleName,
            name = name,
            cedula = cedula,
            email = email,
            status = status,
            phone = phone,
        )
}
