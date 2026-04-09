package smartStock.mobile.application.services

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import smartStock.mobile.application.dtos.AuthLoginRequest
import smartStock.mobile.application.dtos.AuthLoginResponse
import smartStock.mobile.application.interfaces.IRoleRepository
import smartStock.mobile.application.interfaces.IUserRepository
import smartStock.mobile.infrastructure.security.JwtService

@Service
class AuthService(
    private val userRepository: IUserRepository,
    private val roleRepository: IRoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun login(request: AuthLoginRequest): AuthLoginResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val user =
            userRepository.findByEmail(normalizedEmail).orElseThrow {
                BadCredentialsException("Credenciales inválidas")
            }

        if (!user.status) {
            throw BadCredentialsException("Usuario inactivo")
        }

        val passwordHash = user.passwordHash
        val rawPassword = request.password

        val validPassword =
            if (passwordHash.startsWith("$2")) {
                passwordEncoder.matches(rawPassword, passwordHash)
            } else {
                val md5 = HashingUtils.md5Hex(rawPassword)
                md5.equals(passwordHash, ignoreCase = true)
            }

        if (!validPassword) {
            throw BadCredentialsException("Credenciales inválidas")
        }

        if (!passwordHash.startsWith("$2")) {
            val migratedHash =
                passwordEncoder.encode(rawPassword)
                    ?: throw BadCredentialsException("No se pudo proteger la contrasena")
            userRepository.save(user.copy(passwordHash = migratedHash))
        }

        val role = roleRepository.findById(user.roleId).orElseThrow {
            BadCredentialsException("Rol no válido para el usuario")
        }

        val userId = user.id ?: throw BadCredentialsException("Usuario inválido")
        val roleName =
            role.name
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: throw BadCredentialsException("Rol no válido para el usuario")
        val token =
            jwtService.generateToken(
                userId = userId,
                email = user.email,
                roleId = role.id ?: user.roleId,
                roleName = roleName,
                name = user.name,
            )

        return AuthLoginResponse(
            token = token,
            userId = userId,
            roleId = role.id ?: user.roleId,
            roleName = roleName,
            name = user.name,
            email = user.email,
        )
    }
}
