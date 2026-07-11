package de.avanzu.woolstash.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface InventoryItemPhotoDao {
    @Query("SELECT * FROM inventory_item_photos WHERE itemId = :itemId")
    suspend fun findByItemId(itemId: String): List<InventoryItemPhotoEntity>

    @Upsert
    suspend fun upsert(photo: InventoryItemPhotoEntity)

    @Query("SELECT COALESCE(MAX(sortOrder) + 1, 0) FROM inventory_item_photos WHERE itemId = :itemId")
    suspend fun nextSortOrder(itemId: String): Int

    @Query("DELETE FROM inventory_item_photos WHERE itemId = :itemId AND photoId = :photoId")
    suspend fun delete(itemId: String, photoId: String)

    @Query("UPDATE inventory_item_photos SET isHero = 0 WHERE itemId = :itemId")
    suspend fun clearHeroPhoto(itemId: String)

    @Query("UPDATE inventory_item_photos SET isHero = 1 WHERE itemId = :itemId AND photoId = :photoId")
    suspend fun markHeroPhoto(itemId: String, photoId: String)

    @Transaction
    suspend fun setHeroPhoto(itemId: String, photoId: String) {
        clearHeroPhoto(itemId)
        markHeroPhoto(itemId, photoId)
    }
}
