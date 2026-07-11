package de.avanzu.woolstash.data.media

import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.PhotoId
import java.io.File
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryMediaStore(
    filesDir: File,
    private val clock: Clock = Clock.systemUTC(),
    private val photoIdFactory: () -> PhotoId = { PhotoId.new() },
) {
    val mediaRoot: File = filesDir.resolve(MEDIA_ROOT)

    fun inventoryMediaDirectory(): File {
        return mediaRoot.resolve(INVENTORY_DIRECTORY)
    }

    fun inventoryItemMediaDirectory(inventoryItemId: InventoryItemId): File {
        return inventoryMediaDirectory().resolve(inventoryItemId.value)
    }

    suspend fun listPhotos(inventoryItemId: InventoryItemId): List<InventoryPhotoFile> {
        return withContext(Dispatchers.IO) {
            val itemDirectory = inventoryItemMediaDirectory(inventoryItemId)
            val photoDirectories = itemDirectory.listFiles()
                ?.filter { file -> file.isDirectory }
                .orEmpty()

            photoDirectories
                .sortedBy { directory -> directory.name }
                .mapNotNull { directory ->
                    val parsedName = InventoryMediaFolderNames.parsePhotoFolderName(directory.name)
                        ?: return@mapNotNull null

                    val displayFile = directory.resolve(DISPLAY_FILE_NAME)
                    val thumbnailFile = directory.resolve(THUMBNAIL_FILE_NAME)
                    if (!displayFile.isFile || !thumbnailFile.isFile) {
                        return@mapNotNull null
                    }

                    InventoryPhotoFile(
                        photoId = parsedName.photoId,
                        inventoryItemId = inventoryItemId,
                        assignedAt = parsedName.assignedAt,
                        displayFile = displayFile,
                        thumbnailFile = thumbnailFile,
                    )
                }
        }
    }

    suspend fun createPhotoSlot(
        inventoryItemId: InventoryItemId,
        assignedAt: Instant = clock.instant(),
        photoId: PhotoId = photoIdFactory(),
    ): File {
        return withContext(Dispatchers.IO) {
            val photoDirectoryName = InventoryMediaFolderNames.createPhotoFolderName(
                assignedAt = assignedAt,
                photoId = photoId,
            )
            val photoDirectory = inventoryItemMediaDirectory(inventoryItemId)
                .resolve(photoDirectoryName)

            if (!photoDirectory.exists() && !photoDirectory.mkdirs()) {
                error("Could not create inventory photo directory: ${photoDirectory.absolutePath}")
            }
            if (!photoDirectory.isDirectory) {
                error("Inventory photo path is not a directory: ${photoDirectory.absolutePath}")
            }

            photoDirectory
        }
    }

    companion object {
        const val DISPLAY_FILE_NAME = "display.webp"
        const val THUMBNAIL_FILE_NAME = "thumbnail.webp"

        private const val MEDIA_ROOT = "media"
        private const val INVENTORY_DIRECTORY = "inventory"
    }
}
