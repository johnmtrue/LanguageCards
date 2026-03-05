package net.thetrues.languagecards.data

import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.model.Card

/**
 * Seeds the database with [SampleData] on first run.
 * If [language_combo] is empty, inserts all language combinations, decks, cards, and card lines.
 */
fun seedDatabaseIfEmpty(database: LanguageCardsDatabase) {
    val count = database.languageComboQueries.selectCount().executeAsOne()
    if (count > 0L) return

    database.languageComboQueries.transaction {
        for (combo in SampleData.languageCombinations) {
            database.languageComboQueries.insert(
                id = combo.id,
                name = combo.name,
                side_a_name = combo.sideAName,
                side_b_name = combo.sideBName,
            )
            for (deck in combo.decks) {
                database.deckQueries.insert(
                    id = deck.id,
                    name = deck.name,
                    language_combo_id = combo.id,
                )
                for ((index, card) in deck.cards.withIndex()) {
                    insertCard(database, deck.id, card, index)
                }
            }
        }
    }
}

internal fun insertCard(database: LanguageCardsDatabase, deckId: String, card: Card, position: Int) {
    database.cardQueries.insert(id = card.id)
    database.deckCardQueries.insert(
        deck_id = deckId,
        card_id = card.id,
        position = position.toLong(),
    )
    for ((lineIndex, line) in card.lines.withIndex()) {
        val lineId = "${card.id}-L$lineIndex"
        database.cardLineQueries.insert(
            id = lineId,
            card_id = card.id,
            line_index = lineIndex.toLong(),
            side_a = line.sideA,
            side_b = serializeSideB(line.sideB),
        )
    }
}
