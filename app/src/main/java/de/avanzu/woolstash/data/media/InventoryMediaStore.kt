package de.avanzu.woolstash.data.media

import de.avanzu.woolstash.data.local.InventoryItemPhotoDao
import de.avanzu.woolstash.data.local.InventoryItemPhotoEntity
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.PhotoId
import java.io.File
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryMediaStore(
    filesDir: File,
    private val inventoryItemPhotoDao: InventoryItemPhotoDao,
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
            val validPhotoFolders = itemDirectory.listFiles()
                ?.filter { file -> file.isDirectory }
                .orEmpty()
                .sortedBy { directory -> directory.name }
                .mapNotNull { directory ->
                    val parsedName = InventoryMediaFolderNames.parsePhotoFolderName(directory.name)
                        ?: return@mapNotNull null

                    val displayFile = directory.resolve(DISPLAY_FILE_NAME)
                    val thumbnailFile = directory.resolve(THUMBNAIL_FILE_NAME)
                    if (!displayFile.isFile || !thumbnailFile.isFile) {
                        return@mapNotNull null
                    }

                    ValidPhotoFolder(
                        photoId = parsedName.photoId,
                        assignedAt = parsedName.assignedAt,
                        directory = directory,
                        displayFile = displayFile,
                        thumbnailFile = thumbnailFile,
                    )
                }

            val metadataByPhotoId = inventoryItemPhotoDao.findByItemId(inventoryItemId.value)
                .associateBy { entity -> entity.photoId }
                .toMutableMap()
            var nextSortOrder = inventoryItemPhotoDao.nextSortOrder(inventoryItemId.value)

            validPhotoFolders.forEach { folder ->
                if (metadataByPhotoId.containsKey(folder.photoId.value)) {
                    return@forEach
                }

                val metadata = InventoryItemPhotoEntity(
                    itemId = inventoryItemId.value,
                    photoId = folder.photoId.value,
                    assignedAt = folder.assignedAt.toString(),
                    sortOrder = nextSortOrder,
                    isHero = false,
                )
                nextSortOrder += 1
                inventoryItemPhotoDao.upsert(metadata)
                metadataByPhotoId[metadata.photoId] = metadata
            }

            val sortedPhotos = validPhotoFolders
                .mapNotNull { folder ->
                    val metadata = metadataByPhotoId[folder.photoId.value]
                        ?: return@mapNotNull null

                    ListedPhoto(
                        folder = folder,
                        metadata = metadata,
                    )
                }
                .sortedWith(
                    compareBy<ListedPhoto> { listedPhoto -> !listedPhoto.metadata.isHero }
                        .thenBy { listedPhoto -> listedPhoto.metadata.sortOrder }
                        .thenBy { listedPhoto -> listedPhoto.folder.assignedAt }
                        .thenBy { listedPhoto -> listedPhoto.folder.photoId.value },
                )
            val hasExplicitHero = sortedPhotos.any { listedPhoto -> listedPhoto.metadata.isHero }

            sortedPhotos.mapIndexed { index, listedPhoto ->
                InventoryPhotoFile(
                    photoId = listedPhoto.folder.photoId,
                    inventoryItemId = inventoryItemId,
                    assignedAt = listedPhoto.folder.assignedAt,
                    displayFile = listedPhoto.folder.displayFile,
                    thumbnailFile = listedPhoto.folder.thumbnailFile,
                    isHero = listedPhoto.metadata.isHero || (!hasExplicitHero && index == 0),
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

    suspend fun deletePhoto(photo: InventoryPhotoFile) {
        withContext(Dispatchers.IO) {
            photo.displayFile.parentFile?.deleteRecursively()
            inventoryItemPhotoDao.delete(
                itemId = photo.inventoryItemId.value,
                photoId = photo.photoId.value,
            )
        }
    }

    suspend fun setHeroPhoto(
        inventoryItemId: InventoryItemId,
        photoId: PhotoId,
    ) {
        withContext(Dispatchers.IO) {
            inventoryItemPhotoDao.setHeroPhoto(
                itemId = inventoryItemId.value,
                photoId = photoId.value,
            )
        }
    }

    companion object {
        const val DISPLAY_FILE_NAME = "display.webp"
        const val THUMBNAIL_FILE_NAME = "thumbnail.webp"

        private const val MEDIA_ROOT = "media"
        private const val INVENTORY_DIRECTORY = "inventory"
    }
}

private data class ValidPhotoFolder(
    val photoId: PhotoId,
    val assignedAt: Instant,
    val directory: File,
    val displayFile: File,
    val thumbnailFile: File,
)

private data class ListedPhoto(
    val folder: ValidPhotoFolder,
    val metadata: InventoryItemPhotoEntity,
)
