package de.avanzu.woolstash.domain.model

import java.time.Instant
import java.util.UUID

data class InventoryItem(
    val id: InventoryItemId = InventoryItemId.new(),
    val name: String,
    val colorDescription: String? = null,
    val materialDescription: String? = null,
    val weight: Weight? = null,
    val location: String? = null,
    val status: InventoryItemStatus = InventoryItemStatus.Active,
    val tags: List<Tag> = emptyList(),
    val photos: List<PhotoRef> = emptyList(),
    val notes: String? = null,
    val details: ProductDetails,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
){
    val productType: ProductType
        get() = details.productType
}


@JvmInline
value class InventoryItemId(val value: String) {
    companion object {
        fun new(): InventoryItemId = InventoryItemId(UUID.randomUUID().toString())
    }
}

enum class ProductType {
    Yarn,
    Fiber,
}

enum class InventoryItemStatus {
    Active,
    Archived,
}

sealed interface ProductDetails {
    val productType: ProductType
}

