package de.avanzu.woolstash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        InventoryItemEntity::class,
        InventoryItemPhotoEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class WoolStashDatabase : RoomDatabase() {
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun inventoryItemPhotoDao(): InventoryItemPhotoDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inventory_item_photos (
                        itemId TEXT NOT NULL,
                        photoId TEXT NOT NULL,
                        assignedAt TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        isHero INTEGER NOT NULL,
                        PRIMARY KEY(itemId, photoId)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
