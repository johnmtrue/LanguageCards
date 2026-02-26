package net.thetrues.languagecards.model

/**
 * In-memory store of hit/miss per card. Updated when user commits an answer.
 */
class StatsStore {
    private val statsByCardId = mutableMapOf<String, CardStats>()

    fun record(cardId: String, wasHit: Boolean) {
        val stats = statsByCardId.getOrPut(cardId) { CardStats(cardId = cardId) }
        if (wasHit) stats.hits++ else stats.misses++
    }

    fun getStats(cardId: String): CardStats? = statsByCardId[cardId]
}
