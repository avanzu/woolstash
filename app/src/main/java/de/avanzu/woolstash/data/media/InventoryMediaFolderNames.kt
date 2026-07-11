package de.avanzu.woolstash.data.media

import de.avanzu.woolstash.domain.model.PhotoId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object InventoryMediaFolderNames {
    private val assignedAtFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC)

    fun createPhotoFolderName(assignedAt: Instant, photoId: PhotoId): String {
        return "${assignedAtFormatter.format(assignedAt)}_${photoId.value}"
    }

    fun parsePhotoFolderName(name: String): ParsedInventoryPhotoFolderName? {
        val separatorIndex = name.indexOf('_')
        if (separatorIndex <= 0 || separatorIndex == name.lastIndex) {
            return null
        }

        val assignedAtPart = name.substring(0, separatorIndex)
        val photoIdPart = name.substring(separatorIndex + 1)
        if (photoIdPart.isBlank()) {
            return null
        }

        val assignedAt = try {
            LocalDateTime.parse(assignedAtPart, assignedAtFormatter)
                .toInstant(ZoneOffset.UTC)
        } catch (_: DateTimeParseException) {
            return null
        }

        return ParsedInventoryPhotoFolderName(
            assignedAt = assignedAt,
            photoId = PhotoId(photoIdPart),
        )
    }
}

data class ParsedInventoryPhotoFolderName(
    val assignedAt: Instant,
    val photoId: PhotoId,
)
