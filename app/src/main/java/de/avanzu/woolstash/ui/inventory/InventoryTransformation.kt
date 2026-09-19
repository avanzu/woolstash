package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.InventoryItem

internal fun CreateInventoryItemInput.toDyedFiberInventoryItem(
    source: InventoryItem,
): InventoryItem {
    val sourceDetails = requireNotNull(source.details as? FiberDetails) {
        "Only a fiber item can be transformed into a dyed fiber."
    }

    return toInventoryItem(
        details = sourceDetails.copy(preparation = FiberPreparation.Dyed),
    ).copy(notes = source.notes)
}
