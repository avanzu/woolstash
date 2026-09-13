package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventorySourceUsage
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.normalizedDistinct
import java.util.Locale

internal data class InventoryMergeDefaults(
    val colorDescription: String?,
    val materialDescription: String?,
    val weightGrams: Double,
    val tags: List<Tag>,
)

internal fun buildInventoryMergeDefaults(
    items: List<InventoryItem>,
    sourceUsages: List<InventorySourceUsage>,
): InventoryMergeDefaults {
    val itemsById = items.associateBy { item -> item.id }
    val sources = sourceUsages.mapNotNull { usage ->
        itemsById[usage.itemId]?.let { item -> usage to item }
    }
    val totalGrams = sourceUsages.sumOf { usage -> usage.consumedGrams }

    return InventoryMergeDefaults(
        colorDescription = sources.toProportionalDescription(
            totalGrams = totalGrams,
            value = { item -> item.colorDescription },
        ),
        materialDescription = sources.toProportionalDescription(
            totalGrams = totalGrams,
            value = { item -> item.materialDescription },
        ),
        weightGrams = totalGrams,
        tags = sources
            .flatMap { (_, item) -> item.tags }
            .normalizedDistinct(),
    )
}

private fun List<Pair<InventorySourceUsage, InventoryItem>>.toProportionalDescription(
    totalGrams: Double,
    value: (InventoryItem) -> String?,
): String? {
    if (totalGrams <= 0) {
        return null
    }

    val components = linkedMapOf<String, DescriptionComponent>()
    forEach { (usage, item) ->
        val description = value(item)?.trim()?.takeIf { text -> text.isNotEmpty() }
            ?: return@forEach
        val key = description.lowercase(Locale.ROOT)
        val existing = components[key]
        components[key] = DescriptionComponent(
            description = existing?.description ?: description,
            grams = (existing?.grams ?: 0.0) + usage.consumedGrams,
        )
    }

    return components.values
        .sortedWith(
            compareByDescending<DescriptionComponent> { component -> component.grams }
                .thenBy { component -> component.description.lowercase(Locale.ROOT) },
        )
        .joinToString(separator = " · ") { component ->
            val percentage = component.grams / totalGrams * 100.0
            "${percentage.formatPercentage()} % ${component.description}"
        }
        .takeIf { description -> description.isNotEmpty() }
}

private fun Double.formatPercentage(): String {
    val value = String.format(Locale.ROOT, "%.1f", this)
    return value.removeSuffix(".0")
}

private data class DescriptionComponent(
    val description: String,
    val grams: Double,
)
