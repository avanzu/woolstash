package de.avanzu.woolstash.data.local

import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.InventoryReferenceType
import de.avanzu.woolstash.domain.model.YarnDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryReferenceValueMapperTest {
    @Test
    fun toReferenceValueEntities_mapsCoreReferenceFields() {
        val item = InventoryItem(
            name = "Sockenwolle",
            location = "Kiste Schlafzimmer",
            manufacturer = "Malabrigo",
            purchaseSource = "Wollgeschäft",
            details = YarnDetails(),
        )

        assertEquals(
            listOf(
                InventoryReferenceValueEntity(
                    type = InventoryReferenceType.Location.name,
                    name = "Kiste Schlafzimmer",
                ),
                InventoryReferenceValueEntity(
                    type = InventoryReferenceType.Manufacturer.name,
                    name = "Malabrigo",
                ),
                InventoryReferenceValueEntity(
                    type = InventoryReferenceType.PurchaseSource.name,
                    name = "Wollgeschäft",
                ),
            ),
            item.toReferenceValueEntities(),
        )
    }
}
