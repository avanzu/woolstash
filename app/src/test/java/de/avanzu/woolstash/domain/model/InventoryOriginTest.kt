package de.avanzu.woolstash.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryOriginTest {
    @Test
    fun remainingWeightGrams_subtractsConsumedWeight() {
        assertEquals(35.0, remainingWeightGrams(currentGrams = 50.0, consumedGrams = 15.0), 0.0)
    }

    @Test
    fun remainingWeightGrams_clampsOverconsumptionToZero() {
        assertEquals(0.0, remainingWeightGrams(currentGrams = 50.0, consumedGrams = 75.0), 0.0)
    }
}
