package net.thetrues.languagecards.repository

import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.model.PracticeDirection

/**
 * Abstraction for persisting hit/miss stats per card and per direction.
 * Implemented with SQLDelight (card_stats table).
 */
interface StatsRepository {
    fun record(cardId: String, direction: PracticeDirection, wasHit: Boolean)
    fun getStats(cardId: String, direction: PracticeDirection): CardStats?
    fun getAllStats(): List<CardStats>
    fun clearAllStats()
}
