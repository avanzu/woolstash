package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.data.media.InventoryPhotoFile
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemStatus
import de.avanzu.woolstash.domain.model.ProductDetails

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
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                )
            }

            InventoryItemThumbnail(item = item, photoPreview = photoPreview)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProductTypePill(productType = item.productType)
                    StatusPill(status = item.status)
                }

                InventorySupportingText(item = item)

                ProductDetailsLine(
                    details = item.details,
                )

                if (item.tags.isNotEmpty()) {
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
    if (photoPreview != null) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.small,
            color = item.productType.containerColor(),
        ) {
            LocalPhotoImage(
                file = photoPreview.thumbnailFile,
                contentDescription = stringResource(R.string.inventory_item_thumbnail_content_description),
                modifier = Modifier.size(76.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        }
    } else {
        ProductTypeArtwork(
            productType = item.productType,
            contentDescription = stringResource(R.string.inventory_item_thumbnail_placeholder_content_description),
            modifier = modifier,
        )
    }
}

@Composable
internal fun StatusPill(
    status: InventoryItemStatus,
    modifier: Modifier = Modifier,
) {
    val isActive = status == InventoryItemStatus.Active
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = if (isActive) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (isActive) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            text = status.label(),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
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
            maxLines = 1,
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
            maxLines = 1,
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
    val visibleTagCount = 2
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
