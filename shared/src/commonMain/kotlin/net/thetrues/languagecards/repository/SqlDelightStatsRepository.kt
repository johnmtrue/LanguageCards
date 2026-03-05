package net.thetrues.languagecards.repository

import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.model.PracticeDirection

/**
 * SQLDelight implementation of [StatsRepository] using the [card_stats] table.
 */
class SqlDelightStatsRepository(
    private val database: LanguageCardsDatabase,
) : StatsRepository {

    override fun record(cardId: String, direction: PracticeDirection, wasHit: Boolean) {
        database.cardStatsQueries.insertIfNotExists(cardId, direction.name)
        if (wasHit) {
            database.cardStatsQueries.recordHit(cardId, direction.name)
        } else {
            database.cardStatsQueries.recordMiss(cardId, direction.name)
        }
    }

    override fun getStats(cardId: String, direction: PracticeDirection): CardStats? {
        val row = database.cardStatsQueries.selectByCardIdAndDirection(cardId, direction.name)
            .executeAsList()
            .firstOrNull() ?: return null
        return CardStats(
            cardId = row.card_id,
            direction = PracticeDirection.valueOf(row.direction),
            hits = row.hits.toInt(),
            misses = row.misses.toInt(),
        )
    }

    override fun getAllStats(): List<CardStats> {
        return database.cardStatsQueries.selectAll().executeAsList().map { row ->
            CardStats(
                cardId = row.card_id,
                direction = PracticeDirection.valueOf(row.direction),
                hits = row.hits.toInt(),
                misses = row.misses.toInt(),
            )
        }
    }

    override fun clearAllStats() {
        database.cardStatsQueries.deleteAll()
    }
}
