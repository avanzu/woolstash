package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.InventoryOrigin

internal data class InventoryOriginNode(
    val item: InventoryItem,
    val consumedGrams: Double,
    val origins: List<InventoryOriginNode>,
)

internal fun buildInventoryOriginTree(
    rootItemId: InventoryItemId,
    items: List<InventoryItem>,
    origins: List<InventoryOrigin>,
): List<InventoryOriginNode> {
    val itemsById = items.associateBy { item -> item.id }
    val originsByChildId = origins.groupBy { origin -> origin.childItemId }

    fun buildNodes(
        childItemId: InventoryItemId,
        path: Set<InventoryItemId>,
    ): List<InventoryOriginNode> {
        return originsByChildId[childItemId]
            .orEmpty()
            .mapNotNull { origin ->
                val parent = itemsById[origin.parentItemId] ?: return@mapNotNull null
                InventoryOriginNode(
                    item = parent,
                    consumedGrams = origin.consumedGrams,
                    origins = if (parent.id in path) {
                        emptyList()
                    } else {
                        buildNodes(parent.id, path + parent.id)
                    },
                )
            }
            .sortedBy { node -> node.item.name.lowercase() }
    }

    return buildNodes(rootItemId, setOf(rootItemId))
}
