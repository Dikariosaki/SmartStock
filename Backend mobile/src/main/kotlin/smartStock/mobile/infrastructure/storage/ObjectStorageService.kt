package smartStock.mobile.infrastructure.storage

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URI
import java.text.Normalizer
import java.util.UUID

@Service
class ObjectStorageService(
    private val s3Client: S3Client,
    private val properties: StorageProperties,
) {
    fun uploadReportImage(
        reportId: Int,
        reportType: String,
        file: MultipartFile,
    ): StoredObject {
        val objectKey = buildReportObjectKey(reportId, reportType)
        val request =
            PutObjectRequest
                .builder()
                .bucket(properties.bucket)
                .key(objectKey)
                .contentType(file.contentType ?: "image/webp")
                .build()

        s3Client.putObject(request, RequestBody.fromBytes(file.bytes))

        return StoredObject(
            objectKey = objectKey,
            publicUrl = buildPublicUrl(objectKey),
        )
    }

    fun resolvePublicUrl(reference: String): String {
        val normalizedReference = reference.trim()
        if (normalizedReference.isBlank()) {
            return normalizedReference
        }

        val objectKey = extractObjectKey(normalizedReference)
        return when {
            objectKey != null -> buildPublicUrl(objectKey)
            normalizedReference.contains("://") -> normalizedReference
            else -> buildPublicUrl(normalizedReference.removePrefix("/"))
        }
    }

    fun deleteObject(objectKey: String) {
        s3Client.deleteObject(
            DeleteObjectRequest
                .builder()
                .bucket(properties.bucket)
                .key(objectKey)
                .build(),
        )
    }

    fun getObject(reference: String): StoredBinaryObject? {
        val objectKey = extractObjectKey(reference) ?: return null
        return try {
            val response =
                s3Client.getObjectAsBytes(
                    GetObjectRequest
                        .builder()
                        .bucket(properties.bucket)
                        .key(objectKey)
                        .build(),
                )

            StoredBinaryObject(
                objectKey = objectKey,
                bytes = response.asByteArray(),
                contentType = response.response().contentType()?.ifBlank { null } ?: inferContentType(objectKey),
            )
        } catch (_: NoSuchKeyException) {
            null
        } catch (exception: S3Exception) {
            if (exception.statusCode() == 404) {
                null
            } else {
                throw exception
            }
        }
    }

    fun extractObjectKey(reference: String): String? {
        val normalizedReference = reference.trim()
        if (normalizedReference.isBlank()) {
            return null
        }

        if (!normalizedReference.contains("://")) {
            return normalizedReference
                .removePrefix("/")
                .removePrefix("${properties.bucket}/")
                .ifBlank { null }
        }

        val path =
            runCatching { URI(normalizedReference).path.orEmpty() }
                .getOrDefault(normalizedReference)
        val bucketMarker = "/${properties.bucket}/"
        val bucketIndex = path.indexOf(bucketMarker)
        if (bucketIndex < 0) {
            return null
        }

        return path
            .substring(bucketIndex + bucketMarker.length)
            .removePrefix("/")
            .ifBlank { null }
    }

    private fun buildReportObjectKey(
        reportId: Int,
        reportType: String,
    ): String = "reportes/${normalizeTypeSegment(reportType)}/$reportId/${System.currentTimeMillis()}-${UUID.randomUUID()}.webp"

    private fun buildPublicUrl(objectKey: String): String =
        "${properties.publicBaseUrl.trimEnd('/')}/${properties.bucket}/$objectKey"

    private fun normalizeTypeSegment(type: String): String =
        Normalizer.normalize(type.trim(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .replace("[^a-z0-9]+".toRegex(), "-")
            .trim('-')
            .ifBlank { "general" }

    data class StoredObject(
        val objectKey: String,
        val publicUrl: String,
    )

    data class StoredBinaryObject(
        val objectKey: String,
        val bytes: ByteArray,
        val contentType: String,
    )

    private fun inferContentType(objectKey: String): String {
        val normalizedKey = objectKey.lowercase()
        return when {
            normalizedKey.endsWith(".png") -> "image/png"
            normalizedKey.endsWith(".jpg") || normalizedKey.endsWith(".jpeg") -> "image/jpeg"
            normalizedKey.endsWith(".gif") -> "image/gif"
            normalizedKey.endsWith(".svg") -> "image/svg+xml"
            else -> "image/webp"
        }
    }
}
