package smartStock.mobile.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import smartStock.mobile.application.dtos.ReportCreateRequest
import smartStock.mobile.application.dtos.ReportResponse
import smartStock.mobile.application.dtos.ReportUpdateRequest
import smartStock.mobile.application.services.ReportService
import java.util.NoSuchElementException

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Gestion de reportes")
class ReportController(private val reportService: ReportService) {
    @GetMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Listar todos los reportes")
    fun getAll(): ResponseEntity<List<ReportResponse>> {
        return ResponseEntity.ok(reportService.getAll())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Obtener reporte por ID")
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<ReportResponse> {
        val report = reportService.getById(id)
        return if (report != null) ResponseEntity.ok(report) else ResponseEntity.notFound().build()
    }

    @GetMapping("/{id}/evidencia/imagenes/{imageIndex}")
    @Operation(summary = "Obtener una imagen de evidencia de un reporte")
    fun getEvidenceImage(
        @PathVariable id: Int,
        @PathVariable imageIndex: Int,
    ): ResponseEntity<ByteArray> {
        val image = reportService.getEvidenceImage(id, imageIndex) ?: return ResponseEntity.notFound().build()
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(image.contentType))
            .body(image.bytes)
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Crear nuevo reporte")
    fun create(
        @RequestBody request: ReportCreateRequest,
    ): ResponseEntity<ReportResponse> {
        val created = reportService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Actualizar reporte")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: ReportUpdateRequest,
    ): ResponseEntity<ReportResponse> {
        return try {
            val updated = reportService.update(id, request)
            ResponseEntity.ok(updated)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/evidencia", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAnyRole('administrador','supervisor','auxiliar')")
    @Operation(summary = "Actualizar evidencia de un reporte")
    fun updateEvidence(
        @PathVariable id: Int,
        @RequestParam(required = false) observation: String?,
        @RequestParam(name = "retainedImageUrls", required = false) retainedImageUrls: List<String>?,
        @RequestPart(name = "newImages", required = false) newImages: List<MultipartFile>?,
    ): ResponseEntity<ReportResponse> {
        return try {
            val updated =
                reportService.updateEvidence(
                    id = id,
                    observation = observation,
                    retainedImageUrls = retainedImageUrls.orEmpty(),
                    newImages = newImages.orEmpty(),
                )
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('administrador')")
    @Operation(summary = "Activar reporte")
    fun activate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            reportService.activate(id)
            ResponseEntity.ok().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('administrador')")
    @Operation(summary = "Desactivar reporte")
    fun deactivate(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        return try {
            reportService.deactivate(id)
            ResponseEntity.ok().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}
