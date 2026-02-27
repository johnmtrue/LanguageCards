package net.thetrues.languagecards.model

/**
 * Hardcoded French/English deck and cards for the minimum prototype.
 */
object SampleData {
    private val cards = listOf(
        Card("1", "Hello", listOf("Bonjour", "Salut")),
        Card("2", "Thank you", "Merci"),
        Card("3", "Good morning", listOf("Bonjour", "Bon matin")),
        Card("4", "Goodbye", listOf("Au revoir", "Salut")),
        Card("5", "Please", listOf("S'il vous plaît", "S'il te plaît")),
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
        Card("21", "Bread", "Pain"),
        Card("22", "Milk", "Lait"),
        Card("23", "Butter", "Beurre"),
        Card("24", "Cheese", "Fromage"),
        Card("25", "Sugar", "Sucre"),
        Card("26", "Salt", "Sel"),
        Card("27", "Tea", "Thé"),
        Card("28", "Wine", "Vin"),
        Card("29", "Beer", "Bière"),
        Card("30", "Breakfast", "Petit-déjeuner"),
        Card("31", "Lunch", "Déjeuner"),
        Card("32", "Dinner", "Dîner"),
        Card("33", "Restaurant", "Restaurant"),
        Card("34", "Train station", "Gare"),
        Card("35", "Airport", "Aéroport"),
        Card("36", "Hotel", "Hôtel"),
        Card("37", "Bathroom", "Toilettes"),
        Card("38", "Left", "Gauche"),
        Card("39", "Right", "Droite"),
        Card("40", "Straight ahead", "Tout droit"),
    )

    /** Single French deck for the minimum version. */
    val defaultDeck: Deck = Deck(
        id = "french-1",
        name = "French — Basics",
        cards = cards,
    )
}
