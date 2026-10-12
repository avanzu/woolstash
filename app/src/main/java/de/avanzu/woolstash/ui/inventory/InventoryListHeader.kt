package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
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
    activeFilterCount: Int,
    activeSort: InventoryListSort,
    onSearchQueryChange: (String) -> Unit,
    onSearchFiltersClick: () -> Unit,
    onSortSelected: (InventoryListSortField) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSortMenuExpanded by remember {
        mutableStateOf(false)
    }

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
                        imageVector = Icons.Default.MoreVert,
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
                    IconButton(onClick = onSearchFiltersClick) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge {
                                        Text(activeFilterCount.toString())
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.action_open_search_filters),
                            )
                        }
                    }
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (searchQuery.isNotBlank()) {
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
                        Box {
                            IconButton(
                                onClick = { isSortMenuExpanded = true },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = stringResource(R.string.action_sort),
                                )
                            }
                            InventorySortMenu(
                                expanded = isSortMenuExpanded,
                                activeSort = activeSort,
                                onDismiss = { isSortMenuExpanded = false },
                                onSortSelected = { field ->
                                    isSortMenuExpanded = false
                                    onSortSelected(field)
                                },
                            )
                        }
                    }
                },
                placeholder = {
                    Text(stringResource(R.string.inventory_search_placeholder))
                },
            )
        }
    }
}

@Composable
internal fun InventoryDrawerContent(
    isStagingActive: Boolean,
    isBackupBusy: Boolean,
    onCreateBackupClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = stringResource(R.string.drawer_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

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

            DrawerSectionTitle(text = stringResource(R.string.drawer_section_information))
            NavigationDrawerItem(
                label = {
                    Text(stringResource(R.string.privacy_title))
                },
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                    )
                },
                onClick = onPrivacyClick,
            )
        }
    }
}

@Composable
private fun InventorySortMenu(
    expanded: Boolean,
    activeSort: InventoryListSort,
    onDismiss: () -> Unit,
    onSortSelected: (InventoryListSortField) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        InventoryListSortField.entries.forEach { field ->
            val isSelected = field == activeSort.field
            DropdownMenuItem(
                text = { Text(field.label()) },
                trailingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = when (activeSort.direction) {
                                InventoryListSortDirection.Ascending -> Icons.Default.ArrowUpward
                                InventoryListSortDirection.Descending -> Icons.Default.ArrowDownward
                            },
                            contentDescription = stringResource(
                                when (activeSort.direction) {
                                    InventoryListSortDirection.Ascending -> R.string.sort_direction_ascending
                                    InventoryListSortDirection.Descending -> R.string.sort_direction_descending
                                },
                            ),
                        )
                    }
                } else {
                    null
                },
                onClick = { onSortSelected(field) },
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
private fun InventoryListSortField.label(): String {
    return stringResource(
        id = when (this) {
            InventoryListSortField.Updated -> R.string.sort_option_updated
            InventoryListSortField.Created -> R.string.sort_option_created
            InventoryListSortField.Name -> R.string.sort_option_name
            InventoryListSortField.Weight -> R.string.detail_field_weight
            InventoryListSortField.Location -> R.string.detail_field_location
            InventoryListSortField.Color -> R.string.detail_field_color
            InventoryListSortField.Material -> R.string.detail_field_material
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
            activeFilterCount = 2,
            activeSort = InventoryListSort(
                field = InventoryListSortField.Location,
                direction = InventoryListSortDirection.Ascending,
            ),
            onSearchQueryChange = {},
            onSearchFiltersClick = {},
            onSortSelected = {},
            onMenuClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryDrawerContentPreview() {
    WoolStashTheme {
        InventoryDrawerContent(
            isStagingActive = false,
            isBackupBusy = false,
            onCreateBackupClick = {},
            onRestoreBackupClick = {},
            onPrivacyClick = {},
        )
    }
}
