package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    isStagingActive: Boolean,
    isBackupBusy: Boolean,
    onDeleteItemsConfirmed: (Set<InventoryItemId>) -> Unit,
    onItemClick: (InventoryItem) -> Unit,
    onAddYarnClick: () -> Unit,
    onAddFiberClick: () -> Unit,
    onAddYarnFromStockClick: () -> Unit,
    onAddFiberFromStockClick: () -> Unit,
    onCreateBackupClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedItemIds by remember {
        mutableStateOf<Set<InventoryItemId>>(emptySet())
    }

    var pendingDeleteIds by remember {
        mutableStateOf<Set<InventoryItemId>?>(null)
    }

    var searchCriteria by remember {
        mutableStateOf(InventorySearchCriteria())
    }
    var activeSort by remember {
        mutableStateOf(InventoryListSort())
    }
    var searchQuery by remember {
        mutableStateOf("")
    }
    var isSearchFilterSheetVisible by remember {
        mutableStateOf(false)
    }
    var isAddMenuExpanded by remember {
        mutableStateOf(false)
    }
    var isPrivacyDialogVisible by remember {
        mutableStateOf(false)
    }

    val listItems = remember(items, searchCriteria.includeDepletedItems) {
        items.filterByAvailableAmount(searchCriteria.includeDepletedItems)
    }
    val availableTags = remember(items) {
        items.availableTagNames()
    }
    val visibleItems = remember(
        items,
        searchCriteria,
        searchQuery,
        activeSort,
    ) {
        items
            .filterBySearchCriteria(searchCriteria)
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
        searchCriteria = searchCriteria.copy(tags = setOf(tag))
        clearSelection()
    }

    fun selectProductTypeFilter(productType: ProductType?) {
        searchCriteria = searchCriteria.withProductType(productType)
        clearSelection()
    }

    fun selectSort(field: InventoryListSortField) {
        activeSort = activeSort.select(field)
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

    if (isSearchFilterSheetVisible) {
        InventorySearchFilterSheet(
            criteria = searchCriteria,
            availableTags = availableTags,
            resultCount = { criteria ->
                items
                    .filterBySearchCriteria(criteria)
                    .filterBySearchQuery(searchQuery)
                    .size
            },
            onApply = { criteria ->
                searchCriteria = criteria
                isSearchFilterSheetVisible = false
                clearSelection()
            },
            onDismiss = {
                isSearchFilterSheetVisible = false
            },
        )
    }

    if (isPrivacyDialogVisible) {
        PrivacyInformationDialog(
            onDismiss = { isPrivacyDialogVisible = false },
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            InventoryDrawerContent(
                isStagingActive = isStagingActive,
                isBackupBusy = isBackupBusy,
                onCreateBackupClick = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    onCreateBackupClick()
                },
                onRestoreBackupClick = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    onRestoreBackupClick()
                },
                onPrivacyClick = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    isPrivacyDialogVisible = true
                },
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
                        activeFilterCount = searchCriteria.activeFilterCount,
                        activeSort = activeSort,
                        onSearchQueryChange = { query ->
                            searchQuery = query
                            clearSelection()
                        },
                        onSearchFiltersClick = {
                            isSearchFilterSheetVisible = true
                        },
                        onSortSelected = { field -> selectSort(field) },
                        onMenuClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                Box {
                    FloatingActionButton(
                        onClick = { isAddMenuExpanded = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.action_add),
                        )
                    }
                    InventoryAddMenu(
                        expanded = isAddMenuExpanded,
                        onExpandedChange = { expanded -> isAddMenuExpanded = expanded },
                        onAddYarnClick = onAddYarnClick,
                        onAddFiberClick = onAddFiberClick,
                        onAddYarnFromStockClick = onAddYarnFromStockClick,
                        onAddFiberFromStockClick = onAddFiberFromStockClick,
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
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        ProductTypeFilterRow(
                            items = listItems,
                            selectedProductType = searchCriteria.productType,
                            onProductTypeSelected = { productType ->
                                selectProductTypeFilter(productType)
                            },
                        )
                    }

                    item {
                        InventoryListCount(
                            totalItemCount = listItems.size,
                            visibleItemCount = visibleItems.size,
                            isFiltered = searchCriteria.activeFilterCount > 0 ||
                                searchQuery.isNotBlank(),
                        )
                    }

                    if (visibleItems.isEmpty()) {
                        item {
                            InventoryEmptyState(
                                isFiltered = listItems.isNotEmpty() || items.isNotEmpty(),
                                onAddClick = { isAddMenuExpanded = true },
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
    }
}

@Composable
private fun PrivacyInformationDialog(
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.privacy_title))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.privacy_local_processing))
                Text(stringResource(R.string.privacy_backups))
                Text(stringResource(R.string.privacy_deletion))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { uriHandler.openUri(privacyPolicyUrl) },
            ) {
                Text(stringResource(R.string.privacy_open_full_policy))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

@Composable
private fun ProductTypeFilterRow(
    items: List<InventoryItem>,
    selectedProductType: ProductType?,
    onProductTypeSelected: (ProductType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val yarnCount = items.count { item -> item.productType == ProductType.Yarn }
    val fiberCount = items.count { item -> item.productType == ProductType.Fiber }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedProductType == null,
            onClick = { onProductTypeSelected(null) },
            label = { Text(stringResource(R.string.inventory_type_filter_with_count, items.size)) },
        )
        FilterChip(
            selected = selectedProductType == ProductType.Yarn,
            onClick = { onProductTypeSelected(ProductType.Yarn) },
            label = { Text(stringResource(R.string.inventory_yarn_filter_with_count, yarnCount)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ProductType.Yarn.containerColor(),
                selectedLabelColor = ProductType.Yarn.onContainerColor(),
            ),
        )
        FilterChip(
            selected = selectedProductType == ProductType.Fiber,
            onClick = { onProductTypeSelected(ProductType.Fiber) },
            label = { Text(stringResource(R.string.inventory_fiber_filter_with_count, fiberCount)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ProductType.Fiber.containerColor(),
                selectedLabelColor = ProductType.Fiber.onContainerColor(),
            ),
        )
    }
}

@Composable
private fun InventoryEmptyState(
    isFiltered: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ProductTypeArtwork(
            productType = ProductType.Yarn,
            contentDescription = null,
            size = 128.dp,
        )
        Text(
            text = stringResource(
                if (isFiltered) R.string.inventory_empty_filtered_title else R.string.inventory_empty_title,
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                if (isFiltered) R.string.inventory_empty_filtered_body else R.string.inventory_empty_body,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (!isFiltered) {
            androidx.compose.material3.TextButton(onClick = onAddClick) {
                Text(stringResource(R.string.inventory_empty_action))
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
        fontWeight = FontWeight.Medium,
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
            isStagingActive = false,
            isBackupBusy = false,
            onDeleteItemsConfirmed = {},
            onItemClick = {},
            onAddYarnClick = {},
            onAddFiberClick = {},
            onAddYarnFromStockClick = {},
            onAddFiberFromStockClick = {},
            onCreateBackupClick = {},
            onRestoreBackupClick = {},
        )
    }
}
