package com.example.smartstock.core.report

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

object ReportImageCompressor {
    private const val MAX_BYTES = 300 * 1024L
    private val DIMENSION_STEPS = listOf(1600, 1280, 1080)
    private val QUALITY_STEPS = listOf(88, 82, 78, 74)

    sealed interface CompressionResult {
        data class Success(val file: File) : CompressionResult

        data class Failure(val message: String) : CompressionResult
    }

    fun compressToWebp(
        context: Context,
        inputFile: File,
    ): CompressionResult {
        val outputDirectory = File(context.cacheDir, "report-evidence").apply { mkdirs() }
        var orientedBitmap: Bitmap? = null

        return try {
            orientedBitmap = decodeOrientedBitmap(inputFile)
                ?: return CompressionResult.Failure("No se pudo leer la foto capturada.")

            for (maxDimension in DIMENSION_STEPS) {
                val candidateBitmap = scaleBitmap(orientedBitmap, maxDimension)
                try {
                    for (quality in QUALITY_STEPS) {
                        val candidateFile = File(outputDirectory, "report-${UUID.randomUUID()}.webp")
                        if (!writeWebp(candidateBitmap, candidateFile, quality)) {
                            candidateFile.delete()
                            continue
                        }

                        if (candidateFile.length() <= MAX_BYTES) {
                            return CompressionResult.Success(candidateFile)
                        }

                        candidateFile.delete()
                    }
                } finally {
                    if (candidateBitmap !== orientedBitmap && !candidateBitmap.isRecycled) {
                        candidateBitmap.recycle()
                    }
                }
            }

            CompressionResult.Failure(
                "La imagen sigue superando 300 KB despues de la compresion. Toma otra foto con menos detalle.",
            )
        } catch (_: Exception) {
            CompressionResult.Failure("No se pudo optimizar la imagen de evidencia.")
        } finally {
            if (orientedBitmap != null && !orientedBitmap.isRecycled) {
                orientedBitmap.recycle()
            }
            inputFile.delete()
        }
    }

    private fun decodeOrientedBitmap(file: File): Bitmap? {
        val bounds =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
        BitmapFactory.decodeFile(file.absolutePath, bounds)

        val options =
            BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, DIMENSION_STEPS.first())
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

        val decodedBitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
        return applyExifOrientation(file, decodedBitmap)
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        requestedLongSide: Int,
    ): Int {
        var sampleSize = 1
        var currentWidth = width
        var currentHeight = height

        while (max(currentWidth, currentHeight) > requestedLongSide * 2) {
            currentWidth /= 2
            currentHeight /= 2
            sampleSize *= 2
        }

        return sampleSize.coerceAtLeast(1)
    }

    private fun applyExifOrientation(
        file: File,
        bitmap: Bitmap,
    ): Bitmap {
        val orientation =
            try {
                ExifInterface(file).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } catch (_: IOException) {
                ExifInterface.ORIENTATION_NORMAL
            }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }

        val rotatedBitmap =
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true,
            )

        if (rotatedBitmap != bitmap && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        return rotatedBitmap
    }

    private fun scaleBitmap(
        bitmap: Bitmap,
        maxDimension: Int,
    ): Bitmap {
        val longestSide = max(bitmap.width, bitmap.height)
        if (longestSide <= maxDimension) {
            return bitmap
        }

        val scale = maxDimension / longestSide.toFloat()
        val scaledWidth = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val scaledHeight = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }

    private fun writeWebp(
        bitmap: Bitmap,
        outputFile: File,
        quality: Int,
    ): Boolean =
        FileOutputStream(outputFile).use { stream ->
            val format =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            bitmap.compress(format, quality, stream)
        }
}
