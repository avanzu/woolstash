package de.avanzu.woolstash.data.backup

import java.time.Instant

data class WoolStashBackupManifest(
    val backupFormatVersion: Int,
    val databaseSchemaVersion: Int,
    val appVersionCode: Long,
    val appVersionName: String,
    val minimumRestoreAppVersionCode: Long,
    val createdAt: Instant,
    val itemCount: Int,
    val photoCount: Int,
) {
    fun toJson(): String {
        return """
            {
              "backupFormatVersion": $backupFormatVersion,
              "databaseSchemaVersion": $databaseSchemaVersion,
              "appVersionCode": $appVersionCode,
              "appVersionName": "${appVersionName.escapeJson()}",
              "minimumRestoreAppVersionCode": $minimumRestoreAppVersionCode,
              "createdAt": "${createdAt}",
              "itemCount": $itemCount,
              "photoCount": $photoCount
            }
        """.trimIndent()
    }

    companion object {
        fun parse(json: String): WoolStashBackupManifest {
            return WoolStashBackupManifest(
                backupFormatVersion = json.intValue("backupFormatVersion"),
                databaseSchemaVersion = json.intValue("databaseSchemaVersion"),
                appVersionCode = json.longValue("appVersionCode"),
                appVersionName = json.stringValue("appVersionName"),
                minimumRestoreAppVersionCode = json.longValue("minimumRestoreAppVersionCode"),
                createdAt = Instant.parse(json.stringValue("createdAt")),
                itemCount = json.intValue("itemCount"),
                photoCount = json.intValue("photoCount"),
            )
        }
    }
}

private fun String.escapeJson(): String {
    return buildString {
        this@escapeJson.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}

private fun String.intValue(name: String): Int = longValue(name).toInt()

private fun String.longValue(name: String): Long {
    val match = Regex(""""$name"\s*:\s*(-?\d+)""").find(this)
        ?: error("Missing manifest field: $name")
    return match.groupValues[1].toLong()
}

private fun String.stringValue(name: String): String {
    val match = Regex(""""$name"\s*:\s*"((?:\\.|[^"])*)"""").find(this)
        ?: error("Missing manifest field: $name")
    return match.groupValues[1]
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
}
