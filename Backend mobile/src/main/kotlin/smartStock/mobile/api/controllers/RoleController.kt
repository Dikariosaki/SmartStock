package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.RoleResponse
import smartStock.mobile.application.services.RoleService

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Consulta de roles del sistema")
class RoleController(private val roleService: RoleService) {
    @GetMapping
    @PreAuthorize("hasRole('administrador')")
    @Operation(summary = "Listar roles", description = "Obtiene la lista de roles disponibles")
    fun getAll(): ResponseEntity<List<RoleResponse>> {
        return ResponseEntity.ok(roleService.getAll())
    }
}
