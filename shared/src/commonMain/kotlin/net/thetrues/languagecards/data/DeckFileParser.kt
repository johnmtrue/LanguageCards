package net.thetrues.languagecards.data

import kotlinx.serialization.json.Json
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination

/**
 * Parses deck JSON files into domain models.
 */
object DeckFileParser {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parses JSON string to [DeckFile] and converts to domain models.
     * @return Result with (LanguageCombination, Deck) or failure
     */
    fun parse(jsonString: String): Result<Pair<LanguageCombination, Deck>> = runCatching {
        val deckFile = json.decodeFromString<DeckFile>(jsonString)
        deckFile.toDomain()
    }
}
