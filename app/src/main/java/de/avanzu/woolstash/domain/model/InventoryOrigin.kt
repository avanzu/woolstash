package de.avanzu.woolstash.domain.model

data class InventoryOrigin(
    val childItemId: InventoryItemId,
    val parentItemId: InventoryItemId,
    val consumedGrams: Double,
) {
    init {
        require(consumedGrams.isFinite() && consumedGrams > 0) {
            "Consumed weight must be finite and greater than zero."
        }
    }
}

data class InventorySourceUsage(
    val itemId: InventoryItemId,
    val consumedGrams: Double,
) {
    init {
        require(consumedGrams.isFinite() && consumedGrams > 0) {
            "Consumed weight must be finite and greater than zero."
        }
    }
}

fun remainingWeightGrams(
    currentGrams: Double,
    consumedGrams: Double,
): Double {
    require(currentGrams.isFinite() && currentGrams >= 0) {
        "Current weight must be finite and non-negative."
    }
    require(consumedGrams.isFinite() && consumedGrams > 0) {
        "Consumed weight must be finite and greater than zero."
    }
    return (currentGrams - consumedGrams).coerceAtLeast(0.0)
}
