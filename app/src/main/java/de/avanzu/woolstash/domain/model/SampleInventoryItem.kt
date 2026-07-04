package de.avanzu.woolstash.domain.model

object SampleInventoryItems {
    val items = listOf(
        InventoryItem(

            name = "Merino Sockenwolle",
            colorDescription = "Blaugrün",
            materialDescription = "Merino / Polyamid",
            weight = Weight(100.0, MeasurementSource.Manufacturer),
            location = "Kiste Schlafzimmer",
            tags = listOf(Tag("socken"), Tag("handgefärbt")),
            details = YarnDetails(
                length = Length(420.0, MeasurementSource.Manufacturer),
                lengthBasis = LengthBasis.Total,
                yarnWeight = YarnWeight.Fingering,
                recommendedNeedleSize = NeedleSize(2.5),
            ),
        ),
        InventoryItem(
            name = "Kammzug",
            colorDescription = "Naturgrau",
            materialDescription = "Bluefaced Leicester",
            weight = Weight(200.0, MeasurementSource.Measured),
            location = "Faserkiste",
            tags = listOf(Tag("spinnen"), Tag("natur")),
            details = FiberDetails(
                fiberForm = FiberForm.Top,
                preparation = FiberPreparation.Combed,
                breedOrSource = "BFL",
            ),
        ),
    )
}