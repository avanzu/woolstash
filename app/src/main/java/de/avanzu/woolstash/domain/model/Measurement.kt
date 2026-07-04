package de.avanzu.woolstash.domain.model


data class Weight(
    val grams: Double,
    val source: MeasurementSource = MeasurementSource.Unknown,
) {
    init {
        require(grams >= 0) { "Weight must not be negative." }
    }
}

data class Length(
    val meters: Double,
    val source: MeasurementSource = MeasurementSource.Unknown,
) {
    init {
        require(meters >= 0) { "Length must not be negative." }
    }
}

data class NeedleSize(
    val millimeters: Double,
) {
    init {
        require(millimeters > 0) { "Needle size must be greater than zero." }
    }
}

data class Gauge(
    val stitchesPer10cm: Double? = null,
    val rowsPer10cm: Double? = null,
    val needleSize: NeedleSize? = null,
    val note: String? = null,
)

enum class MeasurementSource {
    Manufacturer,
    Measured,
    Calculated,
    Estimated,
    Unknown,
}