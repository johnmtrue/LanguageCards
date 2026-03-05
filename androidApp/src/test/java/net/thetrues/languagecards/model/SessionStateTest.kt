package net.thetrues.languagecards.model

import net.thetrues.languagecards.model.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionStateTest {

    private fun card(id: String) = Card(id, "a", "b")

    @Test
    fun isAtSummary_trueWhenCurrentIndexEqualsCardsSize() {
        val cards = listOf(card("1"))
        val state = SessionState(
            currentIndex = 1,
            cards = cards,
            results = listOf(CardResult("1", true)),
            direction = PracticeDirection.A_TO_B,
        )
        assertTrue(state.isAtSummary)
    }

    @Test
    fun isAtSummary_trueWhenCurrentIndexExceedsCardsSize() {
        val cards = listOf(card("1"))
        val state = SessionState(
            currentIndex = 2,
            cards = cards,
            results = emptyList(),
            direction = PracticeDirection.A_TO_B,
        )
        assertTrue(state.isAtSummary)
    }

    @Test
    fun isAtSummary_falseWhenCurrentIndexLessThanCardsSize() {
        val cards = listOf(card("1"), card("2"))
        val state = SessionState(
            currentIndex = 0,
            cards = cards,
            results = emptyList(),
            direction = PracticeDirection.A_TO_B,
        )
        assertFalse(state.isAtSummary)
    }

    @Test
    fun currentCard_returnsCardAtCurrentIndex() {
        val c1 = card("1")
        val c2 = card("2")
        val state = SessionState(
            currentIndex = 1,
            cards = listOf(c1, c2),
            results = listOf(CardResult("1", true)),
            direction = PracticeDirection.A_TO_B,
        )
        assertEquals(c2, state.currentCard)
    }

    @Test
    fun currentCard_returnsNullWhenAtSummary() {
        val cards = listOf(card("1"))
        val state = SessionState(
            currentIndex = 1,
            cards = cards,
            results = listOf(CardResult("1", true)),
            direction = PracticeDirection.A_TO_B,
        )
        assertNull(state.currentCard)
    }

    @Test
    fun sessionHits_countsCorrectHits() {
        val state = SessionState(
            currentIndex = 2,
            cards = listOf(card("1"), card("2")),
            results = listOf(
                CardResult("1", true),
                CardResult("2", false),
            ),
            direction = PracticeDirection.A_TO_B,
        )
        assertEquals(1, state.sessionHits)
    }

    @Test
    fun sessionMisses_countsCorrectMisses() {
        val state = SessionState(
            currentIndex = 2,
            cards = listOf(card("1"), card("2")),
            results = listOf(
                CardResult("1", true),
                CardResult("2", false),
            ),
            direction = PracticeDirection.A_TO_B,
        )
        assertEquals(1, state.sessionMisses)
    }
}
