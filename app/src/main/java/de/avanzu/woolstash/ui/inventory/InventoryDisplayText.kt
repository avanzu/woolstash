package de.avanzu.woolstash.ui.inventory

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.ProductDetails
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.YarnWeight

@Composable
internal fun InventoryItem.summaryLine(): String {
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
internal fun ProductDetails.detailSummaryLine(): String {
    return when (this) {
        is YarnDetails -> yarnSummaryLine()
        is FiberDetails -> fiberDetailsLine()
    }
}

@Composable
private fun InventoryItem.productTypeLabel(): String {
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

    return parts.joinToString(separator = " · ")
}

@Composable
private fun FiberDetails.fiberDetailsLine(): String {
    val parts = listOfNotNull(
        fiberForm?.label(),
        preparation?.label(),
        intendedSpin,
    )

    return parts.joinToString(separator = " · ")
}

@Composable
private fun YarnWeight.label(): String {
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
private fun FiberForm.label(): String {
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
private fun FiberPreparation.label(): String {
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
private fun formatMeters(meters: Double): String {
    return stringResource(id = R.string.format_length_meters, meters.toInt())
}

@Composable
private fun formatGrams(grams: Double): String {
    return stringResource(id = R.string.format_weight_grams, grams.toInt())
}

@Composable
private fun formatNeedleSize(millimeters: Double): String {
    return stringResource(id = R.string.format_needle_size, millimeters)
}
