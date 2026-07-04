package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.ProductDetails
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.YarnWeight
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
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 24.dp),
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
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = stringResource(R.string.number_of_entries, itemCount),
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

            ProductDetailsLine(details = item.details)

            if (item.tags.isNotEmpty()) {
                TagRow(
                    tags = item.tags.map { tag -> tag.name },
                )
            }
        }
    }
}

@Composable
private fun ProductDetailsLine(details: ProductDetails, modifier: Modifier = Modifier) {
    val text = details.detailSummaryLine()
    if (text.isNotBlank()) {
        Text(
            modifier = modifier,
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
                    Text(tag)
                },
            )
        }
    }
}
@Composable
private fun formatMeters(meters: Double) : String {
    return stringResource(id = R.string.format_length_meters, meters.toInt())
}

@Composable
private fun formatGrams(grams: Double) : String {
    return stringResource(id = R.string.format_weight_grams, grams.toInt())
}
@Composable
private fun formatNeedleSize(millimeters: Double) : String {
    return stringResource(id = R.string.format_needle_size, millimeters)
}

@Composable
private fun InventoryItem.summaryLine(): String {
    val parts = listOfNotNull(
        productTypeLabel(),
        colorDescription,
        materialDescription,
        weight?.let { weight -> formatGrams(weight.grams) },
        location,
    )

    return parts.joinToString(separator = " · ")
}
@Composable
private fun InventoryItem.productTypeLabel(): String {

    return stringResource(
        id = when(details) {
            is YarnDetails -> R.string.product_type_yarn
            is FiberDetails -> R.string.product_type_fiber
        }
    )

}
@Composable
private fun ProductDetails.detailSummaryLine() : String {
    return when(this) {
        is YarnDetails -> yarnSummaryLine()
        is FiberDetails -> fiberDetailsLine()
    }
}
@Composable
private fun YarnDetails.yarnSummaryLine() : String {
    val parts = listOfNotNull(
        length?.let { length -> formatMeters(length.meters)},
        recommendedNeedleSize?.let { recommendedNeedleSize -> formatNeedleSize(recommendedNeedleSize.millimeters) },
        yarnWeight?.label()
    )

    return parts.joinToString(separator = " · ")

}
@Composable
private fun YarnWeight.label() : String {
    return stringResource(
        id = when(this) {
            YarnWeight.DK -> R.string.yarn_weight_dk
            YarnWeight.Aran -> R.string.yarn_weight_aran
            YarnWeight.Lace -> R.string.yarn_weight_lace
            YarnWeight.Bulky -> R.string.yarn_weight_bulky
            YarnWeight.Sport -> R.string.yarn_weight_sport
            YarnWeight.Unknown -> R.string.yarn_weight_unknown
            YarnWeight.Worsted -> R.string.yarn_weight_worsted
            YarnWeight.Fingering -> R.string.yarn_weight_fingering
            YarnWeight.SuperBulky -> R.string.yarn_weight_super_bulky
        }
    )
}
@Composable
private fun FiberDetails.fiberDetailsLine() : String {
    val parts = listOfNotNull(
        fiberForm?.label(),
        preparation?.label(),
        intendedSpin
    )

    return parts.joinToString(separator = " · ")
}
@Composable
private fun FiberForm.label() : String {
    return stringResource(
        id = when(this) {
            FiberForm.Top -> R.string.fiber_form_top
            FiberForm.Unknown -> R.string.fiber_form_unknown
            FiberForm.Batt -> R.string.fiber_form_batt
            FiberForm.Fleece -> R.string.fiber_form_fleece
            FiberForm.Roving -> R.string.fiber_form_roving
            FiberForm.Locks -> R.string.fiber_form_locks
            FiberForm.Rolag -> R.string.fiber_form_rolag
            FiberForm.Other -> R.string.fiber_form_other
        }
    )
}

@Composable
private fun FiberPreparation.label() : String {
    return stringResource(
        id = when(this) {
            FiberPreparation.Raw -> R.string.fiber_preparation_raw
            FiberPreparation.Dyed -> R.string.fiber_preparation_dyed
            FiberPreparation.Other -> R.string.fiber_preparation_other
            FiberPreparation.Carded -> R.string.fiber_preparation_carded
            FiberPreparation.Combed -> R.string.fiber_preparation_combed
            FiberPreparation.Washed -> R.string.fiber_preparation_washed
            FiberPreparation.Blended -> R.string.fiber_preparation_blended
            FiberPreparation.Natural -> R.string.fiber_preparation_natural
            FiberPreparation.Unknown -> R.string.fiber_preparation_unknown
        }
    )
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