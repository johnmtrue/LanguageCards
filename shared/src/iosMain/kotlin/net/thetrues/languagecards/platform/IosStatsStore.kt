package net.thetrues.languagecards.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import okio.toPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.repository.StatsRepository
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private const val STATS_KEY = "stats"
private const val DATA_STORE_FILE = "card_stats.preferences_pb"

private val dataStore: DataStore<Preferences> by lazy {
    val pathString = run {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        requireNotNull(documentDirectory?.path) { "Could not get Documents directory" } + "/$DATA_STORE_FILE"
    }
    androidx.datastore.preferences.core.PreferenceDataStoreFactory.createWithPath(
        produceFile = { pathString.toPath() }
    )
}

/**
 * iOS implementation of StatsRepository using KMP DataStore.
 * Uses singleton DataStore instance per process (required on iOS).
 */
class IosStatsStore(
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

    private suspend fun loadFromDataStore() {
        val prefs = dataStore.data.first()
        val serialized = prefs[stringPreferencesKey(STATS_KEY)] ?: return
        val loaded = deserialize(serialized)
        loaded.forEach { (id, stats) ->
            statsByCardId[id] = stats
        }
    }

    private suspend fun saveToDataStore() {
        dataStore.edit { prefs ->
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
