package de.avanzu.woolstash.data.backup

import android.content.ContentResolver
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteDatabase
import de.avanzu.woolstash.data.local.WoolStashDatabase
import java.io.File
import java.io.IOException
import java.time.Clock
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WoolStashBackupService(
    private val contentResolver: ContentResolver,
    private val databasesDirectory: File,
    private val filesDirectory: File,
    private val cacheDirectory: File,
    private val appVersionCode: Long,
    private val appVersionName: String,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend fun exportMainBackup(
        database: WoolStashDatabase,
        targetUri: Uri,
    ) {
        withContext(Dispatchers.IO) {
            database.openHelper.writableDatabase.checkpoint()
            val manifest = WoolStashBackupManifest(
                backupFormatVersion = BACKUP_FORMAT_VERSION,
                databaseSchemaVersion = WoolStashDatabase.DATABASE_VERSION,
                appVersionCode = appVersionCode,
                appVersionName = appVersionName,
                minimumRestoreAppVersionCode = appVersionCode,
                createdAt = clock.instant(),
                itemCount = database.openHelper.readableDatabase.count("inventory_items"),
                photoCount = database.openHelper.readableDatabase.count("inventory_item_photos"),
            )
            val output = contentResolver.openOutputStream(targetUri)
                ?: throw IOException("Could not open backup target: $targetUri")

            output.use { stream ->
                ZipOutputStream(stream.buffered()).use { zip ->
                    zip.writeTextEntry(MANIFEST_ENTRY, manifest.toJson())
                    writeDatabaseEntries(
                        zip = zip,
                        slot = WoolStashStorageSlot.Main,
                    )
                    writeDirectoryEntries(
                        zip = zip,
                        sourceDirectory = mainMediaDirectory(),
                        entryRoot = MEDIA_ENTRY_ROOT,
                    )
                }
            }
        }
    }

    suspend fun restoreToStaging(sourceUri: Uri): WoolStashBackupManifest {
        return withContext(Dispatchers.IO) {
            val tempDirectory = cacheDirectory.resolve("backup-restore-temp")
            tempDirectory.deleteRecursively()
            tempDirectory.mkdirsOrThrow()

            try {
                unzipSource(sourceUri, tempDirectory)
                val manifest = WoolStashBackupManifest.parse(
                    tempDirectory.resolve(MANIFEST_ENTRY).readText(),
                )
                validateCompatibility(manifest)
                validateRestoreFiles(tempDirectory)

                deleteStagingFiles()
                copyDatabaseFilesFromRestore(tempDirectory)
                copyMediaFilesFromRestore(tempDirectory)
                manifest
            } finally {
                tempDirectory.deleteRecursively()
            }
        }
    }

    suspend fun discardStaging() {
        withContext(Dispatchers.IO) {
            deleteStagingFiles()
        }
    }

    suspend fun promoteStagingToMain() {
        withContext(Dispatchers.IO) {
            val stagingDatabase = databaseFile(WoolStashStorageSlot.Staging)
            if (!stagingDatabase.isFile) {
                throw IOException("No staging database exists.")
            }

            val rollbackDirectory = cacheDirectory.resolve("backup-main-rollback")
            rollbackDirectory.deleteRecursively()
            rollbackDirectory.mkdirsOrThrow()

            try {
                moveExistingMainToRollback(rollbackDirectory)
                moveStagingToMain()
                rollbackDirectory.deleteRecursively()
            } catch (error: Throwable) {
                restoreMainFromRollback(rollbackDirectory)
                throw error
            }
        }
    }

    fun hasStaging(): Boolean {
        return databaseFile(WoolStashStorageSlot.Staging).isFile
    }

    private fun validateCompatibility(manifest: WoolStashBackupManifest) {
        WoolStashBackupCompatibility.validate(
            manifest = manifest,
            appVersionCode = appVersionCode,
        )
    }

    private fun validateRestoreFiles(directory: File) {
        if (!directory.resolve(DATABASE_ENTRY_ROOT)
                .resolve(WoolStashStorageSlot.Main.databaseName)
                .isFile
        ) {
            throw IOException("Backup does not contain a database.")
        }
    }

    private fun unzipSource(sourceUri: Uri, targetDirectory: File) {
        val input = contentResolver.openInputStream(sourceUri)
            ?: throw IOException("Could not open backup source: $sourceUri")

        input.use { stream ->
            ZipInputStream(stream.buffered()).use { zip ->
                generateSequence { zip.nextEntry }.forEach { entry ->
                    val target = targetDirectory.resolve(entry.name).safeChildOf(targetDirectory)
                    if (entry.isDirectory) {
                        target.mkdirsOrThrow()
                    } else {
                        target.parentFile?.mkdirsOrThrow()
                        target.outputStream().use { output ->
                            zip.copyTo(output)
                        }
                    }
                    zip.closeEntry()
                }
            }
        }
    }

    private fun writeDatabaseEntries(
        zip: ZipOutputStream,
        slot: WoolStashStorageSlot,
    ) {
        databaseFiles(slot).forEach { file ->
            if (file.isFile) {
                zip.writeFileEntry(
                    entryName = "$DATABASE_ENTRY_ROOT/${mainDatabaseFileName(file.name)}",
                    file = file,
                )
            }
        }
    }

    private fun writeDirectoryEntries(
        zip: ZipOutputStream,
        sourceDirectory: File,
        entryRoot: String,
    ) {
        if (!sourceDirectory.isDirectory) {
            return
        }

        sourceDirectory.walkTopDown()
            .filter { file -> file.isFile }
            .forEach { file ->
                zip.writeFileEntry(
                    entryName = "$entryRoot/${file.relativeTo(sourceDirectory).invariantSeparatorsPath}",
                    file = file,
                )
            }
    }

    private fun copyDatabaseFilesFromRestore(directory: File) {
        val sourceDirectory = directory.resolve(DATABASE_ENTRY_ROOT)
        listOf(
            WoolStashStorageSlot.Main.databaseName,
            "${WoolStashStorageSlot.Main.databaseName}-wal",
            "${WoolStashStorageSlot.Main.databaseName}-shm",
        ).forEach { sourceName ->
            val source = sourceDirectory.resolve(sourceName)
            if (source.isFile) {
                source.copyTo(
                    target = stagingDatabaseName(sourceName),
                    overwrite = true,
                )
            }
        }
    }

    private fun copyMediaFilesFromRestore(directory: File) {
        val source = directory.resolve(MEDIA_ENTRY_ROOT)
        val target = stagingFilesDirectory().resolve("media")
        target.deleteRecursively()
        if (source.isDirectory) {
            source.copyRecursively(target = target, overwrite = true)
        }
    }

    private fun moveExistingMainToRollback(rollbackDirectory: File) {
        databaseFiles(WoolStashStorageSlot.Main).forEach { file ->
            if (file.exists()) {
                file.renameToOrThrow(rollbackDirectory.resolve(file.name))
            }
        }
        val mainMedia = mainMediaDirectory()
        if (mainMedia.exists()) {
            mainMedia.renameToOrThrow(rollbackDirectory.resolve("media"))
        }
    }

    private fun restoreMainFromRollback(rollbackDirectory: File) {
        rollbackDirectory.listFiles().orEmpty().forEach { file ->
            val target = if (file.name == "media") {
                mainMediaDirectory()
            } else {
                databasesDirectory.resolve(file.name)
            }
            if (!target.exists()) {
                file.renameTo(target)
            }
        }
    }

    private fun moveStagingToMain() {
        databaseFiles(WoolStashStorageSlot.Staging).forEach { file ->
            if (file.exists()) {
                file.renameToOrThrow(databasesDirectory.resolve(mainDatabaseFileName(file.name)))
            }
        }
        val stagingMedia = stagingFilesDirectory().resolve("media")
        if (stagingMedia.exists()) {
            stagingMedia.renameToOrThrow(mainMediaDirectory())
        }
        stagingFilesDirectory().deleteRecursively()
    }

    private fun deleteStagingFiles() {
        databaseFiles(WoolStashStorageSlot.Staging).forEach { file ->
            file.delete()
        }
        stagingFilesDirectory().deleteRecursively()
    }

    private fun databaseFiles(slot: WoolStashStorageSlot): List<File> {
        val database = databaseFile(slot)
        return listOf(
            database,
            File("${database.absolutePath}-wal"),
            File("${database.absolutePath}-shm"),
        )
    }

    private fun databaseFile(slot: WoolStashStorageSlot): File {
        return databasesDirectory.resolve(slot.databaseName)
    }

    private fun stagingDatabaseName(sourceName: String): File {
        return databasesDirectory.resolve(sourceName.replaceMainDatabaseName(WoolStashStorageSlot.Staging.databaseName))
    }

    private fun mainDatabaseFileName(sourceName: String): String {
        return sourceName.replaceMainDatabaseName(WoolStashStorageSlot.Main.databaseName)
    }

    private fun String.replaceMainDatabaseName(replacement: String): String {
        return when (this) {
            WoolStashStorageSlot.Main.databaseName -> replacement
            "${WoolStashStorageSlot.Main.databaseName}-wal" -> "$replacement-wal"
            "${WoolStashStorageSlot.Main.databaseName}-shm" -> "$replacement-shm"
            WoolStashStorageSlot.Staging.databaseName -> WoolStashStorageSlot.Main.databaseName
            "${WoolStashStorageSlot.Staging.databaseName}-wal" -> "${WoolStashStorageSlot.Main.databaseName}-wal"
            "${WoolStashStorageSlot.Staging.databaseName}-shm" -> "${WoolStashStorageSlot.Main.databaseName}-shm"
            else -> this
        }
    }

    private fun mainMediaDirectory(): File {
        return filesDirectory.resolve("media")
    }

    private fun stagingFilesDirectory(): File {
        return filesDirectory.resolve("restore/staging/files")
    }

    companion object {
        const val BACKUP_FORMAT_VERSION = 1
        private const val MANIFEST_ENTRY = "manifest.json"
        private const val DATABASE_ENTRY_ROOT = "database"
        private const val MEDIA_ENTRY_ROOT = "files/media"
    }
}

