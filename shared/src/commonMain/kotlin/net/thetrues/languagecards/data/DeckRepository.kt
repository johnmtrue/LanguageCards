package net.thetrues.languagecards.data

import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination

/**
 * Repository for loading decks and language combinations.
 * Implemented with SQLDelight in Phase 2.
 */
interface DeckRepository {
    /**
     * Returns all language combinations with their decks and cards.
     * Returns empty list if database is not yet seeded.
     */
    fun getLanguageCombinations(): List<LanguageCombination>

    /**
     * Returns a deck by ID, or null if not found.
     */
    fun getDeck(id: String): Deck?
}
