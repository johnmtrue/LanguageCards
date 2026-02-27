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

    private val pastTenseCards = listOf(
        Card("pt-1", "I went", "Je suis allé"),
        Card("pt-2", "I ate", "J'ai mangé"),
        Card("pt-3", "I drank", "J'ai bu"),
        Card("pt-4", "I saw", "J'ai vu"),
        Card("pt-5", "I spoke", "J'ai parlé"),
        Card("pt-6", "I walked", "J'ai marché"),
        Card("pt-7", "I worked", "J'ai travaillé"),
        Card("pt-8", "I slept", "J'ai dormi"),
        Card("pt-9", "I wrote", "J'ai écrit"),
        Card("pt-10", "I read", "J'ai lu"),
        Card("pt-11", "I bought", "J'ai acheté"),
        Card("pt-12", "I sold", "J'ai vendu"),
        Card("pt-13", "I came", "Je suis venu"),
        Card("pt-14", "I left", "Je suis parti"),
        Card("pt-15", "I arrived", "Je suis arrivé"),
        Card("pt-16", "I finished", "J'ai fini"),
        Card("pt-17", "I started", "J'ai commencé"),
        Card("pt-18", "I took", "J'ai pris"),
        Card("pt-19", "I made", "J'ai fait"),
        Card("pt-20", "I had", "J'ai eu"),
    )

    /** French basics deck. */
    val defaultDeck: Deck = Deck(
        id = "french-1",
        name = "French — Basics",
        cards = cards,
    )

    /** French past tense verb phrases deck. */
    val pastTenseDeck: Deck = Deck(
        id = "french-past",
        name = "French - Past Tense",
        cards = pastTenseCards,
    )

    /** All available decks. */
    val decks: List<Deck> = listOf(defaultDeck, pastTenseDeck)
}
