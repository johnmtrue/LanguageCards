package net.thetrues.languagecards.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SerializeSideBTest {

    private val delimiter = "\u0001"

    @Test
    fun emptyList_returnsEmptyString() {
        assertEquals("", serializeSideB(emptyList()))
    }

    @Test
    fun singleAnswer_returnsAnswer() {
        assertEquals("hello", serializeSideB(listOf("hello")))
    }

    @Test
    fun multipleAnswers_joinsWithDelimiter() {
        assertEquals("a${delimiter}b", serializeSideB(listOf("a", "b")))
    }

    @Test
    fun roundTrip_splitProducesOriginalList() {
        val answers = listOf("Bonjour", "Salut", "Coucou")
        val serialized = serializeSideB(answers)
        val parsed = serialized.split(delimiter)
        assertEquals(answers, parsed)
    }
}
