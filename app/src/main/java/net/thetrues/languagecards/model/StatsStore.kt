package net.thetrues.languagecards.model

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

private val Context.statsDataStore: DataStore<Preferences> by preferencesDataStore(name = "card_stats")

private const val STATS_KEY = "stats"

/**
 * Persistent store of hit/miss per card. Loads from DataStore on init, saves on each record.
 */
class StatsStore(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val statsByCardId = mutableMapOf<String, CardStats>()

    init {
        runBlocking {
            loadFromDataStore()
        }
    }

    fun record(cardId: String, wasHit: Boolean) {
        val stats = statsByCardId.getOrPut(cardId) { CardStats(cardId = cardId) }
        if (wasHit) stats.hits++ else stats.misses++
        scope.launch {
            saveToDataStore()
        }
    }

    fun getStats(cardId: String): CardStats? = statsByCardId[cardId]

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
            prefs[stringPreferencesKey(STATS_KEY)] = serialize(statsByCardId.values.toList())
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
