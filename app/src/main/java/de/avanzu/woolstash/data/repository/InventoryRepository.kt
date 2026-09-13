package de.avanzu.woolstash.data.repository

import androidx.room.withTransaction
import de.avanzu.woolstash.data.local.InventoryOriginEntity
import de.avanzu.woolstash.data.local.WoolStashDatabase
import de.avanzu.woolstash.data.local.toDomain
import de.avanzu.woolstash.data.local.toEntity
import de.avanzu.woolstash.data.local.toReferenceValueEntities
import de.avanzu.woolstash.data.local.toTagEntities
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.InventoryReferenceValue
import de.avanzu.woolstash.domain.model.InventoryOrigin
import de.avanzu.woolstash.domain.model.InventorySourceUsage
import de.avanzu.woolstash.domain.model.MeasurementSource
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.normalizedDistinct
import de.avanzu.woolstash.domain.model.toInventoryReferenceName
import de.avanzu.woolstash.domain.model.remainingWeightGrams
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class InventoryRepository(
    private val database: WoolStashDatabase,
) {
    private val inventoryItemDao = database.inventoryItemDao()
    private val inventoryItemTagDao = database.inventoryItemTagDao()
    private val inventoryReferenceValueDao = database.inventoryReferenceValueDao()
    private val inventoryOriginDao = database.inventoryOriginDao()

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

    fun observeReferenceValues(): Flow<List<InventoryReferenceValue>> {
        return inventoryReferenceValueDao.observeAll()
            .combine(inventoryItemDao.observeAll()) { referenceValues, items ->
                val itemReferenceValues = items
                    .map { entity -> entity.toDomain() }
                    .flatMap { item -> item.toReferenceValueEntities() }

                (referenceValues + itemReferenceValues)
                    .distinct()
                    .map { entity -> entity.toDomain() }
                    .sortedWith(
                        compareBy<InventoryReferenceValue> { value -> value.type.name }
                            .thenBy { value -> value.name.lowercase() },
                    )
            }
    }

    fun observeOrigins(): Flow<List<InventoryOrigin>> {
        return inventoryOriginDao.observeAll()
            .map { entities -> entities.map { entity -> entity.toDomain() } }
    }

    suspend fun save(item: InventoryItem) {
        val normalizedItem = item.copy(
            location = item.location.toInventoryReferenceName(),
            manufacturer = item.manufacturer.toInventoryReferenceName(),
            purchaseSource = item.purchaseSource.toInventoryReferenceName(),
            tags = item.tags.normalizedDistinct(),
        )

        database.withTransaction {
            inventoryReferenceValueDao.insertAll(normalizedItem.toReferenceValueEntities())
            inventoryItemDao.upsert(normalizedItem.toEntity())
            inventoryItemTagDao.deleteByItemId(normalizedItem.id.value)
            inventoryItemTagDao.insertAll(normalizedItem.toTagEntities())
        }
    }

    suspend fun create(item: InventoryItem): InventoryItem {
        save(item)
        return item
    }

    suspend fun createFromStock(
        item: InventoryItem,
        sourceUsages: List<InventorySourceUsage>,
    ): InventoryItem {
        require(sourceUsages.size >= 2) { "At least two source items are required." }
        require(sourceUsages.map { usage -> usage.itemId }.distinct().size == sourceUsages.size) {
            "Source items must be distinct."
        }
        require(sourceUsages.none { usage -> usage.itemId == item.id }) {
            "An item cannot be its own source."
        }

        val normalizedItem = item.normalizedForPersistence()
        val changedAt = Instant.now()

        database.withTransaction {
            val sourceItems = sourceUsages.map { usage ->
                val entity = requireNotNull(inventoryItemDao.findById(usage.itemId.value)) {
                    "Source item ${usage.itemId.value} does not exist."
                }
                require(ProductType.valueOf(entity.productType) == ProductType.Fiber) {
                    "Source item ${usage.itemId.value} must be a fiber item."
                }
                usage to entity
            }

            inventoryReferenceValueDao.insertAll(normalizedItem.toReferenceValueEntities())
            inventoryItemDao.upsert(normalizedItem.toEntity())
            inventoryItemTagDao.deleteByItemId(normalizedItem.id.value)
            inventoryItemTagDao.insertAll(normalizedItem.toTagEntities())

            sourceItems.forEach { (usage, entity) ->
                val currentGrams = requireNotNull(entity.weightGrams) {
                    "Source item ${entity.id} has no recorded weight."
                }
                val remainingGrams = remainingWeightGrams(currentGrams, usage.consumedGrams)
                inventoryItemDao.upsert(
                    entity.copy(
                        weightGrams = remainingGrams,
                        weightSource = MeasurementSource.Calculated.name,
                        updatedAt = changedAt.toString(),
                    ),
                )
            }

            inventoryOriginDao.insertAll(
                sourceUsages.map { usage ->
                    InventoryOriginEntity(
                        childItemId = normalizedItem.id.value,
                        parentItemId = usage.itemId.value,
                        consumedGrams = usage.consumedGrams,
                    )
                },
            )
        }

        return normalizedItem
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

    private fun InventoryItem.normalizedForPersistence(): InventoryItem {
        return copy(
            location = location.toInventoryReferenceName(),
            manufacturer = manufacturer.toInventoryReferenceName(),
            purchaseSource = purchaseSource.toInventoryReferenceName(),
            tags = tags.normalizedDistinct(),
        )
    }
}
