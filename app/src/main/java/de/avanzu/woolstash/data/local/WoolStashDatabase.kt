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
        InventoryReferenceValueEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class WoolStashDatabase : RoomDatabase() {
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun inventoryItemPhotoDao(): InventoryItemPhotoDao
    abstract fun inventoryItemTagDao(): InventoryItemTagDao
    abstract fun inventoryReferenceValueDao(): InventoryReferenceValueDao

    companion object {
        const val DATABASE_NAME = "wool_stash.db"
        const val DATABASE_VERSION = 4

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

        val Migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inventory_reference_values (
                        type TEXT NOT NULL,
                        name TEXT NOT NULL,
                        PRIMARY KEY(type, name)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO inventory_reference_values(type, name)
                    SELECT 'Location', TRIM(location)
                    FROM inventory_items
                    WHERE location IS NOT NULL AND TRIM(location) != ''
                    """.trimIndent(),
                )
                db.execSQL("ALTER TABLE inventory_items ADD COLUMN manufacturer TEXT")
                db.execSQL("ALTER TABLE inventory_items ADD COLUMN purchaseSource TEXT")
            }
        }
    }
}
