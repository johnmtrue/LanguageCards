package net.thetrues.languagecards.data

import net.thetrues.languagecards.db.Deck as DeckEntity
import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination

/** Delimiter for storing multiple sideB answers in a single DB column. */
private const val SIDE_B_DELIMITER = "\u0001"

/**
 * SQLDelight implementation of [DeckRepository].
 * Maps DB entities to domain models.
 */
class SqlDelightDeckRepository(
    private val database: LanguageCardsDatabase,
) : DeckRepository {

    override fun addDeck(deck: Deck, languageCombo: LanguageCombination) {
        if (deck.id.isBlank() || languageCombo.id.isBlank()) return
        database.languageComboQueries.transaction {
            val comboExists = database.languageComboQueries.selectById(languageCombo.id).executeAsList().isNotEmpty()
            if (!comboExists) {
                database.languageComboQueries.insert(
                    id = languageCombo.id,
                    name = languageCombo.name,
                    side_a_name = languageCombo.sideAName,
                    side_b_name = languageCombo.sideBName,
                )
            }
            database.deckQueries.insert(
                id = deck.id,
                name = deck.name,
                language_combo_id = languageCombo.id,
            )
            for ((index, card) in deck.cards.withIndex()) {
                insertCard(database, deck.id, card, index)
            }
        }
    }

    override fun addDeckFromJson(json: String): Result<Unit> = DeckFileParser.parse(json)
        .map { (languageCombo, deck) ->
            addDeck(deck, languageCombo)
        }

    override fun deleteDeck(deckId: String): Boolean {
        if (deckId.isBlank()) return false
        val deck = database.deckQueries.selectById(deckId).executeAsList().firstOrNull() ?: return false
        val comboId = deck.language_combo_id
        val cardIds = database.deckCardQueries.selectCardIdsByDeckId(deckId).executeAsList()

        database.deckQueries.transaction {
            for (cardId in cardIds) {
                database.cardStatsQueries.deleteByCardId(cardId)
            }
            database.deckCardQueries.deleteByDeckId(deckId)
            for (cardId in cardIds) {
                database.cardLineQueries.deleteByCardId(cardId)
                database.cardQueries.deleteById(cardId)
            }
            database.deckQueries.deleteById(deckId)
            val remaining = database.deckQueries.countByLanguageComboId(comboId).executeAsOne()
            if (remaining == 0L) {
                database.languageComboQueries.deleteById(comboId)
            }
        }
        return true
    }

    override fun getLanguageCombinations(): List<LanguageCombination> {
        val combos = database.languageComboQueries.selectAll().executeAsList()
        return combos
            .filter {
                it.id.trim().isNotBlank() && it.name.trim().isNotBlank() &&
                    it.side_a_name.trim().isNotBlank() && it.side_b_name.trim().isNotBlank()
            }
            .mapNotNull { combo ->
                val id = combo.id.trim()
                val name = combo.name.trim()
                val sideAName = combo.side_a_name.trim()
                val sideBName = combo.side_b_name.trim()
                val decks = database.deckQueries.selectByLanguageComboId(combo.id).executeAsList()
                    .mapNotNull { deckEntity -> toDeck(deckEntity) }
                try {
                    LanguageCombination(
                        id = id,
                        name = name,
                        sideAName = sideAName,
                        sideBName = sideBName,
                        decks = decks,
                    )
                } catch (_: Throwable) {
                    null
                }
            }
    }

    override fun getDeck(id: String): Deck? {
        val deckEntity = database.deckQueries.selectById(id).executeAsList().firstOrNull() ?: return null
        return toDeck(deckEntity)
    }

    private fun toDeck(deckEntity: DeckEntity): Deck? {
        val id = deckEntity.id.trim()
        val name = deckEntity.name.trim()
        if (id.isBlank() || name.isBlank()) return null
        val cardIds = database.deckCardQueries.selectCardIdsByDeckId(deckEntity.id).executeAsList()
        val cards = cardIds.mapNotNull { cardId -> getCard(cardId) }
        return try {
            Deck(id = id, name = name, cards = cards)
        } catch (_: Throwable) {
            null
        }
    }

    private fun getCard(cardId: String): Card? {
        val id = cardId.trim()
        if (id.isBlank()) return null
        val lines = database.cardLineQueries.selectByCardId(cardId).executeAsList()
        if (lines.isEmpty()) return null
        val cardLines = lines.map { line ->
            CardLine(
                sideA = line.side_a,
                sideB = parseSideB(line.side_b),
            )
        }
        return try {
            Card(id = id, lines = cardLines)
        } catch (_: Throwable) {
            null
        }
    }

    private fun parseSideB(stored: String): List<String> =
        if (stored.isEmpty()) emptyList()
        else stored.split(SIDE_B_DELIMITER)
}

/** Serialize sideB list for DB storage. */
fun serializeSideB(answers: List<String>): String = answers.joinToString(SIDE_B_DELIMITER)
