package de.avanzu.woolstash.data.backup

import de.avanzu.woolstash.data.local.WoolStashDatabase
import java.io.IOException

object WoolStashBackupCompatibility {
    fun validate(
        manifest: WoolStashBackupManifest,
        appVersionCode: Long,
        backupFormatVersion: Int = WoolStashBackupService.BACKUP_FORMAT_VERSION,
        databaseSchemaVersion: Int = WoolStashDatabase.DATABASE_VERSION,
        minimumDatabaseSchemaVersion: Int = MINIMUM_RESTORE_DATABASE_SCHEMA_VERSION,
    ) {
        when {
            manifest.backupFormatVersion != backupFormatVersion -> {
                throw IncompatibleBackupException("backup_format")
            }
            manifest.databaseSchemaVersion !in minimumDatabaseSchemaVersion..databaseSchemaVersion -> {
                throw IncompatibleBackupException("database_schema")
            }
            appVersionCode < manifest.minimumRestoreAppVersionCode -> {
                throw IncompatibleBackupException("app_version")
            }
        }
    }

    // The first released backup format (v1.0.0) contains Room schema 4.
    const val MINIMUM_RESTORE_DATABASE_SCHEMA_VERSION = 4
}

class IncompatibleBackupException(
    val reason: String,
) : IOException("Incompatible backup: $reason")
