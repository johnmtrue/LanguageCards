package net.thetrues.languagecards.repository

import net.thetrues.languagecards.model.CardStats

/**
 * Abstraction for persisting hit/miss stats per card.
 * Implemented on Android with DataStore, on iOS with KMP DataStore.
 */
interface StatsRepository {
    fun record(cardId: String, wasHit: Boolean)
    fun getStats(cardId: String): CardStats?
}
