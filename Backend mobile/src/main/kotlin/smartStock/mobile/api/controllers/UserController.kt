package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.UserCreateRequest
import smartStock.mobile.application.dtos.UserResponse
import smartStock.mobile.application.dtos.UserUpdateRequest
import smartStock.mobile.application.interfaces.IUserService

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios")
class UserController(private val userService: IUserService) {
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un nuevo usuario en el sistema")
    fun create(
        @Valid @RequestBody request: UserCreateRequest,
    ): ResponseEntity<UserResponse> {
        val createdUser = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista de todos los usuarios registrados")
    fun getAll(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAll()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico basado en su ID")
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<UserResponse> {
        val userOptional = userService.getById(id)
        return if (userOptional.isPresent) {
            ResponseEntity.ok(userOptional.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario")
    fun update(
        @PathVariable id: Int,
        @Valid @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<UserResponse> {
        return try {
            val updatedUser = userService.update(id, request)
            ResponseEntity.ok(updatedUser)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por ID")
    fun delete(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            userService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activar usuario", description = "Cambia el estado del usuario a activo (true)")
    fun activate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            userService.activate(id)
            ResponseEntity.ok().build()
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar usuario", description = "Cambia el estado del usuario a inactivo (false)")
    fun deactivate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            userService.deactivate(id)
            ResponseEntity.ok().build()
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }
}
