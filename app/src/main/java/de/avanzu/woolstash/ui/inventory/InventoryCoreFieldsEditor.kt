package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.normalizedDistinct
import java.util.Locale

@Composable
internal fun InventoryCoreFieldsEditor(
    initialName: String,
    initialColorDescription: String?,
    initialMaterialDescription: String?,
    initialWeightGrams: Double?,
    initialLocation: String?,
    initialManufacturer: String?,
    initialPurchaseSource: String?,
    initialTags: List<Tag>,
    tagSuggestions: List<String>,
    referenceSuggestions: InventoryReferenceSuggestions,
    submitLabel: String,
    onSubmit: (CreateInventoryItemInput) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var colorDescription by rememberSaveable(initialColorDescription) {
        mutableStateOf(initialColorDescription.orEmpty())
    }
    var materialDescription by rememberSaveable(initialMaterialDescription) {
        mutableStateOf(initialMaterialDescription.orEmpty())
    }
    var location by rememberSaveable(initialLocation) {
        mutableStateOf(initialLocation.orEmpty())
    }
    var manufacturer by rememberSaveable(initialManufacturer) {
        mutableStateOf(initialManufacturer.orEmpty())
    }
    var purchaseSource by rememberSaveable(initialPurchaseSource) {
        mutableStateOf(initialPurchaseSource.orEmpty())
    }
    var weightText by rememberSaveable(initialWeightGrams) {
        mutableStateOf(initialWeightGrams?.let { grams -> WeightInputUnit.Grams.fromGrams(grams).toString() }.orEmpty())
    }
    var weightUnit by rememberSaveable(initialWeightGrams) {
        mutableStateOf(WeightInputUnit.Grams)
    }
    var tagNames by rememberSaveable(initialTags) {
        mutableStateOf(initialTags.normalizedDistinct().map { tag -> tag.name })
    }
    var tagText by rememberSaveable {
        mutableStateOf("")
    }
    val tags = tagNames.map(::Tag)

    fun addTag(input: String = tagText) {
        val tag = Tag.fromInput(input) ?: return
        tagNames = (tags + tag).normalizedDistinct().map { normalizedTag -> normalizedTag.name }
        tagText = ""
    }

    fun removeTag(tag: Tag) {
        tagNames = tags
            .filterNot { existingTag -> existingTag.name == tag.name }
            .map { remainingTag -> remainingTag.name }
    }

    val parsedWeight = weightText
        .trim()
        .replace(',', '.')
        .takeIf { value -> value.isNotEmpty() }
        ?.toDoubleOrNull()
    val isWeightValid = weightText.isBlank() || parsedWeight?.let { weight -> weight >= 0 } == true
    val normalizedWeightGrams = parsedWeight?.let { weight -> weightUnit.toGrams(weight) }
    val canSubmit = name.isNotBlank() && isWeightValid

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { value -> name = value },
            label = {
                Text(stringResource(R.string.create_field_name))
            },
            singleLine = true,
            isError = name.isBlank(),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = colorDescription,
            onValueChange = { value -> colorDescription = value },
            label = {
                Text(stringResource(R.string.detail_field_color))
            },
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = materialDescription,
            onValueChange = { value -> materialDescription = value },
            label = {
                Text(stringResource(R.string.detail_field_material))
            },
            singleLine = true,
        )

        ReferenceAutocompleteField(
            value = location,
            onValueChange = { value -> location = value },
            label = stringResource(R.string.detail_field_location),
            suggestions = referenceSuggestions.locations,
        )

        ReferenceAutocompleteField(
            value = manufacturer,
            onValueChange = { value -> manufacturer = value },
            label = stringResource(R.string.detail_field_manufacturer),
            suggestions = referenceSuggestions.manufacturers,
        )

        ReferenceAutocompleteField(
            value = purchaseSource,
            onValueChange = { value -> purchaseSource = value },
            label = stringResource(R.string.detail_field_purchase_source),
            suggestions = referenceSuggestions.purchaseSources,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = weightText,
            onValueChange = { value -> weightText = value },
            label = {
                Text(stringResource(R.string.create_field_weight_grams))
            },
            singleLine = true,
            isError = !isWeightValid,
            supportingText = if (isWeightValid) {
                null
            } else {
                {
                    Text(stringResource(R.string.create_weight_error))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = {
                UnitDropdown(
                    selectedLabel = stringResource(weightUnit.labelRes),
                    labels = WeightInputUnit.entries.map { unit -> stringResource(unit.labelRes) },
                    onSelected = { index ->
                        val newUnit = WeightInputUnit.entries[index]
                        if (parsedWeight != null && isWeightValid) {
                            weightText = newUnit.fromGrams(weightUnit.toGrams(parsedWeight)).formatForInput()
                        }
                        weightUnit = newUnit
                    },
                )
            },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = tagText,
                onValueChange = { value -> tagText = value },
                label = {
                    Text(stringResource(R.string.create_field_tags))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        addTag()
                    },
                ),
                trailingIcon = {
                    IconButton(
                        enabled = Tag.fromInput(tagText) != null,
                        onClick = {
                            addTag()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.action_add_tag),
                        )
                    }
                },
            )

            if (tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = {
                                removeTag(tag)
                            },
                            label = {
                                Text(tag.name)
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.action_remove_tag),
                                    modifier = Modifier.size(InputChipDefaults.IconSize),
                                )
                            },
                        )
                    }
                }
            }

            TagSuggestions(
                query = tagText,
                suggestions = tagSuggestions,
                selectedTags = tags,
                onSuggestionClick = { suggestion ->
                    addTag(suggestion)
                },
            )
        }

        InventoryEditorActions(
            canSubmit = canSubmit,
            submitLabel = submitLabel,
            onCancel = onCancel,
            onSubmit = {
                onSubmit(
                    CreateInventoryItemInput(
                        name = name,
                        colorDescription = colorDescription,
                        materialDescription = materialDescription,
                        weightGrams = normalizedWeightGrams,
                        location = location,
                        manufacturer = manufacturer,
                        purchaseSource = purchaseSource,
                        tags = tags,
                    ),
                )
            },
        )
    }
}

