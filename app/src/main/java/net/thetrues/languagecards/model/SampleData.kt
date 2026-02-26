package net.thetrues.languagecards.model

/**
 * Hardcoded French/English deck and cards for the minimum prototype.
 */
object SampleData {
    private val cards = listOf(
        Card("1", "Hello", "Bonjour"),
        Card("2", "Thank you", "Merci"),
        Card("3", "Good morning", "Bonjour"),
        Card("4", "Goodbye", "Au revoir"),
        Card("5", "Please", "S'il vous plaît"),
        Card("6", "Yes", "Oui"),
        Card("7", "No", "Non"),
        Card("8", "Water", "L'eau"),
        Card("9", "Coffee", "Café"),
        Card("10", "I don't understand", "Je ne comprends pas"),
    )

    /** Single French deck for the minimum version. */
    val defaultDeck: Deck = Deck(
        id = "french-1",
        name = "French — Basics",
        cards = cards,
    )
}
