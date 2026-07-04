package de.avanzu.woolstash.data.repository

import de.avanzu.woolstash.data.local.InventoryItemDao
import de.avanzu.woolstash.data.local.toDomain
import de.avanzu.woolstash.data.local.toEntity
import de.avanzu.woolstash.domain.model.InventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepository(
    private val inventoryItemDao: InventoryItemDao,
) {
    fun observeItems(): Flow<List<InventoryItem>> {
        return inventoryItemDao.observeAll()
            .map { entities ->
                entities.map { entity -> entity.toDomain() }
            }
    }

    suspend fun save(item: InventoryItem) {
        inventoryItemDao.upsert(item.toEntity())
    }

    suspend fun seedIfEmpty(items: List<InventoryItem>) {
        if (inventoryItemDao.count() > 0) {
            return
        }

        items.forEach { item ->
            save(item)
        }
    }
}