@Composable
private fun ReferenceAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
) {
    var hasFocus by remember {
        mutableStateOf(false)
    }
    var isMenuDismissed by remember {
        mutableStateOf(false)
    }
    val trimmedValue = value.trim()
    val filteredSuggestions = suggestions
        .distinct()
        .filter { suggestion ->
            trimmedValue.length >= 3 &&
                suggestion.contains(trimmedValue, ignoreCase = true)
        }
        .take(6)
    val showSuggestions = hasFocus && !isMenuDismissed && filteredSuggestions.isNotEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                    if (focusState.isFocused) {
                        isMenuDismissed = false
                    }
                },
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                isMenuDismissed = false
            },
            label = {
                Text(label)
            },
            singleLine = true,
        )

        if (showSuggestions) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    filteredSuggestions.forEachIndexed { index, suggestion ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(suggestion)
                                    isMenuDismissed = true
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            text = suggestion,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (index < filteredSuggestions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagSuggestions(
    query: String,
    suggestions: List<String>,
    selectedTags: List<Tag>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val normalizedQuery = Tag.fromInput(query)?.name.orEmpty()
    val selectedTagNames = selectedTags.map { tag -> tag.name }.toSet()
    val filteredSuggestions = suggestions
        .mapNotNull { suggestion -> Tag.fromInput(suggestion)?.name }
        .distinct()
        .filter { suggestion ->
            normalizedQuery.isNotBlank() &&
                suggestion.contains(normalizedQuery) &&
                suggestion !in selectedTagNames
        }
        .take(6)

    if (filteredSuggestions.isEmpty()) {
        return
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        filteredSuggestions.forEach { suggestion ->
            AssistChip(
                onClick = {
                    onSuggestionClick(suggestion)
                },
                label = {
                    Text(suggestion)
                },
            )
        }
    }
}

private fun Double.formatForInput(): String {
    return if (rem(1.0) == 0.0) {
        toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
    }
}

@Composable
private fun UnitDropdown(
    selectedLabel: String,
    labels: List<String>,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clickable {
                expanded = true
            }
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(selectedLabel)
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        },
    ) {
        labels.forEachIndexed { index, label ->
            DropdownMenuItem(
                text = {
                    Text(label)
                },
                onClick = {
                    expanded = false
                    onSelected(index)
                },
            )
        }
    }
}
