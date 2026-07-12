package de.avanzu.woolstash.data.backup

import de.avanzu.woolstash.data.local.WoolStashDatabase
import java.io.IOException

object WoolStashBackupCompatibility {
    fun validate(
        manifest: WoolStashBackupManifest,
        appVersionCode: Long,
        backupFormatVersion: Int = WoolStashBackupService.BACKUP_FORMAT_VERSION,
        databaseSchemaVersion: Int = WoolStashDatabase.DATABASE_VERSION,
    ) {
        when {
            manifest.backupFormatVersion != backupFormatVersion -> {
                throw IncompatibleBackupException("backup_format")
            }
            manifest.databaseSchemaVersion != databaseSchemaVersion -> {
                throw IncompatibleBackupException("database_schema")
            }
            appVersionCode < manifest.minimumRestoreAppVersionCode -> {
                throw IncompatibleBackupException("app_version")
            }
        }
    }
}

class IncompatibleBackupException(
    val reason: String,
) : IOException("Incompatible backup: $reason")
