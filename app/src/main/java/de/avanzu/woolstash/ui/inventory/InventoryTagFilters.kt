package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.Tag
import java.util.Locale

internal enum class InventoryListSort {
    UpdatedNewest,
    NameAsc,
    TypeThenName,
}

internal fun List<InventoryItem>.availableTagNames(): List<String> {
    return flatMap { item -> item.tags }
        .map { tag -> tag.name }
        .distinct()
        .sorted()
}

internal fun List<InventoryItem>.filterByTag(tagName: String?): List<InventoryItem> {
    if (tagName.isNullOrBlank()) {
        return this
    }

    val normalizedTagName = Tag.fromInput(tagName)?.name ?: return emptyList()

    return filter { item ->
        item.tags.any { tag -> tag.name == normalizedTagName }
    }
}

internal fun List<InventoryItem>.filterByProductType(productType: ProductType?): List<InventoryItem> {
    if (productType == null) {
        return this
    }

    return filter { item -> item.productType == productType }
}

internal fun List<InventoryItem>.filterBySearchQuery(query: String?): List<InventoryItem> {
    val normalizedQuery = query
        ?.trim()
        ?.lowercase(Locale.ROOT)
        .orEmpty()

    if (normalizedQuery.isBlank()) {
        return this
    }

    return filter { item ->
        item.searchableText().any { value ->
            value.lowercase(Locale.ROOT).contains(normalizedQuery)
        }
    }
}

internal fun List<InventoryItem>.sortForInventoryList(sort: InventoryListSort): List<InventoryItem> {
    return when (sort) {
        InventoryListSort.UpdatedNewest -> sortedWith(
            compareByDescending<InventoryItem> { item -> item.updatedAt }
                .thenBy { item -> item.name.normalizedSortName() },
        )

        InventoryListSort.NameAsc -> sortedBy { item -> item.name.normalizedSortName() }

        InventoryListSort.TypeThenName -> sortedWith(
            compareBy<InventoryItem> { item -> item.productType.sortRank() }
                .thenBy { item -> item.name.normalizedSortName() },
        )
    }
}

private fun ProductType.sortRank(): Int {
    return when (this) {
        ProductType.Yarn -> 0
        ProductType.Fiber -> 1
    }
}

private fun String.normalizedSortName(): String {
    return lowercase(Locale.ROOT)
}

private fun InventoryItem.searchableText(): List<String> {
    return listOfNotNull(
        name,
        colorDescription,
        materialDescription,
        location,
        notes,
    ) + tags.map { tag -> tag.name }
}
