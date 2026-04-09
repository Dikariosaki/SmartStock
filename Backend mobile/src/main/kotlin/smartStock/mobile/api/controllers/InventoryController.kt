package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.InventoryResponse
import smartStock.mobile.application.dtos.InventoryUpdateRequest
import smartStock.mobile.application.dtos.MovementStockRequest
import smartStock.mobile.application.services.InventoryService

@RestController
@RequestMapping("/api/inventarios")
@Tag(name = "Inventarios", description = "Gestion de inventario y registro de entradas/salidas/averias")
class InventoryController(private val inventoryService: InventoryService) {
    @GetMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Listar inventarios")
    fun getAll(): ResponseEntity<List<InventoryResponse>> {
        return ResponseEntity.ok(inventoryService.getAll())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Obtener inventario por ID")
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<InventoryResponse> {
        val inventory = inventoryService.getById(id)
        return if (inventory != null) ResponseEntity.ok(inventory) else ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Actualizar inventario (ubicacion, punto reorden)")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: InventoryUpdateRequest,
    ): ResponseEntity<InventoryResponse> {
        return try {
            val updated = inventoryService.update(id, request)
            ResponseEntity.ok(updated)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Activar inventario")
    fun activate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            inventoryService.activate(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Desactivar inventario")
    fun deactivate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            inventoryService.deactivate(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Registrar Entrada", description = "Aumenta el stock y genera un movimiento de entrada")
    fun registerEntry(
        @RequestBody request: MovementStockRequest,
    ): ResponseEntity<Map<String, Any>> {
        val safeRequest = request.copy(type = "ENTRADA")
        val newQuantity = inventoryService.registerEntry(safeRequest)
        return ResponseEntity.ok(mapOf("message" to "Entrada registrada", "newQuantity" to newQuantity))
    }

    @PostMapping("/salida")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Registrar Salida", description = "Disminuye el stock y genera un movimiento de salida")
    fun registerExit(
        @RequestBody request: MovementStockRequest,
    ): ResponseEntity<Map<String, Any>> {
        val safeRequest = request.copy(type = "SALIDA")
        return try {
            val newQuantity = inventoryService.registerExit(safeRequest)
            ResponseEntity.ok(mapOf("message" to "Salida registrada", "newQuantity" to newQuantity))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Error desconocido")))
        }
    }

    @PostMapping("/averia")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Registrar Averia", description = "Disminuye el stock y genera un movimiento de averia")
    fun registerDamage(
        @RequestBody request: MovementStockRequest,
    ): ResponseEntity<Map<String, Any>> {
        val safeRequest = request.copy(type = "AVERIA")
        return try {
            val newQuantity = inventoryService.registerExit(safeRequest)
            ResponseEntity.ok(mapOf("message" to "Averia registrada", "newQuantity" to newQuantity))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Error desconocido")))
        }
    }
}
