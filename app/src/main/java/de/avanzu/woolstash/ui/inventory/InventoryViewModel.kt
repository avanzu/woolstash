package de.avanzu.woolstash.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.avanzu.woolstash.data.repository.InventoryRepository
import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.ProductDetails
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.Weight
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.normalizedDistinct
import java.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryListViewModel(
    private val inventoryRepository: InventoryRepository,
) : ViewModel() {
    val items: StateFlow<List<InventoryItem>> =
        inventoryRepository.observeItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    init {
        viewModelScope.launch {
            inventoryRepository.seedIfEmpty(SampleInventoryItems.items)
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            inventoryRepository.delete(item)
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            inventoryRepository.deleteAll()
        }
    }

    fun deleteItems(ids: Set<InventoryItemId>) {
        viewModelScope.launch {
            inventoryRepository.deleteItemsById(ids)
        }
    }

    fun createYarn(
        input: CreateInventoryItemInput,
        onCreated: (InventoryItemId) -> Unit,
    ) {
        createItem(
            item = input.toInventoryItem(details = YarnDetails()),
            onCreated = onCreated,
        )
    }

    fun createFiber(
        input: CreateInventoryItemInput,
        onCreated: (InventoryItemId) -> Unit,
    ) {
        createItem(
            item = input.toInventoryItem(details = FiberDetails()),
            onCreated = onCreated,
        )
    }

    private fun createItem(
        item: InventoryItem,
        onCreated: (InventoryItemId) -> Unit,
    ) {
        viewModelScope.launch {
            val createdItem = inventoryRepository.create(item)
            onCreated(createdItem.id)
        }
    }

    fun updateCoreFields(
        item: InventoryItem,
        input: CreateInventoryItemInput,
        onUpdated: () -> Unit = {},
    ) {
        viewModelScope.launch {
            inventoryRepository.save(input.toUpdatedInventoryItem(item))
            onUpdated()
        }
    }

    fun updateProductDetails(
        item: InventoryItem,
        details: ProductDetails,
        onUpdated: () -> Unit = {},
    ) {
        viewModelScope.launch {
            inventoryRepository.save(
                item.copy(
                    details = details,
                    updatedAt = Instant.now(),
                ),
            )
            onUpdated()
        }
    }
}

data class CreateInventoryItemInput(
    val name: String,
    val colorDescription: String?,
    val materialDescription: String?,
    val weightGrams: Double?,
    val tags: List<Tag>,
) {
    fun toInventoryItem(details: de.avanzu.woolstash.domain.model.ProductDetails): InventoryItem {
        return InventoryItem(
            name = name.trim(),
            colorDescription = colorDescription.cleanOrNull(),
            materialDescription = materialDescription.cleanOrNull(),
            weight = weightGrams?.let { grams -> Weight(grams) },
            tags = tags.normalizedDistinct(),
            details = details,
        )
    }

    fun toUpdatedInventoryItem(item: InventoryItem): InventoryItem {
        return item.copy(
            name = name.trim(),
            colorDescription = colorDescription.cleanOrNull(),
            materialDescription = materialDescription.cleanOrNull(),
            weight = weightGrams?.let { grams ->
                Weight(
                    grams = grams,
                    source = item.weight?.source ?: de.avanzu.woolstash.domain.model.MeasurementSource.Unknown,
                )
            },
            tags = tags.normalizedDistinct(),
            updatedAt = Instant.now(),
        )
    }
}

private fun String?.cleanOrNull(): String? {
    return this?.trim()?.takeIf { value -> value.isNotEmpty() }
}

class InventoryListViewModelFactory(
    private val inventoryRepository: InventoryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryListViewModel::class.java)) {
            return InventoryListViewModel(inventoryRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
