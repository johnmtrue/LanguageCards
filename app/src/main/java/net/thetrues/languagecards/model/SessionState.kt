package net.thetrues.languagecards.model

/**
 * Immutable state for one practice session.
 * Flow: card screen (show prompt) → user answers → next card or summary.
 */
data class SessionState(
    val currentIndex: Int,
    val cards: List<Card>,
    val results: List<CardResult>,
    val direction: PracticeDirection,
) {
    /** True when all cards have been answered; show summary screen. */
    val isAtSummary: Boolean
        get() = currentIndex >= cards.size

    /** The card to show right now, or null if at summary. */
    val currentCard: Card?
        get() = cards.getOrNull(currentIndex)

    /** Total hits so far this session. */
    val sessionHits: Int
        get() = results.count { it.wasHit }

    /** Total misses so far this session. */
    val sessionMisses: Int
        get() = results.count { !it.wasHit }
}
