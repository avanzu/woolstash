package de.avanzu.woolstash.data.media

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import de.avanzu.woolstash.domain.model.InventoryItemId
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

class InventoryPhotoImporter(
    private val contentResolver: ContentResolver,
    private val mediaStore: InventoryMediaStore,
) {
    suspend fun importPhoto(
        inventoryItemId: InventoryItemId,
        sourceUri: Uri,
    ): InventoryPhotoFile {
        return withContext(Dispatchers.IO) {
            val photoDirectory = mediaStore.createPhotoSlot(inventoryItemId)

            try {
                val sourceFile = photoDirectory.resolve(TEMP_SOURCE_FILE_NAME)
                copySourceToFile(
                    sourceUri = sourceUri,
                    targetFile = sourceFile,
                )
                val sourceBitmap = decodeBitmap(sourceFile)
                val orientedBitmap = sourceBitmap.rotateToExifOrientation(sourceFile)
                val displayBitmap = orientedBitmap
                val thumbnailBitmap = orientedBitmap.scaleDownToMaxDimension(THUMBNAIL_MAX_DIMENSION)

                displayBitmap.writeWebp(
                    file = photoDirectory.resolve(InventoryMediaStore.DISPLAY_FILE_NAME),
                    quality = DISPLAY_WEBP_QUALITY,
                )
                thumbnailBitmap.writeWebp(
                    file = photoDirectory.resolve(InventoryMediaStore.THUMBNAIL_FILE_NAME),
                    quality = THUMBNAIL_WEBP_QUALITY,
                )

                if (sourceBitmap !== orientedBitmap) {
                    sourceBitmap.recycle()
                }
                if (orientedBitmap !== displayBitmap && orientedBitmap !== thumbnailBitmap) {
                    orientedBitmap.recycle()
                }
                if (displayBitmap !== thumbnailBitmap) {
                    displayBitmap.recycle()
                }
                thumbnailBitmap.recycle()
                sourceFile.delete()

                mediaStore.listPhotos(inventoryItemId)
                    .firstOrNull { photo -> photo.displayFile.parentFile == photoDirectory }
                    ?: error("Imported inventory photo could not be listed: ${photoDirectory.absolutePath}")
            } catch (error: Throwable) {
                photoDirectory.deleteRecursively()
                throw error
            }
        }
    }

    private fun copySourceToFile(
        sourceUri: Uri,
        targetFile: File,
    ) {
        openSourceStream(sourceUri).use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun openSourceStream(sourceUri: Uri): InputStream {
        contentResolver.openInputStream(sourceUri)?.let { input ->
            return input
        }

        contentResolver.openFileDescriptor(sourceUri, "r")?.let { descriptor ->
            return descriptor.use { fileDescriptor ->
                FileInputStream(fileDescriptor.fileDescriptor).readBytes().inputStream()
            }
        }

        contentResolver.openTypedAssetFileDescriptor(sourceUri, "image/*", null)?.let { descriptor ->
            return descriptor.use { assetDescriptor ->
                assetDescriptor.createInputStream().readBytes().inputStream()
            }
        }

        throw IOException(
            "Could not open image stream for uri=$sourceUri, " +
                "scheme=${sourceUri.scheme}, authority=${sourceUri.authority}.",
        )
    }

    private fun decodeBitmap(sourceFile: File): Bitmap {
        return BitmapFactory.decodeFile(sourceFile.absolutePath)
            ?: throw IOException("Could not decode image: ${sourceFile.absolutePath}")
    }

    private fun Bitmap.rotateToExifOrientation(sourceFile: File): Bitmap {
        val orientation = try {
            ExifInterface(sourceFile.absolutePath).getAttributeInt(
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
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(-1f, 1f)
            }
            else -> return this
        }

        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    companion object {
        private const val THUMBNAIL_MAX_DIMENSION = 320
        private const val DISPLAY_WEBP_QUALITY = 78
        private const val THUMBNAIL_WEBP_QUALITY = 75
        private const val TEMP_SOURCE_FILE_NAME = "source.tmp"
    }
}

private fun Bitmap.scaleDownToMaxDimension(maxDimension: Int): Bitmap {
    val longestEdge = max(width, height)
    if (longestEdge <= maxDimension) {
        return this
    }

    val scale = maxDimension.toFloat() / longestEdge.toFloat()
    val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}

@Suppress("DEPRECATION")
private fun Bitmap.writeWebp(
    file: File,
    quality: Int,
) {
    file.outputStream().use { output ->
        if (!compress(Bitmap.CompressFormat.WEBP, quality, output)) {
            throw IOException("Could not write image file: ${file.absolutePath}")
        }
    }
}
