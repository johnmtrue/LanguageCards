package net.thetrues.languagecards.repository

import net.thetrues.languagecards.model.CardStats

/**
 * Abstraction for persisting hit/miss stats per card.
 * Implemented with SQLDelight (card_stats table).
 */
interface StatsRepository {
    fun record(cardId: String, wasHit: Boolean)
    fun getStats(cardId: String): CardStats?
    fun getAllStats(): List<CardStats>
    fun clearAllStats()
}
