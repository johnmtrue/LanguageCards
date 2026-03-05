package net.thetrues.languagecards.data

import kotlinx.serialization.Serializable
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination

/**
 * JSON structure for deck files.
 * Used for import/export of decks.
 */
@Serializable
data class DeckFile(
    val languageCombo: DeckFileLanguageCombo,
    val deck: DeckFileDeck,
    val cards: List<DeckFileCard>,
)

@Serializable
data class DeckFileLanguageCombo(
    val id: String,
    val name: String,
    val sideAName: String,
    val sideBName: String,
)

@Serializable
data class DeckFileDeck(
    val id: String,
    val name: String,
)

@Serializable
data class DeckFileCard(
    val id: String,
    val lines: List<DeckFileCardLine>,
)

@Serializable
data class DeckFileCardLine(
    val sideA: String,
    val sideB: List<String>,
)

fun DeckFile.toDomain(): Pair<LanguageCombination, Deck> {
    val languageCombo = LanguageCombination(
        id = languageCombo.id,
        name = languageCombo.name,
        sideAName = languageCombo.sideAName,
        sideBName = languageCombo.sideBName,
        decks = emptyList(),
    )
    val cards = this.cards.map { card ->
        Card(
            id = card.id,
            lines = card.lines.map { line ->
                CardLine(sideA = line.sideA, sideB = line.sideB)
            },
        )
    }
    val deck = Deck(
        id = deck.id,
        name = deck.name,
        cards = cards,
    )
    return languageCombo to deck
}
