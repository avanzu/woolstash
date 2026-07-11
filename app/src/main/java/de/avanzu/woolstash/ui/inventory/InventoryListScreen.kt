package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.data.media.InventoryPhotoFile
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.ui.theme.WoolStashTheme
import kotlinx.coroutines.launch

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
    var searchQuery by remember {
        mutableStateOf("")
    }

    val availableTags = remember(items) {
        items.availableTagNames()
    }
    val visibleItems = remember(
        items,
        activeTagFilter,
        activeProductTypeFilter,
        searchQuery,
        activeSort,
    ) {
        items
            .filterByProductType(activeProductTypeFilter)
            .filterByTag(activeTagFilter)
            .filterBySearchQuery(searchQuery)
            .sortForInventoryList(activeSort)
    }
    val isSelectionMode = selectedItemIds.isNotEmpty()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun clearTagFilter() {
        activeTagFilter = null
        clearSelection()
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun selectProductTypeFilter(productType: ProductType?) {
        activeProductTypeFilter = productType
        clearSelection()
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun selectSort(sort: InventoryListSort) {
        activeSort = sort
        clearSelection()
        coroutineScope.launch {
            drawerState.close()
        }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            InventoryFilterDrawerContent(
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
            )
        },
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                if (isSelectionMode) {
                    SelectionHeader(
                        selectedCount = selectedItemIds.size,
                        onCancelClick = { clearSelection() },
                        onDeleteClick = {
                            pendingDeleteIds = selectedItemIds
                        },
                    )
                } else {
                    InventoryListTopAppBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { query ->
                            searchQuery = query
                            clearSelection()
                        },
                        onMenuClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        onAddYarnClick = onAddYarnClick,
                        onAddFiberClick = onAddFiberClick,
                    )
                }
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        InventoryListCount(
                            totalItemCount = items.size,
                            visibleItemCount = visibleItems.size,
                            isFiltered = activeTagFilter != null ||
                                activeProductTypeFilter != null ||
                                searchQuery.isNotBlank(),
                        )
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
    }
}

@Composable
private fun InventoryListCount(
    totalItemCount: Int,
    visibleItemCount: Int,
    isFiltered: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = if (isFiltered) {
            stringResource(
                R.string.inventory_filtered_item_count,
                visibleItemCount,
                totalItemCount,
            )
        } else {
            stringResource(
                R.string.inventory_item_count,
                visibleItemCount,
            )
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
