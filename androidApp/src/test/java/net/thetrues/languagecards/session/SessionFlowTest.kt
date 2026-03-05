package net.thetrues.languagecards.session

import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardResult
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionFlowTest {

    private fun card(id: String) = Card(id, "a", "b")

    @Test
    fun recordAnswer_whenAtSummary_returnsSameState() {
        val cards = listOf(card("1"))
        val state = SessionState(
            currentIndex = 1,
            cards = cards,
            results = listOf(CardResult("1", true)),
            direction = PracticeDirection.A_TO_B,
        )
        assertTrue(state.isAtSummary)

        val result = SessionFlow.recordAnswer(state, "1", false)

        assertEquals(state, result)
    }

    @Test
    fun recordAnswer_incrementsIndex() {
        val cards = listOf(card("1"), card("2"))
        val state = SessionState(
            currentIndex = 0,
            cards = cards,
            results = emptyList(),
            direction = PracticeDirection.A_TO_B,
        )

        val result = SessionFlow.recordAnswer(state, "1", true)

        assertEquals(1, result.currentIndex)
    }

    @Test
    fun recordAnswer_appendsResult() {
        val cards = listOf(card("1"), card("2"))
        val state = SessionState(
            currentIndex = 0,
            cards = cards,
            results = emptyList(),
            direction = PracticeDirection.A_TO_B,
        )

        val result = SessionFlow.recordAnswer(state, "1", true)

        assertEquals(1, result.results.size)
        assertEquals("1", result.results[0].cardId)
        assertTrue(result.results[0].wasHit)
    }

    @Test
    fun recordAnswer_onLastCard_setsIsAtSummary() {
        val cards = listOf(card("1"), card("2"))
        val state = SessionState(
            currentIndex = 1,
            cards = cards,
            results = listOf(CardResult("1", true)),
            direction = PracticeDirection.A_TO_B,
        )

        val result = SessionFlow.recordAnswer(state, "2", false)

        assertTrue(result.isAtSummary)
        assertEquals(2, result.currentIndex)
        assertEquals(2, result.results.size)
    }

    @Test
    fun recordAnswer_doesNotMutateOriginalState() {
        val cards = listOf(card("1"), card("2"))
        val state = SessionState(
            currentIndex = 0,
            cards = cards,
            results = emptyList(),
            direction = PracticeDirection.A_TO_B,
        )

        SessionFlow.recordAnswer(state, "1", true)

        assertEquals(0, state.currentIndex)
        assertTrue(state.results.isEmpty())
    }
}
