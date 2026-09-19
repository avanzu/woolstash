package de.avanzu.woolstash.ui.inventory

import de.avanzu.woolstash.domain.model.FiberDetails
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.Tag
import de.avanzu.woolstash.domain.model.Weight
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryTransformationTest {
    @Test
    fun toDyedFiberInventoryItem_keepsFiberDetailsAndMarksPreparationAsDyed() {
        val source = InventoryItem(
            name = "Merino natur",
            materialDescription = "Merino",
            weight = Weight(120.0),
            manufacturer = "Faserhof",
            purchaseSource = "Wollmarkt",
            notes = "Schon gewaschen",
            details = FiberDetails(
                fiberForm = FiberForm.Top,
                preparation = FiberPreparation.Combed,
                breedOrSource = "Merino",
                micron = 19.5,
                intendedSpin = "Lace",
            ),
        )
        val input = CreateInventoryItemInput(
            name = "Merino gefärbt",
            colorDescription = "Petrol",
            materialDescription = source.materialDescription,
            weightGrams = 80.0,
            location = "Faserkiste",
            manufacturer = source.manufacturer,
            purchaseSource = source.purchaseSource,
            tags = listOf(Tag("handgefärbt")),
        )

        val transformed = input.toDyedFiberInventoryItem(source)
        val details = transformed.details as FiberDetails

        assertEquals("Merino gefärbt", transformed.name)
        assertEquals("Petrol", transformed.colorDescription)
        assertEquals(80.0, transformed.weight?.grams ?: 0.0, 0.0)
        assertEquals(FiberForm.Top, details.fiberForm)
        assertEquals(FiberPreparation.Dyed, details.preparation)
        assertEquals("Merino", details.breedOrSource)
        assertEquals(19.5, details.micron ?: 0.0, 0.0)
        assertEquals("Lace", details.intendedSpin)
        assertEquals("Faserhof", transformed.manufacturer)
        assertEquals("Wollmarkt", transformed.purchaseSource)
        assertEquals("Schon gewaschen", transformed.notes)
    }
}
