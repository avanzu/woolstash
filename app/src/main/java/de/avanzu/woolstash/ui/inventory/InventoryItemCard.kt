package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var areTagsExpanded by rememberSaveable(item.id.value) {
        mutableStateOf(false)
    }

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
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = item.productTypeLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                InventorySupportingText(item = item)

                ProductDetailsLine(
                    details = item.details,
                )

                if (item.tags.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    TagRow(
                        tags = item.tags.map { tag -> tag.name },
                        isExpanded = areTagsExpanded,
                        onExpandedChange = { areExpanded ->
                            areTagsExpanded = areExpanded
                        },
                        onTagClick = onTagClick,
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
private fun InventorySupportingText(
    item: InventoryItem,
    modifier: Modifier = Modifier,
) {
    val text = item.summaryLine()
    if (text.isNotBlank()) {
        Text(
            modifier = modifier,
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TagRow(
    tags: List<String>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleTagCount = 3
    val visibleTags = if (isExpanded) {
        tags
    } else {
        tags.take(visibleTagCount)
    }
    val hiddenTagCount = tags.size - visibleTags.size

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        visibleTags.forEach { tag ->
            InventoryTag(
                text = tag,
                onClick = { onTagClick(tag) },
            )
        }

        if (hiddenTagCount > 0) {
            InventoryTag(
                text = stringResource(R.string.inventory_tags_show_more, hiddenTagCount),
                onClick = { onExpandedChange(true) },
            )
        } else if (isExpanded && tags.size > visibleTagCount) {
            InventoryTag(
                text = stringResource(R.string.inventory_tags_show_less),
                onClick = { onExpandedChange(false) },
            )
        }
    }
}

@Composable
private fun InventoryTag(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
