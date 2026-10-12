package de.avanzu.woolstash.ui.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementInputUnitTest {
    @Test
    fun millimeters_toMeters_normalizesLength() {
        assertEquals(0.075, LengthInputUnit.Millimeters.toMeters(75.0), 0.0)
    }

    @Test
    fun millimeters_fromMeters_restoresDisplayValue() {
        assertEquals(75.0, LengthInputUnit.Millimeters.fromMeters(0.075), 0.0)
    }
}
