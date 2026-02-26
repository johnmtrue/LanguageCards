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
        Card("11", "Excuse me", "Excusez-moi"),
        Card("12", "How are you?", "Comment allez-vous?"),
        Card("13", "I'm fine", "Je vais bien"),
        Card("14", "My name is", "Je m'appelle"),
        Card("15", "Nice to meet you", "Enchanté"),
        Card("16", "Help", "Aide"),
        Card("17", "Where is", "Où est"),
        Card("18", "I would like", "Je voudrais"),
        Card("19", "The bill please", "L'addition s'il vous plaît"),
        Card("20", "Cheers", "Santé"),
    )

    /** Single French deck for the minimum version. */
    val defaultDeck: Deck = Deck(
        id = "french-1",
        name = "French — Basics",
        cards = cards,
    )
}
