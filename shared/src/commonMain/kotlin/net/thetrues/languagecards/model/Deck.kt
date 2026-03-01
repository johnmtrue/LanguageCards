package net.thetrues.languagecards.model

/**
 * A deck holds a list of cards (e.g. one French/English deck).
 */
data class Deck(
    val id: String,
    val name: String,
    val cards: List<Card>,
)
