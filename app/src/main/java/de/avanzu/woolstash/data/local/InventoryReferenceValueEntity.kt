package de.avanzu.woolstash.data.local

import androidx.room.Entity

@Entity(
    tableName = "inventory_reference_values",
    primaryKeys = ["type", "name"],
)
data class InventoryReferenceValueEntity(
    val type: String,
    val name: String,
)
