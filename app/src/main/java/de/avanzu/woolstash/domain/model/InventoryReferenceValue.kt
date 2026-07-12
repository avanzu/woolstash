package de.avanzu.woolstash.domain.model

data class InventoryReferenceValue(
    val type: InventoryReferenceType,
    val name: String,
)

enum class InventoryReferenceType {
    Location,
    Manufacturer,
    PurchaseSource,
}

fun String?.toInventoryReferenceName(): String? {
    return this
        ?.trim()
        ?.replace(Regex("\\s+"), " ")
        ?.takeIf { value -> value.isNotEmpty() }
}
