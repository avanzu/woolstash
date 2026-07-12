package de.avanzu.woolstash.data.backup

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WoolStashBackupCompatibilityTest {
    @Test
    fun validate_acceptsMatchingBackup() {
        WoolStashBackupCompatibility.validate(
            manifest = manifest(),
            appVersionCode = 2,
            backupFormatVersion = 1,
            databaseSchemaVersion = 4,
        )
    }

    @Test
    fun validate_rejectsUnsupportedBackupFormat() {
        val error = runCatching {
            WoolStashBackupCompatibility.validate(
                manifest = manifest(backupFormatVersion = 2),
                appVersionCode = 2,
                backupFormatVersion = 1,
                databaseSchemaVersion = 4,
            )
        }.exceptionOrNull() as IncompatibleBackupException

        assertEquals("backup_format", error.reason)
    }

    @Test
    fun validate_rejectsMismatchedDatabaseSchema() {
        val error = runCatching {
            WoolStashBackupCompatibility.validate(
                manifest = manifest(databaseSchemaVersion = 5),
                appVersionCode = 2,
                backupFormatVersion = 1,
                databaseSchemaVersion = 4,
            )
        }.exceptionOrNull() as IncompatibleBackupException

        assertEquals("database_schema", error.reason)
    }

    @Test
    fun validate_rejectsBackupThatRequiresNewerApp() {
        val error = runCatching {
            WoolStashBackupCompatibility.validate(
                manifest = manifest(minimumRestoreAppVersionCode = 3),
                appVersionCode = 2,
                backupFormatVersion = 1,
                databaseSchemaVersion = 4,
            )
        }.exceptionOrNull() as IncompatibleBackupException

        assertEquals("app_version", error.reason)
    }

    @Test
    fun manifest_roundTripsThroughJson() {
        val manifest = manifest(appVersionName = "1.0 \"test\"")

        val parsed = WoolStashBackupManifest.parse(manifest.toJson())

        assertEquals(manifest, parsed)
    }

    private fun manifest(
        backupFormatVersion: Int = 1,
        databaseSchemaVersion: Int = 4,
        appVersionName: String = "1.0",
        minimumRestoreAppVersionCode: Long = 1,
    ): WoolStashBackupManifest {
        return WoolStashBackupManifest(
            backupFormatVersion = backupFormatVersion,
            databaseSchemaVersion = databaseSchemaVersion,
            appVersionCode = 1,
            appVersionName = appVersionName,
            minimumRestoreAppVersionCode = minimumRestoreAppVersionCode,
            createdAt = Instant.parse("2026-07-12T10:15:30Z"),
            itemCount = 3,
            photoCount = 4,
        )
    }
}
