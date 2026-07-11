package de.avanzu.woolstash.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemTagDao {
    @Query("SELECT * FROM inventory_item_tags ORDER BY name")
    fun observeAll(): Flow<List<InventoryItemTagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tags: List<InventoryItemTagEntity>)

    @Query("DELETE FROM inventory_item_tags WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: String)

    @Query("DELETE FROM inventory_item_tags WHERE itemId IN (:itemIds)")
    suspend fun deleteByItemIds(itemIds: List<String>)

    @Query("DELETE FROM inventory_item_tags")
    suspend fun deleteAll()
}
