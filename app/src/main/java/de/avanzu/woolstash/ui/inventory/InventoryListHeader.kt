package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
internal fun InventoryListTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(R.string.action_open_navigation),
                    )
                }
            }

            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(
                            onClick = {
                                onSearchQueryChange("")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_clear_search),
                            )
                        }
                    }
                } else {
                    null
                },
                placeholder = {
                    Text(stringResource(R.string.inventory_search_placeholder))
                },
            )
        }
    }
}

@Composable
internal fun InventoryFilterDrawerContent(
    availableTags: List<String>,
    activeTagFilter: String?,
    activeProductTypeFilter: ProductType?,
    activeSort: InventoryListSort,
    includeDepletedItems: Boolean,
    isStagingActive: Boolean,
    isBackupBusy: Boolean,
    onTagFilterSelected: (String) -> Unit,
    onTagFilterCleared: () -> Unit,
    onProductTypeFilterSelected: (ProductType?) -> Unit,
    onSortSelected: (InventoryListSort) -> Unit,
    onIncludeDepletedItemsChange: (Boolean) -> Unit,
    onCreateBackupClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTagSectionExpanded by remember(activeTagFilter) {
        mutableStateOf(activeTagFilter != null)
    }

    ModalDrawerSheet(
        modifier = modifier.widthIn(max = 320.dp),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.action_filter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            DrawerSectionTitle(text = stringResource(R.string.drawer_section_product_type))
            ProductTypeDrawerItem(
                productType = null,
                label = stringResource(R.string.inventory_type_filter_all),
                selected = activeProductTypeFilter == null,
                onClick = onProductTypeFilterSelected,
            )

            ProductTypeDrawerItem(
                productType = ProductType.Yarn,
                label = stringResource(R.string.product_type_yarn),
                selected = activeProductTypeFilter == ProductType.Yarn,
                onClick = onProductTypeFilterSelected,
            )

            ProductTypeDrawerItem(
                productType = ProductType.Fiber,
                label = stringResource(R.string.product_type_fiber),
                selected = activeProductTypeFilter == ProductType.Fiber,
                onClick = onProductTypeFilterSelected,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onIncludeDepletedItemsChange(!includeDepletedItems)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.inventory_show_depleted),
                )
                Switch(
                    checked = includeDepletedItems,
                    onCheckedChange = onIncludeDepletedItemsChange,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DrawerSectionTitle(text = stringResource(R.string.drawer_section_sorting))
            InventoryListSort.entries.forEach { sort ->
                NavigationDrawerItem(
                    label = {
                        Text(sort.label())
                    },
                    selected = sort == activeSort,
                    icon = if (sort == activeSort) {
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
                        onSortSelected(sort)
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DrawerExpandableSectionTitle(
                text = stringResource(R.string.drawer_section_tags),
                expanded = isTagSectionExpanded,
                onClick = {
                    isTagSectionExpanded = !isTagSectionExpanded
                },
            )

            if (isTagSectionExpanded) {
                NavigationDrawerItem(
                    label = {
                        Text(stringResource(R.string.inventory_tag_filter_all))
                    },
                    selected = activeTagFilter == null,
                    onClick = onTagFilterCleared,
                )
                if (availableTags.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = stringResource(R.string.inventory_tag_filter_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    availableTags.forEach { tag ->
                        NavigationDrawerItem(
                            label = {
                                Text(tag)
                            },
                            selected = tag == activeTagFilter,
                            onClick = {
                                onTagFilterSelected(tag)
                            },
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DrawerSectionTitle(text = stringResource(R.string.drawer_section_backup))
            NavigationDrawerItem(
                label = {
                    Text(stringResource(R.string.backup_action_create))
                },
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                    )
                },
                onClick = {
                    if (!isStagingActive && !isBackupBusy) {
                        onCreateBackupClick()
                    }
                },
            )
            NavigationDrawerItem(
                label = {
                    Text(stringResource(R.string.backup_action_restore))
                },
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                    )
                },
                onClick = {
                    if (!isStagingActive && !isBackupBusy) {
                        onRestoreBackupClick()
                    }
                },
            )
        }
    }
}

@Composable
private fun DrawerSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DrawerExpandableSectionTitle(
    text: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = if (expanded) {
                Icons.Default.ExpandLess
            } else {
                Icons.Default.ExpandMore
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProductTypeDrawerItem(
    productType: ProductType?,
    label: String,
    selected: Boolean,
    onClick: (ProductType?) -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Text(label)
        },
        selected = selected,
        icon = productType?.let { type ->
            {
                ProductTypeArtwork(
                    productType = type,
                    contentDescription = null,
                    size = 32.dp,
                )
            }
        },
        onClick = {
            onClick(productType)
        },
    )
}

@Composable
internal fun InventoryAddMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddYarnClick: () -> Unit,
    onAddFiberClick: () -> Unit,
    onAddYarnFromStockClick: () -> Unit,
    onAddFiberFromStockClick: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            onExpandedChange(false)
        },
    ) {
        InventoryAddChoice(
            productType = ProductType.Yarn,
            text = stringResource(R.string.action_add_yarn),
            onClick = {
                onExpandedChange(false)
                onAddYarnClick()
            },
        )
        InventoryAddChoice(
            productType = ProductType.Fiber,
            text = stringResource(R.string.action_add_fiber),
            onClick = {
                onExpandedChange(false)
                onAddFiberClick()
            },
        )
        HorizontalDivider()
        InventoryAddChoice(
            productType = ProductType.Yarn,
            text = stringResource(R.string.action_add_yarn_from_stock),
            onClick = {
                onExpandedChange(false)
                onAddYarnFromStockClick()
            },
        )
        InventoryAddChoice(
            productType = ProductType.Fiber,
            text = stringResource(R.string.action_add_fiber_from_stock),
            onClick = {
                onExpandedChange(false)
                onAddFiberFromStockClick()
            },
        )
    }
}

@Composable
private fun InventoryAddChoice(
    productType: ProductType,
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(text)
        },
        leadingIcon = {
            ProductTypeArtwork(
                productType = productType,
                contentDescription = null,
                size = 36.dp,
            )
        },
        onClick = onClick,
    )
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
internal fun SelectionHeader(
    selectedCount: Int,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
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
}

@Preview(showBackground = true)
@Composable
private fun InventoryListTopAppBarPreview() {
    WoolStashTheme {
        InventoryListTopAppBar(
            searchQuery = "socken",
            onSearchQueryChange = {},
            onMenuClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryFilterDrawerContentPreview() {
    WoolStashTheme {
        InventoryFilterDrawerContent(
            availableTags = listOf("natur", "socken", "spinnen"),
            activeTagFilter = "socken",
            activeProductTypeFilter = ProductType.Yarn,
            activeSort = InventoryListSort.TypeThenName,
            includeDepletedItems = false,
            isStagingActive = false,
            isBackupBusy = false,
            onTagFilterSelected = {},
            onTagFilterCleared = {},
            onProductTypeFilterSelected = {},
            onSortSelected = {},
            onIncludeDepletedItemsChange = {},
            onCreateBackupClick = {},
            onRestoreBackupClick = {},
        )
    }
}
