package de.avanzu.woolstash.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_item_photos",
    primaryKeys = ["itemId", "photoId"],
)
data class InventoryItemPhotoEntity(
    val itemId: String,
    val photoId: String,
    val caption: String?,
    val sortOrder: Int,
)
