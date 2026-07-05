package de.avanzu.woolstash.ui.inventory

import androidx.annotation.StringRes
import de.avanzu.woolstash.R

internal enum class WeightInputUnit(
    @StringRes val labelRes: Int,
) {
    Grams(R.string.unit_grams),
    Kilograms(R.string.unit_kilograms),
    Ounces(R.string.unit_ounces),
    Pounds(R.string.unit_pounds),
}

internal fun WeightInputUnit.toGrams(value: Double): Double {
    return when (this) {
        WeightInputUnit.Grams -> value
        WeightInputUnit.Kilograms -> value * 1_000.0
        WeightInputUnit.Ounces -> value * 28.349523125
        WeightInputUnit.Pounds -> value * 453.59237
    }
}

internal fun WeightInputUnit.fromGrams(grams: Double): Double {
    return when (this) {
        WeightInputUnit.Grams -> grams
        WeightInputUnit.Kilograms -> grams / 1_000.0
        WeightInputUnit.Ounces -> grams / 28.349523125
        WeightInputUnit.Pounds -> grams / 453.59237
    }
}

internal enum class LengthInputUnit(
    @StringRes val labelRes: Int,
) {
    Meters(R.string.unit_meters),
    Centimeters(R.string.unit_centimeters),
    Yards(R.string.unit_yards),
    Inches(R.string.unit_inches),
}

internal fun LengthInputUnit.toMeters(value: Double): Double {
    return when (this) {
        LengthInputUnit.Meters -> value
        LengthInputUnit.Centimeters -> value / 100.0
        LengthInputUnit.Yards -> value * 0.9144
        LengthInputUnit.Inches -> value * 0.0254
    }
}

internal fun LengthInputUnit.fromMeters(meters: Double): Double {
    return when (this) {
        LengthInputUnit.Meters -> meters
        LengthInputUnit.Centimeters -> meters * 100.0
        LengthInputUnit.Yards -> meters / 0.9144
        LengthInputUnit.Inches -> meters / 0.0254
    }
}
