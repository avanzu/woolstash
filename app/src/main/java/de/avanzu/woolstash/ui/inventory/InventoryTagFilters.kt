package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.LengthBasis
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.TwistDirection
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.YarnWeight
import java.util.Locale

internal enum class InventoryListSortField {
    Updated,
    Created,
    Name,
    Weight,
    Location,
    Color,
    Material,
}

internal enum class InventoryListSortDirection {
    Ascending,
    Descending,
}

internal data class InventoryListSort(
    val field: InventoryListSortField = InventoryListSortField.Updated,
    val direction: InventoryListSortDirection = InventoryListSortDirection.Descending,
) {
    fun select(field: InventoryListSortField): InventoryListSort {
        return if (field == this.field) {
            copy(direction = direction.reversed())
        } else {
            InventoryListSort(
                field = field,
                direction = InventoryListSortDirection.Ascending,
            )
        }
    }
}

internal data class OptionalValueFilter<T>(
    val values: Set<T> = emptySet(),
    val includesUnspecified: Boolean = false,
) {
    val isActive: Boolean
        get() = values.isNotEmpty() || includesUnspecified

    fun matches(value: T?): Boolean {
        if (!isActive) {
            return true
        }

        return value?.let(values::contains) ?: includesUnspecified
    }
}

internal data class InventorySearchCriteria(
    val productType: ProductType? = null,
    val tags: Set<String> = emptySet(),
    val includeDepletedItems: Boolean = false,
    val yarnWeights: OptionalValueFilter<YarnWeight> = OptionalValueFilter(),
    val lengthBases: OptionalValueFilter<LengthBasis> = OptionalValueFilter(),
    val twistDirections: OptionalValueFilter<TwistDirection> = OptionalValueFilter(),
    val fiberForms: OptionalValueFilter<FiberForm> = OptionalValueFilter(),
    val fiberPreparations: OptionalValueFilter<FiberPreparation> = OptionalValueFilter(),
) {
    val activeFilterCount: Int
        get() = listOf(
            productType != null,
            tags.isNotEmpty(),
            includeDepletedItems,
            yarnWeights.isActive,
            lengthBases.isActive,
            twistDirections.isActive,
            fiberForms.isActive,
            fiberPreparations.isActive,
        ).count { active -> active }

    fun withProductType(productType: ProductType?): InventorySearchCriteria {
        return copy(
            productType = productType,
            yarnWeights = if (productType == ProductType.Yarn) yarnWeights else OptionalValueFilter(),
            lengthBases = if (productType == ProductType.Yarn) lengthBases else OptionalValueFilter(),
            twistDirections = if (productType == ProductType.Yarn) twistDirections else OptionalValueFilter(),
            fiberForms = if (productType == ProductType.Fiber) fiberForms else OptionalValueFilter(),
            fiberPreparations = if (productType == ProductType.Fiber) {
                fiberPreparations
            } else {
                OptionalValueFilter()
            },
        )
    }
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

internal fun List<InventoryItem>.filterByAvailableAmount(includeDepletedItems: Boolean): List<InventoryItem> {
    if (includeDepletedItems) {
        return this
    }

    return filter { item -> item.weight?.grams != 0.0 }
}

internal fun List<InventoryItem>.filterBySearchCriteria(
    criteria: InventorySearchCriteria,
): List<InventoryItem> {
    return filterByAvailableAmount(criteria.includeDepletedItems)
        .filterByProductType(criteria.productType)
        .filter { item ->
            criteria.tags.isEmpty() || item.tags.any { tag -> tag.name in criteria.tags }
        }
        .filter { item ->
            when (val details = item.details) {
                is YarnDetails -> criteria.yarnWeights.matches(details.yarnWeight) &&
                    criteria.lengthBases.matches(details.lengthBasis) &&
                    criteria.twistDirections.matches(details.twistDirection)

                is FiberDetails -> criteria.fiberForms.matches(details.fiberForm) &&
                    criteria.fiberPreparations.matches(details.preparation)
            }
        }
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
    return sortedWith { first, second ->
        val primaryComparison = when (sort.field) {
            InventoryListSortField.Updated -> compareKnownValues(
                first.updatedAt,
                second.updatedAt,
                sort.direction,
            )

            InventoryListSortField.Created -> compareKnownValues(
                first.createdAt,
                second.createdAt,
                sort.direction,
            )

            InventoryListSortField.Name -> compareKnownValues(
                first.name.normalizedSortName(),
                second.name.normalizedSortName(),
                sort.direction,
            )

            InventoryListSortField.Weight -> compareOptionalValues(
                first.weight?.grams,
                second.weight?.grams,
                sort.direction,
            )

            InventoryListSortField.Location -> compareOptionalValues(
                first.location?.normalizedSortName(),
                second.location?.normalizedSortName(),
                sort.direction,
            )

            InventoryListSortField.Color -> compareOptionalValues(
                first.colorDescription?.normalizedSortName(),
                second.colorDescription?.normalizedSortName(),
                sort.direction,
            )

            InventoryListSortField.Material -> compareOptionalValues(
                first.materialDescription?.normalizedSortName(),
                second.materialDescription?.normalizedSortName(),
                sort.direction,
            )
        }

        primaryComparison.takeIf { comparison -> comparison != 0 }
            ?: first.name.normalizedSortName().compareTo(second.name.normalizedSortName())
    }
}

private fun InventoryListSortDirection.reversed(): InventoryListSortDirection {
    return when (this) {
        InventoryListSortDirection.Ascending -> InventoryListSortDirection.Descending
        InventoryListSortDirection.Descending -> InventoryListSortDirection.Ascending
    }
}

private fun <T : Comparable<T>> compareOptionalValues(
    first: T?,
    second: T?,
    direction: InventoryListSortDirection,
): Int {
    if (first == null) {
        return if (second == null) 0 else 1
    }
    if (second == null) {
        return -1
    }

    return compareKnownValues(first, second, direction)
}

private fun <T : Comparable<T>> compareKnownValues(
    first: T,
    second: T,
    direction: InventoryListSortDirection,
): Int {
    return when (direction) {
        InventoryListSortDirection.Ascending -> first.compareTo(second)
        InventoryListSortDirection.Descending -> second.compareTo(first)
    }
}

private fun String.normalizedSortName(): String {
    return lowercase(Locale.ROOT)
}

private fun InventoryItem.searchableText(): List<String> {
    val commonText = listOfNotNull(
        name,
        colorDescription,
        materialDescription,
        location,
        manufacturer,
        purchaseSource,
        notes,
    ) + tags.map { tag -> tag.name }

    val productText = when (val details = details) {
        is YarnDetails -> listOfNotNull(
            details.dyeLot,
            details.gauge?.note,
        )

        is FiberDetails -> listOfNotNull(
            details.breedOrSource,
            details.intendedSpin,
        )
    }

    return commonText + productText
}
