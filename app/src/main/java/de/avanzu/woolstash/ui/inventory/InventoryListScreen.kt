package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.ui.theme.WoolStashTheme

@Composable
fun InventoryListScreen(
    items: List<InventoryItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                InventoryListHeader(
                    itemCount = items.size,
                )
            }

            items(
                items = items,
                key = { item -> item.id.value },
            ) { item ->
                InventoryItemCard(
                    item = item,
                )
            }
        }
    }
}

@Composable
private fun InventoryListHeader(
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    ) {
        Text(
            text = "Wool Stash Companion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "$itemCount Einträge",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = item.summaryLine(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (item.tags.isNotEmpty()) {
                TagRow(
                    tags = item.tags.map { tag -> tag.name },
                )
            }
        }
    }
}

@Composable
private fun TagRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            AssistChip(
                onClick = {
                    // Filtering comes later.
                },
                label = {
                    Text(text = "#$tag")
                },
            )
        }
    }
}

private fun InventoryItem.summaryLine(): String {
    val parts = listOfNotNull(
        productTypeLabel(),
        colorDescription,
        materialDescription,
        weight?.let { weight -> "${weight.grams.toInt()} g" },
        location,
    )

    return parts.joinToString(separator = " · ")
}

private fun InventoryItem.productTypeLabel(): String {
    return when (details) {
        is YarnDetails -> "Garn"
        is FiberDetails -> "Spinnfaser"
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryListScreenPreview() {
    WoolStashTheme {
        InventoryListScreen(
            items = SampleInventoryItems.items,
        )
    }
}