package net.thetrues.languagecards.repository

import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.model.CardStats

/**
 * SQLDelight implementation of [StatsRepository] using the [card_stats] table.
 */
class SqlDelightStatsRepository(
    private val database: LanguageCardsDatabase,
) : StatsRepository {

    override fun record(cardId: String, wasHit: Boolean) {
        database.cardStatsQueries.insertIfNotExists(cardId)
        if (wasHit) {
            database.cardStatsQueries.recordHit(cardId)
        } else {
            database.cardStatsQueries.recordMiss(cardId)
        }
    }

    override fun getStats(cardId: String): CardStats? {
        val row = database.cardStatsQueries.selectByCardId(cardId).executeAsList().firstOrNull() ?: return null
        return CardStats(
            cardId = row.card_id,
            hits = row.hits.toInt(),
            misses = row.misses.toInt(),
        )
    }

    override fun getAllStats(): List<CardStats> {
        return database.cardStatsQueries.selectAll().executeAsList().map { row ->
            CardStats(
                cardId = row.card_id,
                hits = row.hits.toInt(),
                misses = row.misses.toInt(),
            )
        }
    }

    override fun clearAllStats() {
        database.cardStatsQueries.deleteAll()
    }
}
