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

    /**
     * Adds a deck and its cards. Creates the language combo if it does not exist.
     */
    fun addDeck(deck: Deck, languageCombo: LanguageCombination)

    /**
     * Parses JSON and adds the deck. Creates language combo if new.
     * @return Result.success(Unit) or Result.failure with exception
     */
    fun addDeckFromJson(json: String): Result<Unit>

    /**
     * Deletes a deck and its cards. Removes the language combo if it was the last deck.
     * @return true if deck was found and deleted, false if not found
     */
    fun deleteDeck(deckId: String): Boolean
}
