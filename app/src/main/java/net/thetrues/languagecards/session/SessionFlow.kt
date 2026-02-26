package net.thetrues.languagecards.session

import net.thetrues.languagecards.model.CardResult
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.SessionState

/**
 * Session flow: start from a deck, then for each card record hit/miss and advance.
 * After the last card, state is at summary (do not loop back).
 */
object SessionFlow {

    /**
     * Start a new session with the given deck.
     * Cards are shown in deck order; current index 0 = first card.
     */
    fun startSession(deck: Deck, direction: PracticeDirection): SessionState = SessionState(
        currentIndex = 0,
        cards = deck.cards,
        results = emptyList(),
        direction = direction,
    )

    /**
     * Record the answer for the current card (hit or miss) and advance to the next card.
     * If this was the last card, the returned state will have [SessionState.isAtSummary] true.
     * Does not mutate; returns a new state.
     */
    fun recordAnswer(state: SessionState, cardId: String, wasHit: Boolean): SessionState {
        if (state.isAtSummary) return state
        val newResults = state.results + CardResult(cardId = cardId, wasHit = wasHit)
        return state.copy(
            currentIndex = state.currentIndex + 1,
            results = newResults,
        )
    }
}
