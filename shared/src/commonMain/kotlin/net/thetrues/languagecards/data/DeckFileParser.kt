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

    /**
     * Returns a user-facing message for a parse/validation failure.
     * Use when [parse] fails to show a clear JSON file format error.
     */
    fun formatParseError(t: Throwable?): String {
        if (t == null) return "Invalid deck file format."
        val message = t.message?.takeIf { it.isNotBlank() } ?: t::class.simpleName.orEmpty()
        return "Invalid deck file format: $message"
    }
}
