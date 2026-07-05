package de.avanzu.woolstash.data.repository

import de.avanzu.woolstash.data.local.InventoryItemDao
import de.avanzu.woolstash.data.local.toDomain
import de.avanzu.woolstash.data.local.toEntity
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
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

    suspend fun delete(item: InventoryItem) {
        inventoryItemDao.deleteById(item.id.value)
    }

    suspend fun deleteAll() {
        inventoryItemDao.deleteAll()
    }

    suspend fun deleteItems(items: List<InventoryItem>) {
        val ids = items.map { item -> item.id.value }

        if (ids.isEmpty()) {
            return
        }

        inventoryItemDao.deleteByIds(ids)
    }

    suspend fun deleteItemsById(ids: Set<InventoryItemId>) {
        if (ids.isEmpty()) {
            return
        }

        inventoryItemDao.deleteByIds(
            ids = ids.map { id -> id.value },
        )
    }
}