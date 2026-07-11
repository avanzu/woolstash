package de.avanzu.woolstash.ui.inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.data.media.InventoryPhotoFile
import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.Length
import de.avanzu.woolstash.domain.model.ProductDetails
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.domain.model.Weight
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.ui.theme.WoolStashTheme

@Composable
fun InventoryItemDetailScreen(
    item: InventoryItem,
    photos: List<InventoryPhotoFile>,
    isImportingPhoto: Boolean,
    photoImportError: String?,
    onBackClick: () -> Unit,
    onUpdateCoreFields: (InventoryItem, CreateInventoryItemInput, () -> Unit) -> Unit,
    onUpdateProductDetails: (InventoryItem, ProductDetails, () -> Unit) -> Unit,
    tagSuggestions: List<String>,
    onPhotoSelected: (Uri) -> Unit,
    onSetHeroPhoto: (InventoryPhotoFile) -> Unit,
    onDeletePhoto: (InventoryPhotoFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingSection by remember(item.id) {
        mutableStateOf<InventoryDetailSection?>(null)
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let(onPhotoSelected)
        },
    )

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                DetailTopBar(
                    onBackClick = onBackClick,
                )
            }

            item {
                PhotoSection(
                    photos = photos,
                    isImporting = isImportingPhoto,
                    importError = photoImportError,
                    onAddPhotoClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onSetHeroPhoto = onSetHeroPhoto,
                    onDeletePhoto = onDeletePhoto,
                )
            }

            item {
                DetailTitleSection(
                    item = item,
                )
            }

            item {
                OverviewSection(
                    item = item,
                    isEditing = editingSection == InventoryDetailSection.Overview,
                    onEdit = {
                        editingSection = InventoryDetailSection.Overview
                    },
                    onCancelEdit = {
                        editingSection = null
                    },
                    onSave = { input ->
                        onUpdateCoreFields(item, input) {
                            editingSection = null
                        }
                    },
                    tagSuggestions = tagSuggestions,
                )
            }

            item {
                ProductDetailsSection(
                    details = item.details,
                    isEditing = editingSection == InventoryDetailSection.ProductDetails,
                    onEdit = {
                        editingSection = InventoryDetailSection.ProductDetails
                    },
                    onCancelEdit = {
                        editingSection = null
                    },
                    onSave = { details ->
                        onUpdateProductDetails(item, details) {
                            editingSection = null
                        }
                    },
                )
            }

            if (item.tags.isNotEmpty() && editingSection != InventoryDetailSection.Overview) {
                item {
                    DetailSection(
                        title = stringResource(R.string.detail_section_tags),
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            item.tags.forEach { tag ->
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(tag.name)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (!item.notes.isNullOrBlank()) {
                item {
                    DetailSection(
                        title = stringResource(R.string.detail_section_notes),
                    ) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
            )
        }
    }
}

@Composable
private fun PhotoSection(
    photos: List<InventoryPhotoFile>,
    isImporting: Boolean,
    importError: String?,
    onAddPhotoClick: () -> Unit,
    onSetHeroPhoto: (InventoryPhotoFile) -> Unit,
    onDeletePhoto: (InventoryPhotoFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    var photoPendingDeletion by remember {
        mutableStateOf<InventoryPhotoFile?>(null)
    }
    var selectedPhotoId by remember {
        mutableStateOf<String?>(null)
    }
    val selectedPhoto = photos.firstOrNull { photo ->
        photo.photoId.value == selectedPhotoId
    } ?: photos.firstOrNull()

    photoPendingDeletion?.let { photo ->
        AlertDialog(
            onDismissRequest = {
                photoPendingDeletion = null
            },
            title = {
                Text(stringResource(R.string.detail_photo_delete_dialog_title))
            },
            text = {
                Text(stringResource(R.string.detail_photo_delete_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        photoPendingDeletion = null
                        onDeletePhoto(photo)
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        photoPendingDeletion = null
                    },
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (selectedPhoto == null) {
                Text(
                    text = stringResource(R.string.detail_photo_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LocalPhotoImage(
                    file = selectedPhoto.displayFile,
                    contentDescription = stringResource(R.string.detail_photo_content_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.detail_photo_count, photos.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(
                onClick = onAddPhotoClick,
                enabled = !isImporting,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.detail_photo_add_from_gallery),
                )
            }
        }

        if (isImporting) {
            Text(
                text = stringResource(R.string.detail_photo_importing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (selectedPhoto != null) {
            SelectedPhotoToolbar(
                selectedPhoto = selectedPhoto,
                onSetHeroPhoto = onSetHeroPhoto,
                onDeletePhotoClick = {
                    photoPendingDeletion = selectedPhoto
                },
            )
        }

        if (photos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = photos,
                    key = { photo -> photo.photoId.value },
                ) { photo ->
                    PhotoThumbnail(
                        photo = photo,
                        isSelected = photo.photoId == selectedPhoto?.photoId,
                        onClick = {
                            selectedPhotoId = photo.photoId.value
                        },
                    )
                }
            }
        }

        if (!importError.isNullOrBlank()) {
            Text(
                text = importError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SelectedPhotoToolbar(
    selectedPhoto: InventoryPhotoFile,
    onSetHeroPhoto: (InventoryPhotoFile) -> Unit,
    onDeletePhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            enabled = !selectedPhoto.isHero,
            onClick = {
                onSetHeroPhoto(selectedPhoto)
            },
        ) {
            Icon(
                imageVector = if (selectedPhoto.isHero) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (selectedPhoto.isHero) {
                    stringResource(R.string.detail_photo_is_hero)
                } else {
                    stringResource(R.string.detail_photo_set_hero)
                },
                tint = if (selectedPhoto.isHero) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        IconButton(
            onClick = onDeletePhotoClick,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: InventoryPhotoFile,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        LocalPhotoImage(
            file = photo.thumbnailFile,
            contentDescription = stringResource(R.string.detail_photo_content_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape,
                    ),
            )
        }
    }
}

@Composable
private fun DetailTitleSection(
    item: InventoryItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = item.summaryLine(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OverviewSection(
    item: InventoryItem,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: (CreateInventoryItemInput) -> Unit,
    tagSuggestions: List<String>,
) {
    EditableDetailSection(
        title = stringResource(R.string.detail_section_overview),
        onEdit = onEdit,
        showEditAction = !isEditing,
    ) {
        if (isEditing) {
            InventoryCoreFieldsEditor(
                initialName = item.name,
                initialColorDescription = item.colorDescription,
                initialMaterialDescription = item.materialDescription,
                initialWeightGrams = item.weight?.grams,
                initialTags = item.tags,
                tagSuggestions = tagSuggestions,
                submitLabel = stringResource(R.string.action_save),
                onSubmit = onSave,
                onCancel = onCancelEdit,
            )
        } else {
            DetailField(
                label = stringResource(R.string.detail_field_product_type),
                value = item.productTypeLabel(),
            )
            DetailField(
                label = stringResource(R.string.detail_field_color),
                value = item.colorDescription,
            )
            DetailField(
                label = stringResource(R.string.detail_field_material),
                value = item.materialDescription,
            )
            DetailField(
                label = stringResource(R.string.detail_field_weight),
                value = item.weight?.displayText(),
            )
            DetailField(
                label = stringResource(R.string.detail_field_location),
                value = item.location,
            )
            DetailField(
                label = stringResource(R.string.detail_field_status),
                value = item.status.label(),
            )
            DetailField(
                label = stringResource(R.string.detail_field_created_at),
                value = item.createdAt.displayDate(),
            )
            DetailField(
                label = stringResource(R.string.detail_field_updated_at),
                value = item.updatedAt.displayDate(),
            )
        }
    }
}

@Composable
private fun ProductDetailsSection(
    details: ProductDetails,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: (ProductDetails) -> Unit,
    modifier: Modifier = Modifier,
) {
    EditableDetailSection(
        title = stringResource(R.string.detail_section_product_details),
        onEdit = onEdit,
        modifier = modifier,
        showEditAction = !isEditing,
    ) {
        if (isEditing) {
            when (details) {
                is YarnDetails -> YarnDetailsEditor(
                    details = details,
                    onSubmit = onSave,
                    onCancel = onCancelEdit,
                )

                is FiberDetails -> FiberDetailsEditor(
                    details = details,
                    onSubmit = onSave,
                    onCancel = onCancelEdit,
                )
            }
        } else {
            when (details) {
                is YarnDetails -> YarnDetailsFields(details)
                is FiberDetails -> FiberDetailsFields(details)
            }
        }
    }
}

@Composable
private fun YarnDetailsFields(details: YarnDetails) {
    DetailField(
        label = stringResource(R.string.detail_field_length),
        value = details.length?.displayText(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_length_basis),
        value = details.lengthBasis?.label(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_yarn_weight),
        value = details.yarnWeight?.label(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_skein_count),
        value = details.skeinCount?.toString(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_needle_size),
        value = details.recommendedNeedleSize?.let { needleSize ->
            formatNeedleSize(needleSize.millimeters)
        },
    )
    DetailField(
        label = stringResource(R.string.detail_field_gauge),
        value = details.gauge?.summaryLine(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_dye_lot),
        value = details.dyeLot,
    )
    DetailField(
        label = stringResource(R.string.detail_field_ply),
        value = details.ply?.toString(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_twist_direction),
        value = details.twistDirection?.label(),
    )
}

@Composable
private fun FiberDetailsFields(details: FiberDetails) {
    DetailField(
        label = stringResource(R.string.detail_field_fiber_form),
        value = details.fiberForm?.label(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_preparation),
        value = details.preparation?.label(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_breed_or_source),
        value = details.breedOrSource,
    )
    DetailField(
        label = stringResource(R.string.detail_field_staple_length),
        value = details.stapleLength?.displayText(),
    )
    DetailField(
        label = stringResource(R.string.detail_field_micron),
        value = details.micron?.let { micron -> stringResource(R.string.format_micron, micron) },
    )
    DetailField(
        label = stringResource(R.string.detail_field_intended_spin),
        value = details.intendedSpin,
    )
}

@Composable
private fun Weight.displayText(): String {
    return stringResource(
        R.string.detail_measurement_with_source,
        formatGrams(grams),
        source.label(),
    )
}

@Composable
private fun Length.displayText(): String {
    return stringResource(
        R.string.detail_measurement_with_source,
        formatMeters(meters),
        source.label(),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditableDetailSection(
    title: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    showEditAction: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = showEditAction,
                onClick = {},
                onLongClick = onEdit,
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (showEditAction) {
                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                    )
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
        HorizontalDivider()
    }
}

@Composable
private fun DetailSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
        HorizontalDivider()
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    if (value.isNullOrBlank()) {
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            modifier = Modifier.weight(0.42f),
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.weight(0.58f),
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryItemDetailScreenPreview() {
    WoolStashTheme {
        InventoryItemDetailScreen(
            item = SampleInventoryItems.items.first(),
            photos = emptyList(),
            isImportingPhoto = false,
            photoImportError = null,
            onBackClick = {},
            onUpdateCoreFields = { _, _, onUpdated -> onUpdated() },
            onUpdateProductDetails = { _, _, onUpdated -> onUpdated() },
            tagSuggestions = listOf("socken", "natur"),
            onPhotoSelected = {},
            onSetHeroPhoto = {},
            onDeletePhoto = {},
        )
    }
}

private enum class InventoryDetailSection {
    Overview,
    ProductDetails,
}
