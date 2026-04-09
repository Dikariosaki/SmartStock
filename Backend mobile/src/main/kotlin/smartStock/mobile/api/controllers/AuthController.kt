package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.AuthLoginRequest
import smartStock.mobile.application.dtos.AuthLoginResponse
import smartStock.mobile.application.services.AuthService

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Operaciones para inicio y gestión de sesión")
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica usuario y retorna JWT")
    fun login(
        @Valid @RequestBody request: AuthLoginRequest,
    ): ResponseEntity<Any> {
        return try {
            val response: AuthLoginResponse = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf("message" to (e.message ?: "Credenciales inválidas")),
            )
        }
    }
}
