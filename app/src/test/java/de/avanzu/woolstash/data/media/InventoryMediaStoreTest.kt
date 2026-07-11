package de.avanzu.woolstash.data.media

import de.avanzu.woolstash.data.local.InventoryItemPhotoDao
import de.avanzu.woolstash.data.local.InventoryItemPhotoEntity
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.PhotoId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InventoryMediaStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun listPhotos_returnsCompletePhotoFoldersInLexicographicOrder() = runBlocking {
        val filesDir = temporaryFolder.newFolder("files")
        val inventoryItemId = InventoryItemId("item-1")
        val mediaStore = InventoryMediaStore(
            filesDir = filesDir,
            inventoryItemPhotoDao = FakeInventoryItemPhotoDao(),
            clock = Clock.fixed(Instant.parse("2026-07-06T18:42:33Z"), ZoneOffset.UTC),
            photoIdFactory = { PhotoId("generated-photo") },
        )

        val laterDirectory = createPhotoDirectory(
            mediaStore = mediaStore,
            inventoryItemId = inventoryItemId,
            assignedAt = Instant.parse("2026-07-06T18:42:34Z"),
            photoId = PhotoId("photo-a"),
        )
        val earlierDirectory = createPhotoDirectory(
            mediaStore = mediaStore,
            inventoryItemId = inventoryItemId,
            assignedAt = Instant.parse("2026-07-06T18:42:33Z"),
            photoId = PhotoId("photo-b"),
        )

        val photos = mediaStore.listPhotos(inventoryItemId)

        assertEquals(listOf(PhotoId("photo-b"), PhotoId("photo-a")), photos.map { photo -> photo.photoId })
        assertEquals(earlierDirectory.resolve(InventoryMediaStore.DISPLAY_FILE_NAME), photos[0].displayFile)
        assertEquals(laterDirectory.resolve(InventoryMediaStore.DISPLAY_FILE_NAME), photos[1].displayFile)
        assertEquals(true, photos[0].isHero)
        assertEquals(false, photos[1].isHero)
    }

    @Test
    fun listPhotos_ignoresInvalidAndIncompletePhotoFolders() = runBlocking {
        val filesDir = temporaryFolder.newFolder("files")
        val inventoryItemId = InventoryItemId("item-1")
        val mediaStore = InventoryMediaStore(
            filesDir = filesDir,
            inventoryItemPhotoDao = FakeInventoryItemPhotoDao(),
        )
        val itemDirectory = mediaStore.inventoryItemMediaDirectory(inventoryItemId)

        itemDirectory.resolve("invalid-name").mkdirs()
        itemDirectory.resolve("20260706T184233Z_missing-thumbnail").also { directory ->
            directory.mkdirs()
            directory.resolve(InventoryMediaStore.DISPLAY_FILE_NAME).writeText("display")
        }
        createPhotoDirectory(
            mediaStore = mediaStore,
            inventoryItemId = inventoryItemId,
            assignedAt = Instant.parse("2026-07-06T18:42:34Z"),
            photoId = PhotoId("valid-photo"),
        )

        val photos = mediaStore.listPhotos(inventoryItemId)

        assertEquals(listOf(PhotoId("valid-photo")), photos.map { photo -> photo.photoId })
    }

    @Test
    fun listPhotos_placesExplicitHeroFirst() = runBlocking {
        val filesDir = temporaryFolder.newFolder("files")
        val inventoryItemId = InventoryItemId("item-1")
        val photoDao = FakeInventoryItemPhotoDao()
        val mediaStore = InventoryMediaStore(
            filesDir = filesDir,
            inventoryItemPhotoDao = photoDao,
        )
        createPhotoDirectory(
            mediaStore = mediaStore,
            inventoryItemId = inventoryItemId,
            assignedAt = Instant.parse("2026-07-06T18:42:33Z"),
            photoId = PhotoId("photo-a"),
        )
        createPhotoDirectory(
            mediaStore = mediaStore,
            inventoryItemId = inventoryItemId,
            assignedAt = Instant.parse("2026-07-06T18:42:34Z"),
            photoId = PhotoId("photo-b"),
        )

        mediaStore.listPhotos(inventoryItemId)
        mediaStore.setHeroPhoto(inventoryItemId, PhotoId("photo-b"))

        val photos = mediaStore.listPhotos(inventoryItemId)

        assertEquals(listOf(PhotoId("photo-b"), PhotoId("photo-a")), photos.map { photo -> photo.photoId })
        assertEquals(listOf(true, false), photos.map { photo -> photo.isHero })
    }

    @Test
    fun deletePhoto_removesFolderAndMetadata() = runBlocking {
        val filesDir = temporaryFolder.newFolder("files")
        val inventoryItemId = InventoryItemId("item-1")
        val photoDao = FakeInventoryItemPhotoDao()
        val mediaStore = InventoryMediaStore(
            filesDir = filesDir,
            inventoryItemPhotoDao = photoDao,
        )
        val photoDirectory = createPhotoDirectory(
            mediaStore = mediaStore,
            inventoryItemId = inventoryItemId,
            assignedAt = Instant.parse("2026-07-06T18:42:33Z"),
            photoId = PhotoId("photo-a"),
        )
        val photo = mediaStore.listPhotos(inventoryItemId).single()

        mediaStore.deletePhoto(photo)

        assertEquals(false, photoDirectory.exists())
        assertEquals(emptyList<InventoryItemPhotoEntity>(), photoDao.findByItemId(inventoryItemId.value))
        assertEquals(emptyList<InventoryPhotoFile>(), mediaStore.listPhotos(inventoryItemId))
    }

    private fun createPhotoDirectory(
        mediaStore: InventoryMediaStore,
        inventoryItemId: InventoryItemId,
        assignedAt: Instant,
        photoId: PhotoId,
    ) = mediaStore.inventoryItemMediaDirectory(inventoryItemId)
        .resolve(
            InventoryMediaFolderNames.createPhotoFolderName(
                assignedAt = assignedAt,
                photoId = photoId,
            ),
        )
        .also { directory ->
            directory.mkdirs()
            directory.resolve(InventoryMediaStore.DISPLAY_FILE_NAME).writeText("display")
            directory.resolve(InventoryMediaStore.THUMBNAIL_FILE_NAME).writeText("thumbnail")
        }
}

