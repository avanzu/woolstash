package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.data.media.InventoryPhotoFile
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.ui.theme.WoolStashTheme

@Composable
fun InventoryListScreen(
    items: List<InventoryItem>,
    photoPreviews: Map<InventoryItemId, InventoryPhotoFile>,
    onDeleteItemsConfirmed: (Set<InventoryItemId>) -> Unit,
    onItemClick: (InventoryItem) -> Unit,
    onAddYarnClick: () -> Unit,
    onAddFiberClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedItemIds by remember {
        mutableStateOf<Set<InventoryItemId>>(emptySet())
    }

    var pendingDeleteIds by remember {
        mutableStateOf<Set<InventoryItemId>?>(null)
    }

    var activeTagFilter by remember {
        mutableStateOf<String?>(null)
    }
    var activeProductTypeFilter by remember {
        mutableStateOf<ProductType?>(null)
    }
    var activeSort by remember {
        mutableStateOf(InventoryListSort.UpdatedNewest)
    }

    val availableTags = remember(items) {
        items.availableTagNames()
    }
    val visibleItems = remember(items, activeTagFilter, activeProductTypeFilter, activeSort) {
        items
            .filterByProductType(activeProductTypeFilter)
            .filterByTag(activeTagFilter)
            .sortForInventoryList(activeSort)
    }
    val isSelectionMode = selectedItemIds.isNotEmpty()

    fun toggleSelection(item: InventoryItem) {
        selectedItemIds = if (item.id in selectedItemIds) {
            selectedItemIds - item.id
        } else {
            selectedItemIds + item.id
        }
    }

    fun enterSelectionMode(item: InventoryItem) {
        selectedItemIds = setOf(item.id)
    }

    fun clearSelection() {
        selectedItemIds = emptySet()
    }

    fun selectTagFilter(tag: String) {
        activeTagFilter = tag
        clearSelection()
    }

    fun clearTagFilter() {
        activeTagFilter = null
        clearSelection()
    }

    fun selectProductTypeFilter(productType: ProductType?) {
        activeProductTypeFilter = productType
        clearSelection()
    }

    fun selectSort(sort: InventoryListSort) {
        activeSort = sort
        clearSelection()
    }

    pendingDeleteIds?.let { ids ->
        DeleteItemsConfirmationDialog(
            itemCount = ids.size,
            onConfirm = {
                onDeleteItemsConfirmed(ids)
                pendingDeleteIds = null
                clearSelection()
            },
            onDismiss = {
                pendingDeleteIds = null
            },
        )
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (isSelectionMode) {
                    SelectionHeader(
                        selectedCount = selectedItemIds.size,
                        onCancelClick = { clearSelection() },
                        onDeleteClick = {
                            pendingDeleteIds = selectedItemIds
                        },
                    )
                } else {
                    InventoryListHeader(
                        totalItemCount = items.size,
                        visibleItemCount = visibleItems.size,
                        availableTags = availableTags,
                        activeTagFilter = activeTagFilter,
                        activeProductTypeFilter = activeProductTypeFilter,
                        activeSort = activeSort,
                        onTagFilterSelected = { tag -> selectTagFilter(tag) },
                        onTagFilterCleared = { clearTagFilter() },
                        onProductTypeFilterSelected = { productType ->
                            selectProductTypeFilter(productType)
                        },
                        onSortSelected = { sort -> selectSort(sort) },
                        onAddYarnClick = onAddYarnClick,
                        onAddFiberClick = onAddFiberClick,
                    )
                }
            }

            items(
                items = visibleItems,
                key = { item -> item.id.value },
            ) { item ->
                InventoryItemCard(
                    item = item,
                    photoPreview = photoPreviews[item.id],
                    isSelectionMode = isSelectionMode,
                    isSelected = item.id in selectedItemIds,
                    onClick = {
                        if (isSelectionMode) {
                            toggleSelection(item)
                        } else {
                            onItemClick(item)
                        }
                    },
                    onLongClick = {
                        enterSelectionMode(item)
                    },
                    onTagClick = { tag -> selectTagFilter(tag) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryListScreenPreview() {
    WoolStashTheme {
        InventoryListScreen(
            items = SampleInventoryItems.items,
            photoPreviews = emptyMap(),
            onDeleteItemsConfirmed = {},
            onItemClick = {},
            onAddYarnClick = {},
            onAddFiberClick = {},
        )
    }
}
