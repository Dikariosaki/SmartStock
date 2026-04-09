package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.TaskCreateRequest
import smartStock.mobile.application.dtos.TaskResponse
import smartStock.mobile.application.dtos.TaskUpdateRequest
import smartStock.mobile.application.services.TaskService

@RestController
@RequestMapping("/api/tareas")
@Tag(name = "Tareas", description = "Gestion de tareas")
class TaskController(private val taskService: TaskService) {
    @GetMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Listar todas las tareas")
    fun getAll(): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskService.getAll())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Obtener tarea por ID")
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<TaskResponse> {
        val task = taskService.getById(id)
        return if (task != null) ResponseEntity.ok(task) else ResponseEntity.notFound().build()
    }

    @GetMapping("/usuario/{userId}")
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Listar tareas asignadas a un usuario")
    fun getByUserId(
        @PathVariable userId: Int,
    ): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskService.getByUserId(userId))
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Crear nueva tarea")
    fun create(
        @RequestBody request: TaskCreateRequest,
    ): ResponseEntity<TaskResponse> {
        val created = taskService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Actualizar tarea")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: TaskUpdateRequest,
    ): ResponseEntity<TaskResponse> {
        return try {
            val updated = taskService.update(id, request)
            ResponseEntity.ok(updated)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Activar tarea (Pendiente)")
    fun activate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            taskService.activate(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Desactivar tarea (Completada)")
    fun deactivate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            taskService.deactivate(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('administrador')")
    @Operation(summary = "Eliminar tarea")
    fun delete(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            taskService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}