private class FakeInventoryItemPhotoDao : InventoryItemPhotoDao {
    private val photos = mutableListOf<InventoryItemPhotoEntity>()

    override suspend fun findByItemId(itemId: String): List<InventoryItemPhotoEntity> {
        return photos.filter { photo -> photo.itemId == itemId }
    }

    override suspend fun upsert(photo: InventoryItemPhotoEntity) {
        photos.removeAll { existingPhoto ->
            existingPhoto.itemId == photo.itemId && existingPhoto.photoId == photo.photoId
        }
        photos += photo
    }

    override suspend fun nextSortOrder(itemId: String): Int {
        return photos
            .filter { photo -> photo.itemId == itemId }
            .maxOfOrNull { photo -> photo.sortOrder + 1 }
            ?: 0
    }

    override suspend fun delete(itemId: String, photoId: String) {
        photos.removeAll { photo -> photo.itemId == itemId && photo.photoId == photoId }
    }

    override suspend fun clearHeroPhoto(itemId: String) {
        photos.replaceAll { photo ->
            if (photo.itemId == itemId) {
                photo.copy(isHero = false)
            } else {
                photo
            }
        }
    }

    override suspend fun markHeroPhoto(itemId: String, photoId: String) {
        photos.replaceAll { photo ->
            if (photo.itemId == itemId && photo.photoId == photoId) {
                photo.copy(isHero = true)
            } else {
                photo
            }
        }
    }
}
