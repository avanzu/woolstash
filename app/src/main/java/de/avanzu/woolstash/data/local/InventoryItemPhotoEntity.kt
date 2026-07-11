package de.avanzu.woolstash.data.local

import androidx.room.Entity

@Entity(
    tableName = "inventory_item_photos",
    primaryKeys = ["itemId", "photoId"],
)
data class InventoryItemPhotoEntity(
    val itemId: String,
    val photoId: String,
    val assignedAt: String,
    val sortOrder: Int,
    val isHero: Boolean,
)
