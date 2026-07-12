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
    fun filterBySearchQuery_withoutQuery_returnsAllItems() {
        val items = sampleItems()

        assertEquals(items, items.filterBySearchQuery(null))
        assertEquals(items, items.filterBySearchQuery(""))
        assertEquals(items, items.filterBySearchQuery(" "))
    }

    @Test
    fun filterBySearchQuery_matchesInventoryTextFields() {
        val items = sampleItems()

        assertEquals(listOf(items[0]), items.filterBySearchQuery("Sockenwolle"))
        assertEquals(listOf(items[0]), items.filterBySearchQuery("blau"))
        assertEquals(listOf(items[0]), items.filterBySearchQuery("Merino"))
        assertEquals(listOf(items[1]), items.filterBySearchQuery("Faserkiste"))
        assertEquals(listOf(items[0]), items.filterBySearchQuery("Malabrigo"))
        assertEquals(listOf(items[1]), items.filterBySearchQuery("Faserhof"))
        assertEquals(listOf(items[2]), items.filterBySearchQuery("Projektidee"))
        assertEquals(listOf(items[1]), items.filterBySearchQuery("natur"))
    }

    @Test
    fun filterBySearchQuery_isCaseInsensitive() {
        val items = sampleItems()

        assertEquals(listOf(items[0]), items.filterBySearchQuery("SOCKENWOLLE"))
    }

    @Test
    fun filters_combineTagProductTypeAndSearchQuery() {
        val items = sampleItems()

        assertEquals(
            listOf(items[1]),
            items
                .filterByProductType(ProductType.Fiber)
                .filterByTag("natur")
                .filterBySearchQuery("bluefaced"),
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
                colorDescription = "Blaugrün",
                materialDescription = "Merino / Polyamid",
                location = "Kiste Schlafzimmer",
                manufacturer = "Malabrigo",
                purchaseSource = "Wollgeschäft",
                tags = listOf(Tag("socken")),
                details = YarnDetails(),
                updatedAt = Instant.parse("2026-01-01T10:00:00Z"),
            ),
            InventoryItem(
                name = "Kammzug",
                colorDescription = "Naturgrau",
                materialDescription = "Bluefaced Leicester",
                location = "Faserkiste",
                purchaseSource = "Faserhof",
                tags = listOf(Tag("spinnen"), Tag("natur")),
                details = FiberDetails(),
                updatedAt = Instant.parse("2026-01-02T10:00:00Z"),
            ),
            InventoryItem(
                name = "Rest",
                notes = "Projektidee für Bündchen",
                tags = listOf(Tag("socken")),
                details = YarnDetails(),
                updatedAt = Instant.parse("2026-01-03T10:00:00Z"),
            ),
        )
    }
}
