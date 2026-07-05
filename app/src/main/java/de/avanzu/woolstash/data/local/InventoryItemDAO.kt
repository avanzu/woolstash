package de.avanzu.woolstash.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun findById(id: String): InventoryItemEntity?

    @Upsert
    suspend fun upsert(item: InventoryItemEntity)

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun count(): Int

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAll()

    @Query("DELETE FROM inventory_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

}