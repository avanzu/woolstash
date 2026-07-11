package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.ui.theme.WoolStashTheme

@Composable
internal fun InventoryListHeader(
    totalItemCount: Int,
    visibleItemCount: Int,
    availableTags: List<String>,
    activeTagFilter: String?,
    activeProductTypeFilter: ProductType?,
    activeSort: InventoryListSort,
    onTagFilterSelected: (String) -> Unit,
    onTagFilterCleared: () -> Unit,
    onProductTypeFilterSelected: (ProductType?) -> Unit,
    onSortSelected: (InventoryListSort) -> Unit,
    onAddYarnClick: () -> Unit,
    onAddFiberClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddMenuExpanded by remember {
        mutableStateOf(false)
    }
    var isFilterMenuExpanded by remember {
        mutableStateOf(false)
    }
    var isSortMenuExpanded by remember {
        mutableStateOf(false)
    }
    val isFiltered = activeTagFilter != null || activeProductTypeFilter != null

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Row {
                InventoryTagFilterMenu(
                    expanded = isFilterMenuExpanded,
                    onExpandedChange = { expanded -> isFilterMenuExpanded = expanded },
                    availableTags = availableTags,
                    activeTagFilter = activeTagFilter,
                    onTagSelected = onTagFilterSelected,
                )
                InventorySortMenu(
                    expanded = isSortMenuExpanded,
                    onExpandedChange = { expanded -> isSortMenuExpanded = expanded },
                    activeSort = activeSort,
                    onSortSelected = onSortSelected,
                )
                InventoryAddMenu(
                    expanded = isAddMenuExpanded,
                    onExpandedChange = { expanded -> isAddMenuExpanded = expanded },
                    onAddYarnClick = onAddYarnClick,
                    onAddFiberClick = onAddFiberClick,
                )
            }
        }

        InventoryProductTypeFilter(
            activeProductTypeFilter = activeProductTypeFilter,
            onProductTypeFilterSelected = onProductTypeFilterSelected,
        )

        Text(
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

        if (activeTagFilter != null) {
            AssistChip(
                onClick = onTagFilterCleared,
                label = {
                    Text(activeTagFilter)
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_clear_filter),
                    )
                },
            )
        }
    }
}

@Composable
private fun InventoryProductTypeFilter(
    activeProductTypeFilter: ProductType?,
    onProductTypeFilterSelected: (ProductType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ProductTypeFilterChip(
            selected = activeProductTypeFilter == null,
            label = stringResource(R.string.inventory_type_filter_all),
            onClick = {
                onProductTypeFilterSelected(null)
            },
        )
        ProductTypeFilterChip(
            selected = activeProductTypeFilter == ProductType.Yarn,
            label = stringResource(R.string.product_type_yarn),
            onClick = {
                onProductTypeFilterSelected(ProductType.Yarn)
            },
        )
        ProductTypeFilterChip(
            selected = activeProductTypeFilter == ProductType.Fiber,
            label = stringResource(R.string.product_type_fiber),
            onClick = {
                onProductTypeFilterSelected(ProductType.Fiber)
            },
        )
    }
}

@Composable
private fun ProductTypeFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(label)
        },
    )
}

@Composable
private fun InventoryTagFilterMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    availableTags: List<String>,
    activeTagFilter: String?,
    onTagSelected: (String) -> Unit,
) {
    Column {
        IconButton(
            enabled = availableTags.isNotEmpty(),
            onClick = {
                onExpandedChange(true)
            },
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(R.string.action_filter),
                tint = if (activeTagFilter != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            },
        ) {
            availableTags.forEach { tag ->
                DropdownMenuItem(
                    text = {
                        Text(tag)
                    },
                    leadingIcon = if (tag == activeTagFilter) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onExpandedChange(false)
                        onTagSelected(tag)
                    },
                )
            }
        }
    }
}

@Composable
private fun InventorySortMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    activeSort: InventoryListSort,
    onSortSelected: (InventoryListSort) -> Unit,
) {
    Column {
        IconButton(
            onClick = {
                onExpandedChange(true)
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.action_sort),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            },
        ) {
            InventoryListSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(sort.label())
                    },
                    leadingIcon = if (sort == activeSort) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onExpandedChange(false)
                        onSortSelected(sort)
                    },
                )
            }
        }
    }
}

@Composable
private fun InventoryListSort.label(): String {
    return stringResource(
        id = when (this) {
            InventoryListSort.UpdatedNewest -> R.string.sort_option_updated_newest
            InventoryListSort.NameAsc -> R.string.sort_option_name_asc
            InventoryListSort.TypeThenName -> R.string.sort_option_type_then_name
        },
    )
}

@Composable
private fun InventoryAddMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddYarnClick: () -> Unit,
    onAddFiberClick: () -> Unit,
) {
    Column {
        IconButton(
            onClick = {
                onExpandedChange(true)
            },
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.action_add),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            },
        ) {
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.action_add_yarn))
                },
                onClick = {
                    onExpandedChange(false)
                    onAddYarnClick()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.action_add_fiber))
                },
                onClick = {
                    onExpandedChange(false)
                    onAddFiberClick()
                },
            )
        }
    }
}

@Composable
internal fun SelectionHeader(
    selectedCount: Int,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onCancelClick,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.action_cancel),
            )
        }

        Text(
            text = stringResource(
                R.string.inventory_selection_count,
                selectedCount,
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        IconButton(
            onClick = onDeleteClick,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryListHeaderPreview() {
    WoolStashTheme {
        InventoryListHeader(
            totalItemCount = 12,
            visibleItemCount = 12,
            availableTags = listOf("natur", "socken", "spinnen"),
            activeTagFilter = null,
            activeProductTypeFilter = null,
            activeSort = InventoryListSort.UpdatedNewest,
            onTagFilterSelected = {},
            onTagFilterCleared = {},
            onProductTypeFilterSelected = {},
            onSortSelected = {},
            onAddYarnClick = {},
            onAddFiberClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilteredInventoryListHeaderPreview() {
    WoolStashTheme {
        InventoryListHeader(
            totalItemCount = 12,
            visibleItemCount = 3,
            availableTags = listOf("natur", "socken", "spinnen"),
            activeTagFilter = "socken",
            activeProductTypeFilter = ProductType.Yarn,
            activeSort = InventoryListSort.TypeThenName,
            onTagFilterSelected = {},
            onTagFilterCleared = {},
            onProductTypeFilterSelected = {},
            onSortSelected = {},
            onAddYarnClick = {},
            onAddFiberClick = {},
        )
    }
}
