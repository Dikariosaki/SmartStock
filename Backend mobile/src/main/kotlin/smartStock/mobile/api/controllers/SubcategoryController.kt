package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smartStock.mobile.application.dtos.SubcategoryResponse
import smartStock.mobile.application.services.SubcategoryService

@RestController
@RequestMapping("/api/subcategorias")
@Tag(name = "Subcategorias", description = "Consulta de subcategorias para formularios")
class SubcategoryController(private val subcategoryService: SubcategoryService) {
    @GetMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Listar subcategorias", description = "Obtiene la lista de subcategorias disponibles")
    fun getAll(): ResponseEntity<List<SubcategoryResponse>> {
        return ResponseEntity.ok(subcategoryService.getAll())
    }
}
