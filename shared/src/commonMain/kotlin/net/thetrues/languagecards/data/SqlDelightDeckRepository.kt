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

    override fun getLanguageCombinations(): List<LanguageCombination> {
        val combos = database.languageComboQueries.selectAll().executeAsList()
        return combos.map { combo ->
            val decks = database.deckQueries.selectByLanguageComboId(combo.id).executeAsList()
            LanguageCombination(
                id = combo.id,
                name = combo.name,
                sideAName = combo.side_a_name,
                sideBName = combo.side_b_name,
                decks = decks.map { deckEntity -> toDeck(deckEntity) },
            )
        }
    }

    override fun getDeck(id: String): Deck? {
        val deckEntity = database.deckQueries.selectById(id).executeAsList().firstOrNull() ?: return null
        return toDeck(deckEntity)
    }

    private fun toDeck(deckEntity: DeckEntity): Deck {
        val cardIds = database.deckCardQueries.selectCardIdsByDeckId(deckEntity.id).executeAsList()
        val cards = cardIds.mapNotNull { cardId -> getCard(cardId) }
        return Deck(
            id = deckEntity.id,
            name = deckEntity.name,
            cards = cards,
        )
    }

    private fun getCard(cardId: String): Card? {
        val lines = database.cardLineQueries.selectByCardId(cardId).executeAsList()
        if (lines.isEmpty()) return null
        val cardLines = lines.map { line ->
            CardLine(
                sideA = line.side_a,
                sideB = parseSideB(line.side_b),
            )
        }
        return Card(id = cardId, lines = cardLines)
    }

    private fun parseSideB(stored: String): List<String> =
        if (stored.isEmpty()) emptyList()
        else stored.split(SIDE_B_DELIMITER)
}

/** Serialize sideB list for DB storage. */
fun serializeSideB(answers: List<String>): String = answers.joinToString(SIDE_B_DELIMITER)
