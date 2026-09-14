package de.avanzu.woolstash.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WoolStashBackupPromotionTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun restoreMainFilesFromRollback_replacesPartiallyPromotedFiles() {
        val databasesDirectory = temporaryFolder.newFolder("databases")
        val filesDirectory = temporaryFolder.newFolder("files")
        val rollbackDirectory = temporaryFolder.newFolder("rollback")
        val mainMediaDirectory = filesDirectory.resolve("media")

        databasesDirectory.resolve("wool_stash.db").writeText("partially promoted database")
        mainMediaDirectory.mkdirs()
        mainMediaDirectory.resolve("staging-photo.webp").writeText("staging photo")

        rollbackDirectory.resolve("wool_stash.db").writeText("original database")
        rollbackDirectory.resolve("wool_stash.db-wal").writeText("original wal")
        rollbackDirectory.resolve("media").also { mediaDirectory ->
            mediaDirectory.mkdirs()
            mediaDirectory.resolve("original-photo.webp").writeText("original photo")
        }

        restoreMainFilesFromRollback(
            rollbackDirectory = rollbackDirectory,
            databasesDirectory = databasesDirectory,
            mainMediaDirectory = mainMediaDirectory,
        )

        assertEquals("original database", databasesDirectory.resolve("wool_stash.db").readText())
        assertEquals("original wal", databasesDirectory.resolve("wool_stash.db-wal").readText())
        assertEquals("original photo", mainMediaDirectory.resolve("original-photo.webp").readText())
        assertFalse(mainMediaDirectory.resolve("staging-photo.webp").exists())
        assertEquals(emptyList<String>(), rollbackDirectory.listFiles().orEmpty().map { it.name })
    }
}
