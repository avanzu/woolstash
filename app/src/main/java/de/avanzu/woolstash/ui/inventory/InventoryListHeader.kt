package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
    onAddYarnClick: () -> Unit,
    onAddFiberClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddMenuExpanded by remember {
        mutableStateOf(false)
    }

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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onMenuClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.action_open_navigation),
                )
            }

            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
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

            Box {
                FilledIconButton(
                    onClick = {
                        isAddMenuExpanded = true
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
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
                )
            }
        }
    }
}

@Composable
internal fun InventoryFilterDrawerContent(
    availableTags: List<String>,
    activeTagFilter: String?,
    activeProductTypeFilter: ProductType?,
    activeSort: InventoryListSort,
    onTagFilterSelected: (String) -> Unit,
    onTagFilterCleared: () -> Unit,
    onProductTypeFilterSelected: (ProductType?) -> Unit,
    onSortSelected: (InventoryListSort) -> Unit,
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
        onClick = {
            onClick(productType)
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
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            onExpandedChange(false)
        },
    ) {
        InventoryAddChoice(
            text = stringResource(R.string.action_add_yarn),
            onClick = {
                onExpandedChange(false)
                onAddYarnClick()
            },
        )
        InventoryAddChoice(
            text = stringResource(R.string.action_add_fiber),
            onClick = {
                onExpandedChange(false)
                onAddFiberClick()
            },
        )
    }
}

@Composable
private fun InventoryAddChoice(
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(text)
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
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
            onAddYarnClick = {},
            onAddFiberClick = {},
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
            onTagFilterSelected = {},
            onTagFilterCleared = {},
            onProductTypeFilterSelected = {},
            onSortSelected = {},
        )
    }
}
