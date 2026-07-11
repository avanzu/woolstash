package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import java.util.Locale

@Composable
internal fun InventoryCoreFieldsEditor(
    initialName: String,
    initialColorDescription: String?,
    initialMaterialDescription: String?,
    initialWeightGrams: Double?,
    submitLabel: String,
    onSubmit: (CreateInventoryItemInput) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var colorDescription by remember(initialColorDescription) {
        mutableStateOf(initialColorDescription.orEmpty())
    }
    var materialDescription by remember(initialMaterialDescription) {
        mutableStateOf(initialMaterialDescription.orEmpty())
    }
    var weightText by remember(initialWeightGrams) {
        mutableStateOf(initialWeightGrams?.let { grams -> WeightInputUnit.Grams.fromGrams(grams).toString() }.orEmpty())
    }
    var weightUnit by remember(initialWeightGrams) {
        mutableStateOf(WeightInputUnit.Grams)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (onCancel != null) {
                IconButton(
                    onClick = onCancel,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_cancel),
                    )
                }
            }

            Button(
                enabled = canSubmit,
                onClick = {
                    onSubmit(
                        CreateInventoryItemInput(
                            name = name,
                            colorDescription = colorDescription,
                            materialDescription = materialDescription,
                            weightGrams = normalizedWeightGrams,
                        ),
                    )
                },
            ) {
                Text(submitLabel)
            }
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
