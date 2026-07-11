package de.avanzu.woolstash.data.repository

import androidx.room.withTransaction
import de.avanzu.woolstash.data.local.WoolStashDatabase
import de.avanzu.woolstash.data.local.toDomain
import de.avanzu.woolstash.data.local.toEntity
import de.avanzu.woolstash.data.local.toTagEntities
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.normalizedDistinct
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val database: WoolStashDatabase,
) {
    private val inventoryItemDao = database.inventoryItemDao()
    private val inventoryItemTagDao = database.inventoryItemTagDao()

    fun observeItems(): Flow<List<InventoryItem>> {
        return combine(
            inventoryItemDao.observeAll(),
            inventoryItemTagDao.observeAll(),
        ) { itemEntities, tagEntities ->
            val tagsByItemId = tagEntities
                .groupBy { tag -> tag.itemId }
                .mapValues { (_, tags) ->
                    tags.map { tag -> Tag(tag.name) }
                        .normalizedDistinct()
                }

            itemEntities.map { entity ->
                entity.toDomain(tags = tagsByItemId[entity.id].orEmpty())
            }
        }
    }

    suspend fun save(item: InventoryItem) {
        val normalizedItem = item.copy(tags = item.tags.normalizedDistinct())

        database.withTransaction {
            inventoryItemDao.upsert(normalizedItem.toEntity())
            inventoryItemTagDao.deleteByItemId(normalizedItem.id.value)
            inventoryItemTagDao.insertAll(normalizedItem.toTagEntities())
        }
    }

    suspend fun create(item: InventoryItem): InventoryItem {
        save(item)
        return item
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
        database.withTransaction {
            inventoryItemTagDao.deleteByItemId(item.id.value)
            inventoryItemDao.deleteById(item.id.value)
        }
    }

    suspend fun deleteAll() {
        database.withTransaction {
            inventoryItemTagDao.deleteAll()
            inventoryItemDao.deleteAll()
        }
    }

    suspend fun deleteItems(items: List<InventoryItem>) {
        val ids = items.map { item -> item.id.value }

        if (ids.isEmpty()) {
            return
        }

        database.withTransaction {
            inventoryItemTagDao.deleteByItemIds(ids)
            inventoryItemDao.deleteByIds(ids)
        }
    }

    suspend fun deleteItemsById(ids: Set<InventoryItemId>) {
        if (ids.isEmpty()) {
            return
        }

        val itemIds = ids.map { id -> id.value }

        database.withTransaction {
            inventoryItemTagDao.deleteByItemIds(itemIds)
            inventoryItemDao.deleteByIds(ids = itemIds)
        }
    }
}
