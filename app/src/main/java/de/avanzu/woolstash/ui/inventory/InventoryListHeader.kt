package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R

@Composable
internal fun InventoryListHeader(
    itemCount: Int,
    onAddYarnClick: () -> Unit,
    onAddFiberClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddMenuExpanded by remember {
        mutableStateOf(false)
    }

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
            InventoryAddMenu(
                expanded = isAddMenuExpanded,
                onExpandedChange = { expanded -> isAddMenuExpanded = expanded },
                onAddYarnClick = onAddYarnClick,
                onAddFiberClick = onAddFiberClick,
            )
        }

        Text(
            text = stringResource(
                R.string.inventory_item_count,
                itemCount,
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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
