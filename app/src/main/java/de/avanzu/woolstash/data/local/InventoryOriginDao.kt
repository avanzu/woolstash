package de.avanzu.woolstash.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryOriginDao {
    @Query("SELECT * FROM inventory_origins ORDER BY childItemId, parentItemId")
    fun observeAll(): Flow<List<InventoryOriginEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(origins: List<InventoryOriginEntity>)
}
