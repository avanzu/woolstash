package de.avanzu.woolstash.ui.inventory

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.Gauge
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemStatus
import de.avanzu.woolstash.domain.model.LengthBasis
import de.avanzu.woolstash.domain.model.MeasurementSource
import de.avanzu.woolstash.domain.model.ProductDetails
import de.avanzu.woolstash.domain.model.TwistDirection
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.YarnWeight
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun InventoryItem.summaryLine(): String {
    val parts = listOfNotNull(
        colorDescription,
        materialDescription,
        weight?.let { weight -> formatGrams(weight.grams) },
        location,
        manufacturer,
    )

    return parts.joinToString(separator = ", ")
}

@Composable
internal fun ProductDetails.detailSummaryLine(): String {
    return when (this) {
        is YarnDetails -> yarnSummaryLine()
        is FiberDetails -> fiberDetailsLine()
    }
}

@Composable
internal fun InventoryItem.productTypeLabel(): String {
    return stringResource(
        id = when (details) {
            is YarnDetails -> R.string.product_type_yarn
            is FiberDetails -> R.string.product_type_fiber
        },
    )
}

@Composable
private fun YarnDetails.yarnSummaryLine(): String {
    val parts = listOfNotNull(
        length?.let { length -> formatMeters(length.meters) },
        recommendedNeedleSize?.let { needleSize -> formatNeedleSize(needleSize.millimeters) },
        yarnWeight?.label(),
    )

    return parts.joinToString(separator = ", ")
}

@Composable
private fun FiberDetails.fiberDetailsLine(): String {
    val parts = listOfNotNull(
        fiberForm?.label(),
        preparation?.label(),
        intendedSpin,
    )

    return parts.joinToString(separator = ", ")
}

@Composable
internal fun YarnWeight.label(): String {
    return stringResource(
        id = when (this) {
            YarnWeight.DK -> R.string.yarn_weight_dk
            YarnWeight.Aran -> R.string.yarn_weight_aran
            YarnWeight.Lace -> R.string.yarn_weight_lace
            YarnWeight.Bulky -> R.string.yarn_weight_bulky
            YarnWeight.Sport -> R.string.yarn_weight_sport
            YarnWeight.Unknown -> R.string.yarn_weight_unknown
            YarnWeight.Worsted -> R.string.yarn_weight_worsted
            YarnWeight.Fingering -> R.string.yarn_weight_fingering
            YarnWeight.SuperBulky -> R.string.yarn_weight_super_bulky
        },
    )
}

@Composable
internal fun FiberForm.label(): String {
    return stringResource(
        id = when (this) {
            FiberForm.Top -> R.string.fiber_form_top
            FiberForm.Unknown -> R.string.fiber_form_unknown
            FiberForm.Batt -> R.string.fiber_form_batt
            FiberForm.Fleece -> R.string.fiber_form_fleece
            FiberForm.Roving -> R.string.fiber_form_roving
            FiberForm.Locks -> R.string.fiber_form_locks
            FiberForm.Rolag -> R.string.fiber_form_rolag
            FiberForm.Other -> R.string.fiber_form_other
        },
    )
}

@Composable
internal fun FiberPreparation.label(): String {
    return stringResource(
        id = when (this) {
            FiberPreparation.Raw -> R.string.fiber_preparation_raw
            FiberPreparation.Dyed -> R.string.fiber_preparation_dyed
            FiberPreparation.Other -> R.string.fiber_preparation_other
            FiberPreparation.Carded -> R.string.fiber_preparation_carded
            FiberPreparation.Combed -> R.string.fiber_preparation_combed
            FiberPreparation.Washed -> R.string.fiber_preparation_washed
            FiberPreparation.Blended -> R.string.fiber_preparation_blended
            FiberPreparation.Natural -> R.string.fiber_preparation_natural
            FiberPreparation.Unknown -> R.string.fiber_preparation_unknown
        },
    )
}

@Composable
internal fun LengthBasis.label(): String {
    return stringResource(
        id = when (this) {
            LengthBasis.Total -> R.string.length_basis_total
            LengthBasis.PerUnit -> R.string.length_basis_per_unit
            LengthBasis.PerHundredGrams -> R.string.length_basis_per_hundred_grams
        },
    )
}

@Composable
internal fun TwistDirection.label(): String {
    return stringResource(
        id = when (this) {
            TwistDirection.S -> R.string.twist_direction_s
            TwistDirection.Z -> R.string.twist_direction_z
            TwistDirection.Unknown -> R.string.twist_direction_unknown
        },
    )
}

@Composable
internal fun InventoryItemStatus.label(): String {
    return stringResource(
        id = when (this) {
            InventoryItemStatus.Active -> R.string.inventory_item_status_active
            InventoryItemStatus.Archived -> R.string.inventory_item_status_archived
        },
    )
}

@Composable
internal fun MeasurementSource.label(): String {
    return stringResource(
        id = when (this) {
            MeasurementSource.Manufacturer -> R.string.measurement_source_manufacturer
            MeasurementSource.Measured -> R.string.measurement_source_measured
            MeasurementSource.Calculated -> R.string.measurement_source_calculated
            MeasurementSource.Estimated -> R.string.measurement_source_estimated
            MeasurementSource.Unknown -> R.string.measurement_source_unknown
        },
    )
}

@Composable
internal fun Gauge.summaryLine(): String? {
    val stitchText = stitchesPer10cm?.let { stitches ->
        stringResource(R.string.format_gauge_stitches, stitches)
    }
    val rowText = rowsPer10cm?.let { rows ->
        stringResource(R.string.format_gauge_rows, rows)
    }
    val needleText = needleSize?.let { needleSize ->
        formatNeedleSize(needleSize.millimeters)
    }

    return listOfNotNull(stitchText, rowText, needleText, note)
        .joinToString(separator = " · ")
        .takeIf { text -> text.isNotBlank() }
}

internal fun Instant.displayDate(): String {
    return DateTimeFormatter.ISO_LOCAL_DATE
        .withZone(ZoneId.systemDefault())
        .format(this)
}

@Composable
internal fun formatMeters(meters: Double): String {
    return stringResource(id = R.string.format_length_meters, meters.toInt())
}

@Composable
internal fun formatGrams(grams: Double): String {
    return stringResource(id = R.string.format_weight_grams, grams.toInt())
}

@Composable
internal fun formatNeedleSize(millimeters: Double): String {
    return stringResource(id = R.string.format_needle_size, millimeters)
}
