package net.thetrues.languagecards.platform

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.repository.StatsRepository

private val Context.statsDataStore: DataStore<Preferences> by preferencesDataStore(name = "card_stats")

private const val STATS_KEY = "stats"

/**
 * Android implementation of StatsRepository using DataStore.
 */
class AndroidStatsStore(
    private val context: Context,
    private val scope: CoroutineScope,
) : StatsRepository {

    private val statsByCardId = mutableMapOf<String, CardStats>()

    init {
        runBlocking {
            loadFromDataStore()
        }
    }

    override fun record(cardId: String, wasHit: Boolean) {
        val stats = statsByCardId.getOrPut(cardId) { CardStats(cardId = cardId) }
        if (wasHit) stats.hits++ else stats.misses++
        scope.launch {
            saveToDataStore()
        }
    }

    override fun getStats(cardId: String): CardStats? = statsByCardId[cardId]

    override fun getAllStats(): List<CardStats> = statsByCardId.values.toList()

    override fun clearAllStats() {
        statsByCardId.clear()
        scope.launch {
            saveToDataStore()
        }
    }

    private suspend fun loadFromDataStore() {
        val prefs = context.statsDataStore.data.first()
        val serialized = prefs[stringPreferencesKey(STATS_KEY)] ?: return
        val loaded = deserialize(serialized)
        loaded.forEach { (id, stats) ->
            statsByCardId[id] = stats
        }
    }

    private suspend fun saveToDataStore() {
        context.statsDataStore.edit { prefs ->
            val serialized = if (statsByCardId.isEmpty()) "" else serialize(statsByCardId.values.toList())
            prefs[stringPreferencesKey(STATS_KEY)] = serialized
        }
    }

    private fun serialize(stats: List<CardStats>): String =
        stats.joinToString("|") { "${it.cardId}:${it.hits}:${it.misses}" }

    private fun deserialize(value: String): Map<String, CardStats> {
        if (value.isEmpty()) return emptyMap()
        return value.split("|").mapNotNull { part ->
            val tokens = part.split(":")
            if (tokens.size == 3) {
                val (id, hits, misses) = tokens
                id to CardStats(
                    cardId = id,
                    hits = hits.toIntOrNull() ?: 0,
                    misses = misses.toIntOrNull() ?: 0,
                )
            } else null
        }.toMap()
    }
}
