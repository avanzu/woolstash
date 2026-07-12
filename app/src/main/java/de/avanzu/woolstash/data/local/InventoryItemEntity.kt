package de.avanzu.woolstash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey
    val id: String,

    val productType: String,

    val name: String,
    val colorDescription: String?,
    val materialDescription: String?,

    val weightGrams: Double?,
    val weightSource: String?,

    val location: String?,
    val manufacturer: String?,
    val purchaseSource: String?,
    val status: String,
    val notes: String?,

    val createdAt: String,
    val updatedAt: String,

    // Yarn fields
    val lengthMeters: Double?,
    val lengthSource: String?,
    val lengthBasis: String?,
    val yarnWeight: String?,
    val skeinCount: Int?,
    val recommendedNeedleSizeMillimeters: Double?,

    val gaugeStitchesPer10cm: Double?,
    val gaugeRowsPer10cm: Double?,
    val gaugeNeedleSizeMillimeters: Double?,
    val gaugeNote: String?,

    val dyeLot: String?,
    val ply: Int?,
    val twistDirection: String?,

    // Fiber fields
    val fiberForm: String?,
    val fiberPreparation: String?,
    val breedOrSource: String?,
    val stapleLengthMeters: Double?,
    val stapleLengthSource: String?,
    val micron: Double?,
    val intendedSpin: String?,
)
