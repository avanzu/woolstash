package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventorySourceUsage
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.Weight
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryMergeDefaultsTest {
    @Test
    fun buildInventoryMergeDefaults_mergesDescriptionsByConsumedShare() {
        val merino = fiber(
            name = "Merino",
            color = "Rot",
            material = "Merino",
            tags = listOf(Tag("weich")),
        )
        val silk = fiber(
            name = "Seide",
            color = "Blau",
            material = "Seide",
            tags = listOf(Tag("glanz")),
        )

        val defaults = buildInventoryMergeDefaults(
            items = listOf(merino, silk),
            sourceUsages = listOf(
                InventorySourceUsage(merino.id, 70.0),
                InventorySourceUsage(silk.id, 30.0),
            ),
        )

        assertEquals(100.0, defaults.weightGrams, 0.0)
        assertEquals("70 % Rot · 30 % Blau", defaults.colorDescription)
        assertEquals("70 % Merino · 30 % Seide", defaults.materialDescription)
        assertEquals(listOf("weich", "glanz"), defaults.tags.map { tag -> tag.name })
    }

    @Test
    fun buildInventoryMergeDefaults_aggregatesEqualDescriptions() {
        val first = fiber(name = "Erste", material = "Merino")
        val second = fiber(name = "Zweite", material = "merino")
        val silk = fiber(name = "Seide", material = "Seide")

        val defaults = buildInventoryMergeDefaults(
            items = listOf(first, second, silk),
            sourceUsages = listOf(
                InventorySourceUsage(first.id, 20.0),
                InventorySourceUsage(second.id, 30.0),
                InventorySourceUsage(silk.id, 50.0),
            ),
        )

        assertEquals("50 % Merino · 50 % Seide", defaults.materialDescription)
    }

    private fun fiber(
        name: String,
        color: String? = null,
        material: String? = null,
        tags: List<Tag> = emptyList(),
    ): InventoryItem {
        return InventoryItem(
            name = name,
            colorDescription = color,
            materialDescription = material,
            weight = Weight(100.0),
            tags = tags,
            details = FiberDetails(),
        )
    }
}
