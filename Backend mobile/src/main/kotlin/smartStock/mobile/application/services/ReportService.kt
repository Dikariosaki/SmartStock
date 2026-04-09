package smartStock.mobile.application.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import smartStock.mobile.application.dtos.ReportCreateRequest
import smartStock.mobile.application.dtos.ReportEvidenceDto
import smartStock.mobile.application.dtos.ReportResponse
import smartStock.mobile.application.dtos.ReportUpdateRequest
import smartStock.mobile.application.dtos.StoredReportEvidenceDto
import smartStock.mobile.application.interfaces.IReportRepository
import smartStock.mobile.domain.entities.Report
import smartStock.mobile.infrastructure.storage.ObjectStorageService
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.NoSuchElementException
import javax.imageio.ImageIO

@Service
class ReportService(
    private val reportRepository: IReportRepository,
    private val objectMapper: ObjectMapper,
    private val objectStorageService: ObjectStorageService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getAll(): List<ReportResponse> {
        return reportRepository.findAll().map { it.toResponse() }
    }

    fun getById(id: Int): ReportResponse? {
        return reportRepository.findById(id).map { it.toResponse() }.orElse(null)
    }

    fun create(request: ReportCreateRequest): ReportResponse {
        val report =
            Report(
                title = request.title,
                description = request.description,
                evidenceJson = null,
                type = request.type,
                status = true,
            )
        return reportRepository.save(report).toResponse()
    }

    fun update(
        id: Int,
        request: ReportUpdateRequest,
    ): ReportResponse {
        val existingReport = reportRepository.findById(id).orElseThrow { NoSuchElementException("Reporte no encontrado") }

        val updatedReport =
            existingReport.copy(
                title = request.title,
                description = request.description,
                type = request.type,
            )
        return reportRepository.save(updatedReport).toResponse()
    }

    fun updateEvidence(
        id: Int,
        observation: String?,
        retainedImageUrls: List<String>,
        newImages: List<MultipartFile>,
    ): ReportResponse {
        val existingReport = reportRepository.findById(id).orElseThrow { NoSuchElementException("Reporte no encontrado") }
        val existingEvidence = parseStoredEvidence(existingReport.evidenceJson)
        val currentImageReferences = existingEvidence?.allImageReferences().orEmpty()
        val normalizedObservation = observation?.trim()?.ifBlank { null }
        val resolvedRetainedReferences =
            retainedImageUrls
                .map(String::trim)
                .filter(String::isNotBlank)
                .mapNotNull { retainedUrl ->
                    resolveRetainedReference(
                        reportId = id,
                        retainedValue = retainedUrl,
                        currentImageReferences = currentImageReferences,
                    )
                }
                .distinct()

        if (resolvedRetainedReferences.size != retainedImageUrls.map(String::trim).filter(String::isNotBlank).distinct().size) {
            throw IllegalArgumentException("Una o varias imagenes retenidas no pertenecen al reporte.")
        }

        val safeNewImages = newImages.filterNot(MultipartFile::isEmpty)
        validateNewImages(safeNewImages)

        if (resolvedRetainedReferences.size + safeNewImages.size > MAX_EVIDENCE_IMAGES) {
            throw IllegalArgumentException("Solo se permiten hasta $MAX_EVIDENCE_IMAGES imagenes por reporte.")
        }

        val uploadedObjects = mutableListOf<ObjectStorageService.StoredObject>()

        return try {
            val retainedBlobKeys = resolvedRetainedReferences.mapNotNull(objectStorageService::extractObjectKey)
            val uploadedBlobKeys =
                safeNewImages.map { image ->
                    objectStorageService
                        .uploadReportImage(id, existingReport.type, image)
                        .also(uploadedObjects::add)
                        .objectKey
                }

            val finalBlobKeys = (retainedBlobKeys + uploadedBlobKeys).distinct()
            val finalEvidence =
                if (finalBlobKeys.isEmpty() && normalizedObservation == null) {
                    null
                } else {
                    StoredReportEvidenceDto(blobKeys = finalBlobKeys, observation = normalizedObservation)
                }

            val updatedReport =
                existingReport.copy(
                    evidenceJson = finalEvidence?.let(this::serializeEvidence),
                )

            val saved = reportRepository.save(updatedReport)
            val removedBlobKeys =
                currentImageReferences
                    .filterNot(resolvedRetainedReferences::contains)
                    .mapNotNull(objectStorageService::extractObjectKey)
                    .distinct()
            removedBlobKeys.forEach { objectKey ->
                runCatching { objectStorageService.deleteObject(objectKey) }
                    .onFailure { throwable ->
                        logger.warn("No se pudo eliminar el objeto removido {}", objectKey, throwable)
                    }
            }

            saved.toResponse()
        } catch (exception: Exception) {
            uploadedObjects.forEach { uploaded ->
                runCatching { objectStorageService.deleteObject(uploaded.objectKey) }
                    .onFailure { throwable ->
                        logger.warn("No se pudo limpiar el objeto recien subido {}", uploaded.objectKey, throwable)
                    }
            }
            throw exception
        }
    }

    fun activate(id: Int) {
        val existingReport = reportRepository.findById(id).orElseThrow { NoSuchElementException("Reporte no encontrado") }
        val updatedReport = existingReport.copy(status = true)
        reportRepository.save(updatedReport)
    }

    fun deactivate(id: Int) {
        val existingReport = reportRepository.findById(id).orElseThrow { NoSuchElementException("Reporte no encontrado") }
        val updatedReport = existingReport.copy(status = false)
        reportRepository.save(updatedReport)
    }

    private fun parseStoredEvidence(evidenceJson: String?): StoredReportEvidenceDto? {
        if (evidenceJson.isNullOrBlank()) {
            return null
        }

        return runCatching { objectMapper.readValue(evidenceJson, StoredReportEvidenceDto::class.java) }.getOrNull()
    }

    private fun serializeEvidence(evidence: StoredReportEvidenceDto): String = objectMapper.writeValueAsString(evidence)

    private fun validateNewImages(images: List<MultipartFile>) {
        if (images.any { !it.contentType.orEmpty().startsWith("image/") }) {
            throw IllegalArgumentException("Solo se permiten archivos de imagen.")
        }
    }

    fun getEvidenceImage(
        reportId: Int,
        imageIndex: Int,
    ): EvidenceImageContent? {
        val report = reportRepository.findById(reportId).orElse(null) ?: return null
        val storedEvidence = parseStoredEvidence(report.evidenceJson) ?: return null
        val reference = storedEvidence.allImageReferences().getOrNull(imageIndex) ?: return null

        return objectStorageService.getObject(reference)?.let { storedObject ->
            EvidenceImageContent(
                bytes = storedObject.bytes,
                contentType = storedObject.contentType,
            )
        } ?: buildPlaceholderImage(
            title = report.title,
            imageIndex = imageIndex,
        )
    }

    private fun toResponseEvidence(
        reportId: Int,
        evidenceJson: String?,
    ): ReportEvidenceDto? {
        val storedEvidence = parseStoredEvidence(evidenceJson) ?: return null
        val imageUrls =
            storedEvidence
                .allImageReferences()
                .indices
                .map { imageIndex -> buildEvidenceImagePath(reportId, imageIndex) }
        return ReportEvidenceDto(imageUrls = imageUrls, observation = storedEvidence.observation)
    }

    private fun Report.toResponse() =
        ReportResponse(
            id = id,
            title = title,
            description = description,
            evidence = id?.let { reportId -> toResponseEvidence(reportId, evidenceJson) },
            createdAt = createdAt ?: LocalDateTime.now(),
            type = type,
            status = status,
        )

    private fun resolveRetainedReference(
        reportId: Int,
        retainedValue: String,
        currentImageReferences: List<String>,
    ): String? {
        val normalizedValue = retainedValue.trim()
        if (normalizedValue.isBlank()) {
            return null
        }

        currentImageReferences.firstOrNull { reference ->
            reference == normalizedValue || objectStorageService.resolvePublicUrl(reference) == normalizedValue
        }?.let { return it }

        val imageIndex = extractProxyImageIndex(reportId, normalizedValue) ?: return null
        return currentImageReferences.getOrNull(imageIndex)
    }

    private fun extractProxyImageIndex(
        reportId: Int,
        value: String,
    ): Int? {
        val path = value.substringAfter("://", value).substringAfter('/', value)
        val pattern = Regex("/?api/reportes/$reportId/evidencia/imagenes/(\\d+)$", RegexOption.IGNORE_CASE)
        return pattern.find("/${path.trimStart('/')}")?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun buildEvidenceImagePath(
        reportId: Int,
        imageIndex: Int,
    ): String = "/api/reportes/$reportId/evidencia/imagenes/$imageIndex"

    private fun buildPlaceholderImage(
        title: String,
        imageIndex: Int,
    ): EvidenceImageContent {
        val image = BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = Color(15, 23, 42)
        graphics.fillRect(0, 0, image.width, image.height)
        graphics.color = Color(30, 41, 59)
        graphics.fillRoundRect(72, 72, image.width - 144, image.height - 144, 48, 48)
        graphics.color = Color(148, 163, 184)
        graphics.font = Font("SansSerif", Font.BOLD, 42)
        graphics.drawString("Evidencia no disponible", 120, 220)
        graphics.font = Font("SansSerif", Font.PLAIN, 30)
        graphics.drawString(title.take(48), 120, 300)
        graphics.drawString("Imagen ${imageIndex + 1}", 120, 352)
        graphics.drawString("El blob asociado no se encontro en RustFS.", 120, 404)
        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return EvidenceImageContent(
            bytes = outputStream.toByteArray(),
            contentType = "image/png",
        )
    }

    companion object {
        private const val MAX_EVIDENCE_IMAGES = 10
    }

    data class EvidenceImageContent(
        val bytes: ByteArray,
        val contentType: String,
    )
}
