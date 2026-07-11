package de.avanzu.woolstash.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "inventory_item_tags",
    primaryKeys = ["itemId", "name"],
    foreignKeys = [
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("itemId"),
        Index("name"),
    ],
)
data class InventoryItemTagEntity(
    val itemId: String,
    val name: String,
)
