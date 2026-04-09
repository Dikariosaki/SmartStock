package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.ProductCreateRequest
import smartStock.mobile.application.dtos.ProductResponse
import smartStock.mobile.application.dtos.ProductUpdateRequest
import smartStock.mobile.application.services.ProductService

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestion de productos")
class ProductController(private val productService: ProductService) {
    @GetMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Listar productos", description = "Obtiene todos los productos")
    fun getAll(): ResponseEntity<List<ProductResponse>> {
        return ResponseEntity.ok(productService.getAll())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Obtener producto por ID")
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<ProductResponse> {
        val product = productService.getById(id)
        return if (product != null) ResponseEntity.ok(product) else ResponseEntity.notFound().build()
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Crear producto")
    fun create(
        @RequestBody request: ProductCreateRequest,
    ): ResponseEntity<ProductResponse> {
        val created = productService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Actualizar producto")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: ProductUpdateRequest,
    ): ResponseEntity<ProductResponse> {
        return try {
            val updated = productService.update(id, request)
            ResponseEntity.ok(updated)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Activar producto")
    fun activate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            productService.activate(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('administrador','supervisor')")
    @Operation(summary = "Desactivar producto")
    fun deactivate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            productService.deactivate(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}
