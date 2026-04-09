package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.MovementResponse
import smartStock.mobile.application.services.MovementService

@RestController
@RequestMapping("/api/movimientos")
@Tag(name = "Movimientos", description = "Historial de movimientos de stock")
class MovementController(private val movementService: MovementService) {
    @GetMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Listar todos los movimientos")
    fun getAll(): ResponseEntity<List<MovementResponse>> {
        return ResponseEntity.ok(movementService.getAll())
    }

    @GetMapping("/inventario/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Listar movimientos por Inventario ID")
    fun getByInventory(
        @PathVariable id: Int,
    ): ResponseEntity<List<MovementResponse>> {
        return ResponseEntity.ok(movementService.getByInventoryId(id))
    }
}
