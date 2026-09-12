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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.Gauge
import de.avanzu.woolstash.domain.model.Length
import de.avanzu.woolstash.domain.model.LengthBasis
import de.avanzu.woolstash.domain.model.MeasurementSource
import de.avanzu.woolstash.domain.model.NeedleSize
import de.avanzu.woolstash.domain.model.TwistDirection
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.YarnWeight
import java.util.Locale

@Composable
internal fun YarnDetailsEditor(
    details: YarnDetails,
    onSubmit: (YarnDetails) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lengthText by rememberSaveable(details) { mutableStateOf(details.length?.meters?.toString().orEmpty()) }
    var lengthUnit by rememberSaveable(details) { mutableStateOf(LengthInputUnit.Meters) }
    var lengthBasis by rememberSaveable(details) { mutableStateOf(details.lengthBasis) }
    var yarnWeight by rememberSaveable(details) { mutableStateOf(details.yarnWeight) }
    var skeinCountText by rememberSaveable(details) { mutableStateOf(details.skeinCount?.toString().orEmpty()) }
    var needleSizeText by rememberSaveable(details) {
        mutableStateOf(details.recommendedNeedleSize?.millimeters?.toString().orEmpty())
    }
    var gaugeStitchesText by rememberSaveable(details) {
        mutableStateOf(details.gauge?.stitchesPer10cm?.toString().orEmpty())
    }
    var gaugeRowsText by rememberSaveable(details) {
        mutableStateOf(details.gauge?.rowsPer10cm?.toString().orEmpty())
    }
    var gaugeNeedleText by rememberSaveable(details) {
        mutableStateOf(details.gauge?.needleSize?.millimeters?.toString().orEmpty())
    }
    var gaugeNote by rememberSaveable(details) { mutableStateOf(details.gauge?.note.orEmpty()) }
    var dyeLot by rememberSaveable(details) { mutableStateOf(details.dyeLot.orEmpty()) }
    var plyText by rememberSaveable(details) { mutableStateOf(details.ply?.toString().orEmpty()) }
    var twistDirection by rememberSaveable(details) { mutableStateOf(details.twistDirection) }

    val length = parseOptionalNonNegativeDouble(lengthText)
    val skeinCount = parseOptionalNonNegativeInt(skeinCountText)
    val needleSize = parseOptionalPositiveDouble(needleSizeText)
    val gaugeStitches = parseOptionalNonNegativeDouble(gaugeStitchesText)
    val gaugeRows = parseOptionalNonNegativeDouble(gaugeRowsText)
    val gaugeNeedle = parseOptionalPositiveDouble(gaugeNeedleText)
    val ply = parseOptionalNonNegativeInt(plyText)
    val canSave = listOf(length, skeinCount, needleSize, gaugeStitches, gaugeRows, gaugeNeedle, ply)
        .all { result -> result.isValid }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OptionalDecimalField(
            value = lengthText,
            onValueChange = { value -> lengthText = value },
            label = stringResource(R.string.detail_field_length),
            isError = !length.isValid,
            suffix = stringResource(lengthUnit.labelRes),
            suffixOptions = LengthInputUnit.entries.map { unit -> stringResource(unit.labelRes) },
            onSuffixSelected = { index ->
                val newUnit = LengthInputUnit.entries[index]
                if (length.value != null && length.isValid) {
                    lengthText = newUnit.fromMeters(lengthUnit.toMeters(length.value)).formatForInput()
                }
                lengthUnit = newUnit
            },
        )
        NullableDropdownField(
            label = stringResource(R.string.detail_field_length_basis),
            selected = lengthBasis,
            selectedLabel = { value -> value.label() },
            values = LengthBasis.entries,
            onSelected = { value -> lengthBasis = value },
        )
        NullableDropdownField(
            label = stringResource(R.string.detail_field_yarn_weight),
            selected = yarnWeight,
            selectedLabel = { value -> value.label() },
            values = YarnWeight.entries,
            onSelected = { value -> yarnWeight = value },
        )
        OptionalIntField(
            value = skeinCountText,
            onValueChange = { value -> skeinCountText = value },
            label = stringResource(R.string.detail_field_skein_count),
            isError = !skeinCount.isValid,
        )
        OptionalDecimalField(
            value = needleSizeText,
            onValueChange = { value -> needleSizeText = value },
            label = stringResource(R.string.detail_field_needle_size),
            isError = !needleSize.isValid,
            suffix = stringResource(R.string.unit_millimeters),
        )
        OptionalDecimalField(
            value = gaugeStitchesText,
            onValueChange = { value -> gaugeStitchesText = value },
            label = stringResource(R.string.detail_field_gauge_stitches),
            isError = !gaugeStitches.isValid,
            suffix = stringResource(R.string.unit_stitches_per_10cm),
        )
        OptionalDecimalField(
            value = gaugeRowsText,
            onValueChange = { value -> gaugeRowsText = value },
            label = stringResource(R.string.detail_field_gauge_rows),
            isError = !gaugeRows.isValid,
            suffix = stringResource(R.string.unit_rows_per_10cm),
        )
        OptionalDecimalField(
            value = gaugeNeedleText,
            onValueChange = { value -> gaugeNeedleText = value },
            label = stringResource(R.string.detail_field_gauge_needle),
            isError = !gaugeNeedle.isValid,
            suffix = stringResource(R.string.unit_millimeters),
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = gaugeNote,
            onValueChange = { value -> gaugeNote = value },
            label = {
                Text(stringResource(R.string.detail_field_gauge_note))
            },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = dyeLot,
            onValueChange = { value -> dyeLot = value },
            label = {
                Text(stringResource(R.string.detail_field_dye_lot))
            },
            singleLine = true,
        )
        OptionalIntField(
            value = plyText,
            onValueChange = { value -> plyText = value },
            label = stringResource(R.string.detail_field_ply),
            isError = !ply.isValid,
        )
        NullableDropdownField(
            label = stringResource(R.string.detail_field_twist_direction),
            selected = twistDirection,
            selectedLabel = { value -> value.label() },
            values = TwistDirection.entries,
            onSelected = { value -> twistDirection = value },
        )
        InventoryEditorActions(
            canSubmit = canSave,
            submitLabel = stringResource(R.string.action_save),
            onCancel = onCancel,
            onSubmit = {
                val updatedGauge = Gauge(
                    stitchesPer10cm = gaugeStitches.value,
                    rowsPer10cm = gaugeRows.value,
                    needleSize = gaugeNeedle.value?.let { millimeters -> NeedleSize(millimeters) },
                    note = gaugeNote.cleanOrNull(),
                ).takeIf { gauge ->
                    gauge.stitchesPer10cm != null ||
                        gauge.rowsPer10cm != null ||
                        gauge.needleSize != null ||
                        gauge.note != null
                }

                onSubmit(
                    details.copy(
                        length = length.value?.let { value ->
                            Length(
                                meters = lengthUnit.toMeters(value),
                                source = details.length?.source ?: MeasurementSource.Unknown,
                            )
                        },
                        lengthBasis = lengthBasis,
                        yarnWeight = yarnWeight,
                        skeinCount = skeinCount.value,
                        recommendedNeedleSize = needleSize.value?.let { millimeters -> NeedleSize(millimeters) },
                        gauge = updatedGauge,
                        dyeLot = dyeLot.cleanOrNull(),
                        ply = ply.value,
                        twistDirection = twistDirection,
                    ),
                )
            },
        )
    }
}

