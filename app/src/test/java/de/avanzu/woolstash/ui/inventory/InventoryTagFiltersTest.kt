package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.YarnDetails
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryTagFiltersTest {
    @Test
    fun filterByTag_withoutFilter_returnsAllItems() {
        val items = sampleItems()

        assertEquals(items, items.filterByTag(null))
        assertEquals(items, items.filterByTag(""))
    }

    @Test
    fun filterByTag_withTag_returnsMatchingItems() {
        val items = sampleItems()

        assertEquals(listOf(items[0], items[2]), items.filterByTag("socken"))
    }

    @Test
    fun filterByTag_withUnknownTag_returnsEmptyList() {
        assertEquals(emptyList<InventoryItem>(), sampleItems().filterByTag("luxus"))
    }

    @Test
    fun availableTagNames_returnsSortedDistinctTags() {
        assertEquals(
            listOf("natur", "socken", "spinnen"),
            sampleItems().availableTagNames(),
        )
    }

    @Test
    fun filterByProductType_withoutFilter_returnsAllItems() {
        val items = sampleItems()

        assertEquals(items, items.filterByProductType(null))
    }

    @Test
    fun filterByProductType_withProductType_returnsMatchingItems() {
        val items = sampleItems()

        assertEquals(listOf(items[0], items[2]), items.filterByProductType(ProductType.Yarn))
        assertEquals(listOf(items[1]), items.filterByProductType(ProductType.Fiber))
    }

    @Test
    fun filters_combineTagAndProductType() {
        val items = sampleItems()

        assertEquals(
            listOf(items[1]),
            items
                .filterByProductType(ProductType.Fiber)
                .filterByTag("natur"),
        )
    }

    @Test
    fun sortForInventoryList_byName_sortsAlphabetically() {
        val items = sampleItems()

        assertEquals(
            listOf("Kammzug", "Rest", "Sockenwolle"),
            items.sortForInventoryList(InventoryListSort.NameAsc).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_byType_groupsByTypeThenName() {
        val items = sampleItems()

        assertEquals(
            listOf("Rest", "Sockenwolle", "Kammzug"),
            items.sortForInventoryList(InventoryListSort.TypeThenName).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_byUpdatedNewest_sortsNewestFirst() {
        val items = sampleItems()

        assertEquals(
            listOf("Rest", "Kammzug", "Sockenwolle"),
            items.sortForInventoryList(InventoryListSort.UpdatedNewest).map { item -> item.name },
        )
    }

    private fun sampleItems(): List<InventoryItem> {
        return listOf(
            InventoryItem(
                name = "Sockenwolle",
                tags = listOf(Tag("socken")),
                details = YarnDetails(),
                updatedAt = Instant.parse("2026-01-01T10:00:00Z"),
            ),
            InventoryItem(
                name = "Kammzug",
                tags = listOf(Tag("spinnen"), Tag("natur")),
                details = FiberDetails(),
                updatedAt = Instant.parse("2026-01-02T10:00:00Z"),
            ),
            InventoryItem(
                name = "Rest",
                tags = listOf(Tag("socken")),
                details = YarnDetails(),
                updatedAt = Instant.parse("2026-01-03T10:00:00Z"),
            ),
        )
    }
}
