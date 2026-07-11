package de.avanzu.woolstash.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TagTest {
    @Test
    fun fromInput_normalizesTagName() {
        assertEquals(Tag("socken"), Tag.fromInput(" #Socken "))
        assertEquals(Tag("projektidee"), Tag.fromInput("PROJEKTIDEE"))
    }

    @Test
    fun fromInput_rejectsBlankTagName() {
        assertNull(Tag.fromInput("   #   "))
    }

    @Test
    fun normalizedDistinct_removesDuplicatesAfterNormalization() {
        val tags = listOf(
            Tag("Socken"),
            Tag("#socken"),
            Tag("natur"),
        ).normalizedDistinct()

        assertEquals(listOf(Tag("socken"), Tag("natur")), tags)
    }
}
