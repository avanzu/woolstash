package de.avanzu.woolstash.data.backup

enum class WoolStashStorageSlot(
    val databaseName: String,
) {
    Main("wool_stash.db"),
    Staging("wool_stash_staging.db"),
}
