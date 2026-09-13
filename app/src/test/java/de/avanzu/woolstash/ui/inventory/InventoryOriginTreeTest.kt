package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryOrigin
import de.avanzu.woolstash.domain.model.Weight
import de.avanzu.woolstash.domain.model.YarnDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryOriginTreeTest {
    @Test
    fun buildInventoryOriginTree_keepsDepletedAndNestedSources() {
        val raw = InventoryItem(name = "Rohfaser", weight = Weight(0.0), details = FiberDetails())
        val dyed = InventoryItem(name = "Gefärbte Faser", weight = Weight(0.0), details = FiberDetails())
        val companion = InventoryItem(name = "Seide", weight = Weight(10.0), details = FiberDetails())
        val yarn = InventoryItem(name = "Mischgarn", weight = Weight(80.0), details = YarnDetails())
        val origins = listOf(
            InventoryOrigin(dyed.id, raw.id, 60.0),
            InventoryOrigin(yarn.id, dyed.id, 50.0),
            InventoryOrigin(yarn.id, companion.id, 30.0),
        )

        val tree = buildInventoryOriginTree(yarn.id, listOf(raw, dyed, companion, yarn), origins)

        assertEquals(listOf("Gefärbte Faser", "Seide"), tree.map { node -> node.item.name })
        assertEquals("Rohfaser", tree.first().origins.single().item.name)
        assertEquals(0.0, tree.first().item.weight?.grams ?: -1.0, 0.0)
    }
}
