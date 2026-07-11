package de.avanzu.woolstash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        InventoryItemEntity::class,
        InventoryItemPhotoEntity::class,
        InventoryItemTagEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class WoolStashDatabase : RoomDatabase() {
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun inventoryItemPhotoDao(): InventoryItemPhotoDao
    abstract fun inventoryItemTagDao(): InventoryItemTagDao

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

        val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inventory_item_tags (
                        itemId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        PRIMARY KEY(itemId, name),
                        FOREIGN KEY(itemId) REFERENCES inventory_items(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_item_tags_itemId ON inventory_item_tags(itemId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_item_tags_name ON inventory_item_tags(name)")
            }
        }
    }
}