private fun SupportSQLiteDatabase.checkpoint() {
    query("PRAGMA wal_checkpoint(FULL)").use {
        while (it.moveToNext()) {
            // Exhaust the cursor so SQLite executes the checkpoint.
        }
    }
}

private fun SupportSQLiteDatabase.count(tableName: String): Int {
    return query("SELECT COUNT(*) FROM $tableName").use { cursor ->
        if (cursor.moveToFirst()) cursor.getInt(0) else 0
    }
}

private fun ZipOutputStream.writeTextEntry(
    entryName: String,
    text: String,
) {
    putNextEntry(ZipEntry(entryName))
    write(text.toByteArray(Charsets.UTF_8))
    closeEntry()
}

private fun ZipOutputStream.writeFileEntry(
    entryName: String,
    file: File,
) {
    putNextEntry(ZipEntry(entryName))
    file.inputStream().use { input ->
        input.copyTo(this)
    }
    closeEntry()
}

private fun File.mkdirsOrThrow() {
    if (!exists() && !mkdirs()) {
        throw IOException("Could not create directory: $absolutePath")
    }
}

private fun File.renameToOrThrow(target: File) {
    target.parentFile?.mkdirsOrThrow()
    if (!renameTo(target)) {
        throw IOException("Could not move $absolutePath to ${target.absolutePath}")
    }
}

private fun File.safeChildOf(parent: File): File {
    val parentPath = parent.canonicalPath
    val childPath = canonicalPath
    if (childPath != parentPath && !childPath.startsWith("$parentPath${File.separator}")) {
        throw IOException("Unsafe zip entry path: $childPath")
    }
    return this
}
