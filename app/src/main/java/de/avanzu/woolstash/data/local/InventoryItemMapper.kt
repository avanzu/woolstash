package de.avanzu.woolstash.data.local

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.Gauge
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.InventoryItemStatus
import de.avanzu.woolstash.domain.model.Length
import de.avanzu.woolstash.domain.model.LengthBasis
import de.avanzu.woolstash.domain.model.MeasurementSource
import de.avanzu.woolstash.domain.model.NeedleSize
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.Weight
import de.avanzu.woolstash.domain.model.YarnDetails
import de.avanzu.woolstash.domain.model.YarnWeight
import de.avanzu.woolstash.domain.model.TwistDirection
import java.time.Instant

fun InventoryItem.toEntity(): InventoryItemEntity {
    val yarnDetails = details as? YarnDetails
    val fiberDetails = details as? FiberDetails

    return InventoryItemEntity(
        id = id.value,
        productType = productType.name,

        name = name,
        colorDescription = colorDescription,
        materialDescription = materialDescription,

        weightGrams = weight?.grams,
        weightSource = weight?.source?.name,

        location = location,
        status = status.name,
        notes = notes,

        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),

        lengthMeters = yarnDetails?.length?.meters,
        lengthSource = yarnDetails?.length?.source?.name,
        lengthBasis = yarnDetails?.lengthBasis?.name,
        yarnWeight = yarnDetails?.yarnWeight?.name,
        skeinCount = yarnDetails?.skeinCount,
        recommendedNeedleSizeMillimeters = yarnDetails?.recommendedNeedleSize?.millimeters,

        gaugeStitchesPer10cm = yarnDetails?.gauge?.stitchesPer10cm,
        gaugeRowsPer10cm = yarnDetails?.gauge?.rowsPer10cm,
        gaugeNeedleSizeMillimeters = yarnDetails?.gauge?.needleSize?.millimeters,
        gaugeNote = yarnDetails?.gauge?.note,

        dyeLot = yarnDetails?.dyeLot,
        ply = yarnDetails?.ply,
        twistDirection = yarnDetails?.twistDirection?.name,

        fiberForm = fiberDetails?.fiberForm?.name,
        fiberPreparation = fiberDetails?.preparation?.name,
        breedOrSource = fiberDetails?.breedOrSource,
        stapleLengthMeters = fiberDetails?.stapleLength?.meters,
        stapleLengthSource = fiberDetails?.stapleLength?.source?.name,
        micron = fiberDetails?.micron,
        intendedSpin = fiberDetails?.intendedSpin,
    )
}

fun InventoryItemEntity.toDomain(): InventoryItem {
    return InventoryItem(
        id = InventoryItemId(id),
        name = name,
        colorDescription = colorDescription,
        materialDescription = materialDescription,
        weight = weightGrams?.let { grams ->
            Weight(
                grams = grams,
                source = weightSource.toMeasurementSource(),
            )
        },
        location = location,
        status = InventoryItemStatus.valueOf(status),
        tags = emptyList(),
        photos = emptyList(),
        notes = notes,
        details = toProductDetails(),
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )
}

private fun InventoryItemEntity.toProductDetails() = when (ProductType.valueOf(productType)) {
    ProductType.Yarn -> YarnDetails(
        length = lengthMeters?.let { meters ->
            Length(
                meters = meters,
                source = lengthSource.toMeasurementSource(),
            )
        },
        lengthBasis = lengthBasis?.let { LengthBasis.valueOf(it) },
        yarnWeight = yarnWeight?.let { YarnWeight.valueOf(it) },
        skeinCount = skeinCount,
        recommendedNeedleSize = recommendedNeedleSizeMillimeters?.let { NeedleSize(it) },
        gauge = toGauge(),
        dyeLot = dyeLot,
        ply = ply,
        twistDirection = twistDirection?.let { TwistDirection.valueOf(it) },
    )

    ProductType.Fiber -> FiberDetails(
        fiberForm = fiberForm?.let { FiberForm.valueOf(it) },
        preparation = fiberPreparation?.let { FiberPreparation.valueOf(it) },
        breedOrSource = breedOrSource,
        stapleLength = stapleLengthMeters?.let { meters ->
            Length(
                meters = meters,
                source = stapleLengthSource.toMeasurementSource(),
            )
        },
        micron = micron,
        intendedSpin = intendedSpin,
    )
}

private fun InventoryItemEntity.toGauge(): Gauge? {
    if (
        gaugeStitchesPer10cm == null &&
        gaugeRowsPer10cm == null &&
        gaugeNeedleSizeMillimeters == null &&
        gaugeNote == null
    ) {
        return null
    }

    return Gauge(
        stitchesPer10cm = gaugeStitchesPer10cm,
        rowsPer10cm = gaugeRowsPer10cm,
        needleSize = gaugeNeedleSizeMillimeters?.let { NeedleSize(it) },
        note = gaugeNote,
    )
}

private fun String?.toMeasurementSource(): MeasurementSource {
    return this?.let { MeasurementSource.valueOf(it) }
        ?: MeasurementSource.Unknown
}