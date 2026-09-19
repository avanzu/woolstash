package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.Gauge
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.LengthBasis
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.TwistDirection
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.YarnWeight
import de.avanzu.woolstash.domain.model.Weight
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
    fun filterByAvailableAmount_hidesOnlyZeroWeightsByDefault() {
        val positive = InventoryItem(name = "Voll", weight = Weight(50.0), details = YarnDetails())
        val depleted = InventoryItem(name = "Leer", weight = Weight(0.0), details = YarnDetails())
        val unknown = InventoryItem(name = "Unbekannt", weight = null, details = FiberDetails())

        assertEquals(
            listOf(positive, unknown),
            listOf(positive, depleted, unknown).filterByAvailableAmount(false),
        )
    }

    @Test
    fun filterByAvailableAmount_includesZeroWeightsWhenEnabled() {
        val items = listOf(
            InventoryItem(name = "Voll", weight = Weight(50.0), details = YarnDetails()),
            InventoryItem(name = "Leer", weight = Weight(0.0), details = YarnDetails()),
        )

        assertEquals(items, items.filterByAvailableAmount(true))
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
    fun filterBySearchQuery_matchesProductSpecificTextFields() {
        val items = sampleItems()

        assertEquals(listOf(items[0]), items.filterBySearchQuery("Partie 42"))
        assertEquals(listOf(items[0]), items.filterBySearchQuery("elastisch"))
        assertEquals(listOf(items[1]), items.filterBySearchQuery("Schafhof"))
        assertEquals(listOf(items[1]), items.filterBySearchQuery("Kammgarn"))
    }

    @Test
    fun filterBySearchCriteria_combinesCommonAndProductSpecificCriteria() {
        val items = sampleItems()
        val criteria = InventorySearchCriteria(
            productType = ProductType.Fiber,
            tags = setOf("natur", "luxus"),
            fiberForms = OptionalValueFilter(values = setOf(FiberForm.Top)),
            fiberPreparations = OptionalValueFilter(values = setOf(FiberPreparation.Combed)),
        )

        assertEquals(listOf(items[1]), items.filterBySearchCriteria(criteria))
    }

    @Test
    fun filterBySearchCriteria_canMatchMissingDropdownValues() {
        val items = sampleItems()
        val criteria = InventorySearchCriteria(
            productType = ProductType.Yarn,
            yarnWeights = OptionalValueFilter(includesUnspecified = true),
        )

        assertEquals(listOf(items[2]), items.filterBySearchCriteria(criteria))
    }

    @Test
    fun withProductType_clearsCriteriaForHiddenProductSections() {
        val criteria = InventorySearchCriteria(
            productType = ProductType.Yarn,
            yarnWeights = OptionalValueFilter(values = setOf(YarnWeight.Fingering)),
        )

        val fiberCriteria = criteria.withProductType(ProductType.Fiber)

        assertEquals(false, fiberCriteria.yarnWeights.isActive)
        assertEquals(ProductType.Fiber, fiberCriteria.productType)
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
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Name,
                    direction = InventoryListSortDirection.Ascending,
                ),
            ).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_byNameDescending_reversesOrder() {
        val items = sampleItems()

        assertEquals(
            listOf("Sockenwolle", "Rest", "Kammzug"),
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Name,
                    direction = InventoryListSortDirection.Descending,
                ),
            ).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_byWeight_sortsMissingValuesLastInBothDirections() {
        val items = sampleItems()

        assertEquals(
            listOf("Sockenwolle", "Kammzug", "Rest"),
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Weight,
                    direction = InventoryListSortDirection.Ascending,
                ),
            ).map { item -> item.name },
        )
        assertEquals(
            listOf("Kammzug", "Sockenwolle", "Rest"),
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Weight,
                    direction = InventoryListSortDirection.Descending,
                ),
            ).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_byLocation_sortsTextAndKeepsMissingValuesLast() {
        val items = sampleItems()

        assertEquals(
            listOf("Kammzug", "Sockenwolle", "Rest"),
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Location,
                    direction = InventoryListSortDirection.Ascending,
                ),
            ).map { item -> item.name },
        )
        assertEquals(
            listOf("Sockenwolle", "Kammzug", "Rest"),
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Location,
                    direction = InventoryListSortDirection.Descending,
                ),
            ).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_byCreated_sortsOldestFirst() {
        val items = sampleItems()

        assertEquals(
            listOf("Kammzug", "Rest", "Sockenwolle"),
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Created,
                    direction = InventoryListSortDirection.Ascending,
                ),
            ).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_equalValues_useNameAscendingAsTieBreaker() {
        val beta = InventoryItem(name = "Beta", weight = Weight(50.0), details = YarnDetails())
        val alpha = InventoryItem(name = "Alpha", weight = Weight(50.0), details = YarnDetails())

        assertEquals(
            listOf("Alpha", "Beta"),
            listOf(beta, alpha).sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Weight,
                    direction = InventoryListSortDirection.Descending,
                ),
            ).map { item -> item.name },
        )
    }

    @Test
    fun sortForInventoryList_byUpdatedNewest_sortsNewestFirst() {
        val items = sampleItems()

        assertEquals(
            listOf("Rest", "Kammzug", "Sockenwolle"),
            items.sortForInventoryList(
                InventoryListSort(
                    field = InventoryListSortField.Updated,
                    direction = InventoryListSortDirection.Descending,
                ),
            ).map { item -> item.name },
        )
    }

    @Test
    fun sortSelection_newFieldStartsAscendingAndSelectedFieldTogglesDirection() {
        val initial = InventoryListSort()

        val byName = initial.select(InventoryListSortField.Name)
        val byNameDescending = byName.select(InventoryListSortField.Name)
        val byNameAscendingAgain = byNameDescending.select(InventoryListSortField.Name)

        assertEquals(InventoryListSortDirection.Ascending, byName.direction)
        assertEquals(InventoryListSortDirection.Descending, byNameDescending.direction)
        assertEquals(InventoryListSortDirection.Ascending, byNameAscendingAgain.direction)
    }

    private fun sampleItems(): List<InventoryItem> {
        return listOf(
            InventoryItem(
                name = "Sockenwolle",
                colorDescription = "Blaugrün",
                materialDescription = "Merino / Polyamid",
                weight = Weight(100.0),
                location = "Kiste Schlafzimmer",
                manufacturer = "Malabrigo",
                purchaseSource = "Wollgeschäft",
                tags = listOf(Tag("socken")),
                details = YarnDetails(
                    lengthBasis = LengthBasis.PerHundredGrams,
                    yarnWeight = YarnWeight.Fingering,
                    gauge = Gauge(note = "elastisch gestrickt"),
                    dyeLot = "Partie 42",
                    twistDirection = TwistDirection.Z,
                ),
                createdAt = Instant.parse("2025-01-03T10:00:00Z"),
                updatedAt = Instant.parse("2026-01-01T10:00:00Z"),
            ),
            InventoryItem(
                name = "Kammzug",
                colorDescription = "Naturgrau",
                materialDescription = "Bluefaced Leicester",
                weight = Weight(200.0),
                location = "Faserkiste",
                purchaseSource = "Faserhof",
                tags = listOf(Tag("spinnen"), Tag("natur")),
                details = FiberDetails(
                    fiberForm = FiberForm.Top,
                    preparation = FiberPreparation.Combed,
                    breedOrSource = "Schafhof am Deich",
                    intendedSpin = "Kammgarn",
                ),
                createdAt = Instant.parse("2025-01-01T10:00:00Z"),
                updatedAt = Instant.parse("2026-01-02T10:00:00Z"),
            ),
            InventoryItem(
                name = "Rest",
                notes = "Projektidee für Bündchen",
                tags = listOf(Tag("socken")),
                details = YarnDetails(),
                createdAt = Instant.parse("2025-01-02T10:00:00Z"),
                updatedAt = Instant.parse("2026-01-03T10:00:00Z"),
            ),
        )
    }
}
