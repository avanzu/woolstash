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
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.ui.theme.WoolStashTheme

@Composable
fun InventoryListScreen(
    items: List<InventoryItem>,
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
                        itemCount = items.size,
                        onAddYarnClick = onAddYarnClick,
                        onAddFiberClick = onAddFiberClick,
                    )
                }
            }

            items(
                items = items,
                key = { item -> item.id.value },
            ) { item ->
                InventoryItemCard(
                    item = item,
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
            onDeleteItemsConfirmed = {},
            onItemClick = {},
            onAddYarnClick = {},
            onAddFiberClick = {},
        )
    }
}
