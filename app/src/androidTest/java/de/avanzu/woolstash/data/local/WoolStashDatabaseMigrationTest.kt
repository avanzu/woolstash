package de.avanzu.woolstash.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WoolStashDatabaseMigrationTest {
    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = WoolStashDatabase::class.java,
    )

    @Test
    fun migration4To5_preservesInventoryAndAddsOrigins() {
        migrationTestHelper.createDatabase(TEST_DATABASE_NAME, 4).apply {
            execSQL(
                """
                INSERT INTO inventory_items(
                    id,
                    productType,
                    name,
                    status,
                    createdAt,
                    updatedAt
                ) VALUES (
                    'fiber-1',
                    'Fiber',
                    'Migration test fiber',
                    'Active',
                    '2026-09-14T10:00:00Z',
                    '2026-09-14T10:00:00Z'
                )
                """.trimIndent(),
            )
            close()
        }

        migrationTestHelper.runMigrationsAndValidate(
            TEST_DATABASE_NAME,
            5,
            true,
            WoolStashDatabase.Migration4To5,
        ).apply {
            assertEquals(1, queryCount("inventory_items"))
            assertEquals(0, queryCount("inventory_origins"))

            execSQL(
                """
                INSERT INTO inventory_origins(childItemId, parentItemId, consumedGrams)
                VALUES ('fiber-1', 'fiber-1', 25.0)
                """.trimIndent(),
            )
            assertEquals(1, queryCount("inventory_origins"))
            close()
        }
    }

    private fun androidx.sqlite.db.SupportSQLiteDatabase.queryCount(tableName: String): Int {
        return query("SELECT COUNT(*) FROM $tableName").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
    }

    companion object {
        private const val TEST_DATABASE_NAME = "wool-stash-migration-test"
    }
}
