package de.avanzu.woolstash.data.media

import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.PhotoId
import java.io.File
import java.time.Instant

data class InventoryPhotoFile(
    val photoId: PhotoId,
    val inventoryItemId: InventoryItemId,
    val assignedAt: Instant,
    val displayFile: File,
    val thumbnailFile: File,
    val isHero: Boolean,
)
