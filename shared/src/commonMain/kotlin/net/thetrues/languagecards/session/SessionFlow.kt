package net.thetrues.languagecards.session

import net.thetrues.languagecards.model.CardResult
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.SessionOptions
import net.thetrues.languagecards.model.SessionState
import net.thetrues.languagecards.repository.StatsRepository

/**
 * Session flow: start from a deck, then for each card record hit/miss and advance.
 * After the last card, state is at summary (do not loop back).
 */
object SessionFlow {

    const val DEFAULT_SESSION_SIZE = 10

    /**
     * Start a new session with the given deck.
     * Selects cards using [CardSelector]: half weighted toward most-missed cards,
     * half random from the remainder, then shuffled.
     */
    fun startSession(
        deck: Deck,
        direction: PracticeDirection,
        statsRepository: StatsRepository,
        options: SessionOptions = SessionOptions.Default,
    ): SessionState {
        val cardIds = deck.cards.map { it.id }
        val statsByCard = statsRepository.getStatsForCards(cardIds, direction)
        val selectedCards = CardSelector.selectCards(
            deck = deck,
            direction = direction,
            sessionSize = DEFAULT_SESSION_SIZE,
            statsByCard = statsByCard,
        )
        return SessionState(
            currentIndex = 0,
            cards = selectedCards,
            results = emptyList(),
            direction = direction,
            options = options,
        )
    }

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
