package net.thetrues.languagecards.data

import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination

/**
 * Hardcoded language combinations and decks for the prototype.
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
        Card("41", "sweet", listOf("doux", "sucré")),
        Card("42", "under", "sous"),
        Card("43", "to dream", "rêver"),
        Card("44", "people", listOf("gens", "personnes")),
        Card("45", "to cry", "pleurer"),
        Card("46", "market", "marché"),
        Card("47", "body", "corps"),
        Card("48", "to mix", "mélanger"),
        Card("49", "world", "monde"),
        Card("50", "except", "sauf"),
        Card("51", "was", "était"),
        Card("52", "tire", "pneu"),
        Card("53", "to tire", "fatiguer"),
        Card("54", "alone", "seul"),
        Card("55", "silk", "soie"),
        Card("56", "piece", listOf("pièce", "morceau")),
        Card("57", "I can't", listOf("Je ne peux pas", "Je n'arrive pas")),
        Card("58", "unplug", "débrancher"),
        Card("59", "to dry", "sécher"),
        Card("60", "upstairs", listOf("à l'étage", "en haut")),
        Card("61", "sometimes", "parfois"),
        Card("62", "hole", "trou"),
        Card("63", "tight", "serré"),
        Card("64", "delete", "supprimer"),
        Card("65", "a folder", "un dossier"),
        Card("66", "to slip", "glisser"),
        Card("67", "plugin", "branche"),
        Card("68", "flag", listOf("drapeau", "signaler")),
        Card("69", "obviously", "évidemment"),
        Card("70", "dishwasher", "lave-vaisselle"),
        Card("71", "nightstand", "table de chevet"),
        Card("72", "later", listOf("plus tard", "tout à l'heure")),
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

    private val frenchBasicsDeck = Deck(
        id = "french-1",
        name = "French — Basics",
        cards = cards,
    )

    private val frenchPastTenseDeck = Deck(
        id = "french-past",
        name = "French - Past Tense",
        cards = pastTenseCards,
    )

    private val frenchConversationCards = listOf(
        Card("fc-1", "How are you?", listOf("Comment allez-vous?", "Comment vas-tu?")),
        Card(
            id = "fc-d1",
            lines = listOf(
                CardLine("Hi, Paul. Is everything going well?", "Salut, Paul, Ça va bien?"),
                CardLine("Very well / Great, thanks", "Très bien, merci"),
            ),
        ),
        Card("fc-2", "How's it going?", listOf("Comment ça va?", "Ça va?")),
        Card(
            id = "fc-d2",
            lines = listOf(
                CardLine("Hello, Pierre. How are you?", "Bonjour, Pierre, Comment ça va?"),
                CardLine("Fine, thank you", "Bien, merci"),
            ),
        ),
        Card("fc-3", "Is everything OK?", listOf("Ça va?", "Tout va bien?", "Tout est OK?")),
        Card(
            id = "fc-d3",
            lines = listOf(
                CardLine("Is he going to join us?", "Il va nous rejoindre?"),
                CardLine("Yes, he is (that's it).  At five PM.", "C'est ça. À dix-sept heures."),
            ),
        ),
        Card("fc-4", "everything is OK", listOf("Tout va bien", "Ça va")),
        Card(
            id = "fc-d4",
            lines = listOf(
                CardLine("I can't go out.  I'm sick.", "Je ne peux pas sortir.  Je suis malade."),
                CardLine("I see. Rest well!", "Je vois.  Repose-toi bien!"),
            ),
        ),
        Card("fc-5", "I'm not bad", listOf("pas mal", "Ça va pas mal", "Je ne vais pas mal")),
        Card(
            id = "fc-d5",
            lines = listOf(
                CardLine("You have to use vous with people you don't know", "Il faut vouvoyer les gens qu'on ne connait pas."),
                CardLine("Oh yes! I understand (I get it).", "Ah oui!  Je pige."),
            ),
        ),
        Card("fc-6", "I am doing well", "Je vais bien"),
        Card(
            id = "fc-d6",
            lines = listOf(
                CardLine("Thanks for this card.", "Merci pour cette carte."),
                CardLine("Don't mention it.", "Il n'y a pas de quoi. / Pas de quoi."),
            ),
        ),
        Card("fc-7", "I am doing very well", "Je vais très bien"),
        Card("fc-8", "I see", "Je vois"),
        Card("fc-9", "I understand", listOf("Je comprends", "Je pige")),
        Card("fc-10", "Thanks for", listOf("Merci pour", "Merci de")),
        Card("fc-11", "Yes he is", listOf("Oui, il l'est", "C'est ça")),
        Card("fc-12", "Don't mention it", listOf("De rien", "Pas de quoi", "Il n'y a pas de quoi.")),
    )

    private val frenchConversationDeck = Deck(
        id = "french-conversation",
        name = "French - Conversation",
        cards = frenchConversationCards,
    )

    private val spanishBasicsCards = listOf(
        Card("es-1", "Hello", "Hola"),
        Card("es-2", "Thank you", "Gracias"),
        Card("es-3", "Goodbye", "Adiós"),
        Card("es-4", "Yes", "Sí"),
        Card("es-5", "No", "No"),
        Card("es-6", "Please", "Por favor"),
        Card("es-7", "Water", "Agua"),
        Card("es-8", "Coffee", "Café"),
        Card("es-9", "Bread", "Pan"),
        Card("es-10", "Help", "Ayuda"),
    )

    private val spanishBasicsDeck = Deck(
        id = "spanish-1",
        name = "Spanish — Basics",
        cards = spanishBasicsCards,
    )

    /** All language combinations. Decks are grouped under each combination. */
    val languageCombinations: List<LanguageCombination> = listOf(
        LanguageCombination(
            id = "en-fr",
            name = "English – French",
            sideAName = "English",
            sideBName = "French",
            decks = listOf(frenchBasicsDeck, frenchPastTenseDeck, frenchConversationDeck),
        ),
        LanguageCombination(
            id = "en-es",
            name = "English – Spanish",
            sideAName = "English",
            sideBName = "Spanish",
            decks = listOf(spanishBasicsDeck),
        ),
    )

    /** All decks (flattened). For backwards compatibility until Phase 2/3. */
    val decks: List<Deck> = languageCombinations.flatMap { it.decks }
}
