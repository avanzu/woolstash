package de.avanzu.woolstash.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.avanzu.woolstash.data.repository.InventoryRepository
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryListViewModel(
    private val inventoryRepository: InventoryRepository,
) : ViewModel() {
    val items: StateFlow<List<de.avanzu.woolstash.domain.model.InventoryItem>> =
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