@Composable
internal fun FiberDetailsEditor(
    details: FiberDetails,
    onSubmit: (FiberDetails) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fiberForm by rememberSaveable(details) { mutableStateOf(details.fiberForm) }
    var preparation by rememberSaveable(details) { mutableStateOf(details.preparation) }
    var breedOrSource by rememberSaveable(details) { mutableStateOf(details.breedOrSource.orEmpty()) }
    var stapleLengthText by rememberSaveable(details) {
        mutableStateOf(
            details.stapleLength?.meters
                ?.let { meters -> LengthInputUnit.Centimeters.fromMeters(meters).toString() }
                .orEmpty(),
        )
    }
    var stapleLengthUnit by rememberSaveable(details) {
        mutableStateOf(LengthInputUnit.Centimeters)
    }
    var micronText by rememberSaveable(details) { mutableStateOf(details.micron?.toString().orEmpty()) }
    var intendedSpin by rememberSaveable(details) { mutableStateOf(details.intendedSpin.orEmpty()) }

    val stapleLength = parseOptionalNonNegativeDouble(stapleLengthText)
    val micron = parseOptionalNonNegativeDouble(micronText)
    val canSave = stapleLength.isValid && micron.isValid

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NullableDropdownField(
            label = stringResource(R.string.detail_field_fiber_form),
            selected = fiberForm,
            selectedLabel = { value -> value.label() },
            values = FiberForm.entries,
            onSelected = { value -> fiberForm = value },
        )
        NullableDropdownField(
            label = stringResource(R.string.detail_field_preparation),
            selected = preparation,
            selectedLabel = { value -> value.label() },
            values = FiberPreparation.entries,
            onSelected = { value -> preparation = value },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = breedOrSource,
            onValueChange = { value -> breedOrSource = value },
            label = {
                Text(stringResource(R.string.detail_field_breed_or_source))
            },
            singleLine = true,
        )
        OptionalDecimalField(
            value = stapleLengthText,
            onValueChange = { value -> stapleLengthText = value },
            label = stringResource(R.string.detail_field_staple_length),
            isError = !stapleLength.isValid,
            suffix = stringResource(stapleLengthUnit.labelRes),
            suffixOptions = LengthInputUnit.entries.map { unit -> stringResource(unit.labelRes) },
            onSuffixSelected = { index ->
                val newUnit = LengthInputUnit.entries[index]
                if (stapleLength.value != null && stapleLength.isValid) {
                    stapleLengthText = newUnit
                        .fromMeters(stapleLengthUnit.toMeters(stapleLength.value))
                        .formatForInput()
                }
                stapleLengthUnit = newUnit
            },
        )
        OptionalDecimalField(
            value = micronText,
            onValueChange = { value -> micronText = value },
            label = stringResource(R.string.detail_field_micron),
            isError = !micron.isValid,
            suffix = stringResource(R.string.unit_micron),
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = intendedSpin,
            onValueChange = { value -> intendedSpin = value },
            label = {
                Text(stringResource(R.string.detail_field_intended_spin))
            },
        )
        InventoryEditorActions(
            canSubmit = canSave,
            submitLabel = stringResource(R.string.action_save),
            onCancel = onCancel,
            onSubmit = {
                onSubmit(
                    details.copy(
                        fiberForm = fiberForm,
                        preparation = preparation,
                        breedOrSource = breedOrSource.cleanOrNull(),
                        stapleLength = stapleLength.value?.let { value ->
                            Length(
                                meters = stapleLengthUnit.toMeters(value),
                                source = details.stapleLength?.source ?: MeasurementSource.Unknown,
                            )
                        },
                        micron = micron.value,
                        intendedSpin = intendedSpin.cleanOrNull(),
                    ),
                )
            },
        )
    }
}

