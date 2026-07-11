package de.avanzu.woolstash.data.media

import de.avanzu.woolstash.domain.model.PhotoId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryMediaFolderNamesTest {
    @Test
    fun createPhotoFolderName_usesUtcSortableFileNameFormat() {
        val folderName = InventoryMediaFolderNames.createPhotoFolderName(
            assignedAt = Instant.parse("2026-07-06T18:42:33Z"),
            photoId = PhotoId("7d8e4b5c-1234"),
        )

        assertEquals("20260706T184233Z_7d8e4b5c-1234", folderName)
    }

    @Test
    fun parsePhotoFolderName_readsAssignedAtAndPhotoId() {
        val parsedName = InventoryMediaFolderNames.parsePhotoFolderName(
            "20260706T184233Z_7d8e4b5c-1234",
        )

        assertEquals(Instant.parse("2026-07-06T18:42:33Z"), parsedName?.assignedAt)
        assertEquals(PhotoId("7d8e4b5c-1234"), parsedName?.photoId)
    }

    @Test
    fun parsePhotoFolderName_returnsNullForInvalidNames() {
        val invalidNames = listOf(
            "",
            "20260706T184233Z",
            "20260706T184233Z_",
            "_7d8e4b5c-1234",
            "2026-07-06T18:42:33Z_7d8e4b5c-1234",
            "20260706T184233_7d8e4b5c-1234",
            "not-a-date_7d8e4b5c-1234",
        )

        invalidNames.forEach { name ->
            assertNull("Expected '$name' to be invalid", InventoryMediaFolderNames.parsePhotoFolderName(name))
        }
    }

    @Test
    fun folderNamesWithEarlierAssignedAtSortBeforeLaterAssignedAt() {
        val earlierName = InventoryMediaFolderNames.createPhotoFolderName(
            assignedAt = Instant.parse("2026-07-06T18:42:33Z"),
            photoId = PhotoId("photo-b"),
        )
        val laterName = InventoryMediaFolderNames.createPhotoFolderName(
            assignedAt = Instant.parse("2026-07-06T18:42:34Z"),
            photoId = PhotoId("photo-a"),
        )

        assertTrue(earlierName < laterName)
    }

    @Test
    fun folderNamesWithSameAssignedAtSortByPhotoId() {
        val firstName = InventoryMediaFolderNames.createPhotoFolderName(
            assignedAt = Instant.parse("2026-07-06T18:42:33Z"),
            photoId = PhotoId("photo-a"),
        )
        val secondName = InventoryMediaFolderNames.createPhotoFolderName(
            assignedAt = Instant.parse("2026-07-06T18:42:33Z"),
            photoId = PhotoId("photo-b"),
        )

        assertTrue(firstName < secondName)
    }
}
