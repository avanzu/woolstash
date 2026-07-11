package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.data.media.InventoryPhotoFile
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.ProductDetails
import de.avanzu.woolstash.domain.model.ProductType

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun InventoryItemCard(
    item: InventoryItem,
    photoPreview: InventoryPhotoFile?,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                )
            }

            InventoryItemThumbnail(
                item = item,
                photoPreview = photoPreview,
            )

            Column(
                modifier = Modifier.weight(1f),
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

                ProductDetailsLine(
                    details = item.details,
                )

                if (item.tags.isNotEmpty()) {
                    TagRow(
                        tags = item.tags.map { tag -> tag.name },
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryItemThumbnail(
    item: InventoryItem,
    photoPreview: InventoryPhotoFile?,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (photoPreview != null) {
            LocalPhotoImage(
                file = photoPreview.thumbnailFile,
                contentDescription = stringResource(R.string.inventory_item_thumbnail_content_description),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(item.productType.placeholderDrawableRes()),
                contentDescription = stringResource(R.string.inventory_item_thumbnail_placeholder_content_description),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun ProductType.placeholderDrawableRes(): Int {
    return when (this) {
        ProductType.Yarn -> R.drawable.placeholder_yarn
        ProductType.Fiber -> R.drawable.placeholder_fiber
    }
}

@Composable
private fun ProductDetailsLine(
    details: ProductDetails,
    modifier: Modifier = Modifier,
) {
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