@Composable
private fun OptionalDecimalField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    suffix: String? = null,
    suffixOptions: List<String> = emptyList(),
    onSuffixSelected: ((Int) -> Unit)? = null,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        suffix = suffix?.let { suffixText ->
            {
                if (suffixOptions.isEmpty() || onSuffixSelected == null) {
                    Text(suffixText)
                } else {
                    UnitDropdown(
                        selectedLabel = suffixText,
                        labels = suffixOptions,
                        onSelected = onSuffixSelected,
                    )
                }
            }
        },
    )
}

@Composable
private fun OptionalIntField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun <T> NullableDropdownField(
    label: String,
    selected: T?,
    selectedLabel: @Composable (T) -> String,
    values: List<T>,
    onSelected: (T?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = true
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                selected?.let { value -> selectedLabel(value) }
                    ?: stringResource(R.string.detail_field_empty),
            )
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
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.detail_field_empty))
                },
                onClick = {
                    expanded = false
                    onSelected(null)
                },
            )
            values.forEach { value ->
                DropdownMenuItem(
                    text = {
                        Text(selectedLabel(value))
                    },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    },
                )
            }
        }
    }
}

private data class ParsedValue<T>(
    val value: T?,
    val isValid: Boolean,
)

private fun parseOptionalNonNegativeDouble(text: String): ParsedValue<Double> {
    return parseOptionalDouble(text) { value -> value >= 0 }
}

private fun parseOptionalPositiveDouble(text: String): ParsedValue<Double> {
    return parseOptionalDouble(text) { value -> value > 0 }
}

private fun parseOptionalDouble(
    text: String,
    predicate: (Double) -> Boolean,
): ParsedValue<Double> {
    val normalized = text.trim().replace(',', '.')
    if (normalized.isEmpty()) {
        return ParsedValue(value = null, isValid = true)
    }

    val value = normalized.toDoubleOrNull()
    return ParsedValue(
        value = value,
        isValid = value?.let(predicate) == true,
    )
}

private fun parseOptionalNonNegativeInt(text: String): ParsedValue<Int> {
    val normalized = text.trim()
    if (normalized.isEmpty()) {
        return ParsedValue(value = null, isValid = true)
    }

    val value = normalized.toIntOrNull()
    return ParsedValue(
        value = value,
        isValid = value?.let { number -> number >= 0 } == true,
    )
}

private fun String.cleanOrNull(): String? {
    return trim().takeIf { value -> value.isNotEmpty() }
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
