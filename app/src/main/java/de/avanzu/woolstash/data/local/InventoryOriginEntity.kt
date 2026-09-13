package de.avanzu.woolstash.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "inventory_origins",
    primaryKeys = ["childItemId", "parentItemId"],
    foreignKeys = [
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["childItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("childItemId"),
        Index("parentItemId"),
    ],
)
data class InventoryOriginEntity(
    val childItemId: String,
    val parentItemId: String,
    val consumedGrams: Double,
)
