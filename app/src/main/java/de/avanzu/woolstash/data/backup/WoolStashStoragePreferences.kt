package de.avanzu.woolstash.data.backup

import android.content.Context

class WoolStashStoragePreferences(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun activeSlot(): WoolStashStorageSlot {
        return preferences.getString(KEY_ACTIVE_SLOT, null)
            ?.let { value -> WoolStashStorageSlot.valueOf(value) }
            ?: WoolStashStorageSlot.Main
    }

    fun setActiveSlot(slot: WoolStashStorageSlot) {
        preferences.edit()
            .putString(KEY_ACTIVE_SLOT, slot.name)
            .apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "wool_stash_storage"
        private const val KEY_ACTIVE_SLOT = "active_slot"
    }
}
