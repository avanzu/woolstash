package de.avanzu.woolstash.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryReferenceValueDao {
    @Query("SELECT * FROM inventory_reference_values ORDER BY type, name")
    fun observeAll(): Flow<List<InventoryReferenceValueEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(values: List<InventoryReferenceValueEntity>)

    @Query("DELETE FROM inventory_reference_values")
    suspend fun deleteAll()
}
