package de.avanzu.woolstash.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InventoryReferenceValueTest {
    @Test
    fun toInventoryReferenceName_trimsAndCollapsesWhitespace() {
        assertEquals(
            "Kiste Schlafzimmer",
            "  Kiste   Schlafzimmer  ".toInventoryReferenceName(),
        )
    }

    @Test
    fun toInventoryReferenceName_returnsNullForBlankInput() {
        assertNull("   ".toInventoryReferenceName())
        assertNull(null.toInventoryReferenceName())
    }
}
