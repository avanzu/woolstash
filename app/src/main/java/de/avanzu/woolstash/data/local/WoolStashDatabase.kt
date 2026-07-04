package de.avanzu.woolstash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        InventoryItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class WoolStashDatabase : RoomDatabase() {
    abstract fun inventoryItemDao(): InventoryItemDao
}