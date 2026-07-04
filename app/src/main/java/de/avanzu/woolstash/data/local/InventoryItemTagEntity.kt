package de.avanzu.woolstash.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_item_tags",
    primaryKeys = ["itemId", "name"],
)
data class InventoryItemTagEntity(
    val itemId: String,
    val name: String,
)