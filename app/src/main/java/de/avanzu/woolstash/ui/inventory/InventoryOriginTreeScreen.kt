package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryOrigin

@Composable
fun InventoryOriginTreeScreen(
    rootItem: InventoryItem,
    allItems: List<InventoryItem>,
    origins: List<InventoryOrigin>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = remember(rootItem.id, allItems, origins) {
        buildInventoryOriginTree(rootItem.id, allItems, origins).flattenForDisplay()
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                OriginTreeTopBar(
                    title = stringResource(R.string.detail_section_origin_tree),
                    onBackClick = onBackClick,
                )
            },
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = rootItem.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.detail_origin_tree_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                items(
                    items = rows,
                    key = { row -> "${row.path}-${row.node.item.id.value}" },
                ) { row ->
                    OriginTreeRow(row = row)
                }
            }
        }
    }
}

@Composable
private fun OriginTreeTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun OriginTreeRow(
    row: InventoryOriginTreeDisplayRow,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (row.depth.coerceAtMost(6) * 20).dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductTypeArtwork(
                productType = row.node.item.productType,
                contentDescription = null,
                size = 40.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = row.node.item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(
                        R.string.detail_origin_tree_amounts,
                        formatGrams(row.node.consumedGrams),
                        row.node.item.weight?.let { weight -> formatGrams(weight.grams) }
                            ?: stringResource(R.string.detail_field_empty),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class InventoryOriginTreeDisplayRow(
    val node: InventoryOriginNode,
    val depth: Int,
    val path: String,
)

private fun List<InventoryOriginNode>.flattenForDisplay(
    depth: Int = 0,
    path: String = "root",
): List<InventoryOriginTreeDisplayRow> {
    return flatMapIndexed { index, node ->
        val nodePath = "$path/$index"
        listOf(
            InventoryOriginTreeDisplayRow(
                node = node,
                depth = depth,
                path = nodePath,
            ),
        ) + node.origins.flattenForDisplay(depth = depth + 1, path = nodePath)
    }
}
