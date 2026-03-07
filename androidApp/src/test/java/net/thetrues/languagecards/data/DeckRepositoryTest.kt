package net.thetrues.languagecards.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeckRepositoryTest {

    private lateinit var database: LanguageCardsDatabase
    private lateinit var repository: SqlDelightDeckRepository

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LanguageCardsDatabase.Schema.create(driver)
        database = LanguageCardsDatabase(driver)
        repository = SqlDelightDeckRepository(database)
    }

    @Test
    fun addDeck_createsNewLanguageComboAndDeck() {
        val combo = LanguageCombination(
            id = "en-fr",
            name = "English – French",
            sideAName = "English",
            sideBName = "French",
            decks = emptyList(),
        )
        val deck = Deck(
            id = "deck-1",
            name = "French Basics",
            cards = listOf(
                Card("c1", "Hello", "Bonjour"),
                Card("c2", "Thank you", "Merci"),
            ),
        )

        repository.addDeck(deck, combo)

        val combos = repository.getLanguageCombinations()
        assertEquals(1, combos.size)
        assertEquals("en-fr", combos[0].id)
        assertEquals("English – French", combos[0].name)
        assertEquals(1, combos[0].decks.size)
        assertEquals("deck-1", combos[0].decks[0].id)
        assertEquals("French Basics", combos[0].decks[0].name)
        assertEquals(2, combos[0].decks[0].cards.size)
        assertEquals("Hello", combos[0].decks[0].cards[0].sideA)
        assertEquals(listOf("Bonjour"), combos[0].decks[0].cards[0].sideB)
    }

    @Test
    fun addDeck_toExistingLanguageCombo_addsDeckOnly() {
        val combo = LanguageCombination(
            id = "en-fr",
            name = "English – French",
            sideAName = "English",
            sideBName = "French",
            decks = emptyList(),
        )
        val deck1 = Deck("deck-1", "Basics", listOf(Card("c1", "Hi", "Salut")))
        val deck2 = Deck("deck-2", "Advanced", listOf(Card("c2", "Bye", "Au revoir")))

        repository.addDeck(deck1, combo)
        repository.addDeck(deck2, combo)

        val combos = repository.getLanguageCombinations()
        assertEquals(1, combos.size)
        assertEquals(2, combos[0].decks.size)
        assertEquals(setOf("deck-1", "deck-2"), combos[0].decks.map { it.id }.toSet())
    }

    @Test
    fun addDeckFromJson_parsesAndAddsDeck() {
        val json = """
            {
              "languageCombo": {
                "id": "en-es",
                "name": "English – Spanish",
                "sideAName": "English",
                "sideBName": "Spanish"
              },
              "deck": {
                "id": "spanish-1",
                "name": "Spanish Basics"
              },
              "cards": [
                {"id": "es-1", "lines": [{"sideA": "Hello", "sideB": ["Hola"]}]},
                {"id": "es-2", "lines": [{"sideA": "Thank you", "sideB": ["Gracias"]}]}
              ]
            }
        """.trimIndent()

        val result = repository.addDeckFromJson(json)

        assertTrue(result.isSuccess)
        val combos = repository.getLanguageCombinations()
        assertEquals(1, combos.size)
        assertEquals("en-es", combos[0].id)
        assertEquals(1, combos[0].decks.size)
        assertEquals("spanish-1", combos[0].decks[0].id)
        assertEquals(2, combos[0].decks[0].cards.size)
    }

    @Test
    fun addDeckFromJson_bundledDeckFormat_addsNewDeck() {
        // Uses the exact JSON structure of bundled decks (e.g. en-fr-french-basics.deck.json)
        // to verify the "Add a deck from APK" flow parses and inserts correctly.
        val json = """
            {
              "languageCombo": {
                "id": "en-fr",
                "name": "English – French",
                "sideAName": "English",
                "sideBName": "French"
              },
              "deck": {
                "id": "french-1",
                "name": "French — Basics"
              },
              "cards": [
                {"id": "1", "lines": [{"sideA": "Hello", "sideB": ["Bonjour", "Salut"]}]},
                {"id": "2", "lines": [{"sideA": "Thank you", "sideB": ["Merci"]}]}
              ]
            }
        """.trimIndent()

        val result = repository.addDeckFromJson(json)

        assertTrue(result.isSuccess)
        val combos = repository.getLanguageCombinations()
        assertEquals(1, combos.size)
        assertEquals("en-fr", combos[0].id)
        assertEquals(1, combos[0].decks.size)
        assertEquals("french-1", combos[0].decks[0].id)
        assertEquals("French — Basics", combos[0].decks[0].name)
        assertEquals(2, combos[0].decks[0].cards.size)
        assertEquals("Hello", combos[0].decks[0].cards[0].lines[0].sideA)
        assertEquals(listOf("Bonjour", "Salut"), combos[0].decks[0].cards[0].lines[0].sideB)
    }

    @Test
    fun addDeckFromJson_invalidJson_returnsFailure() {
        val result = repository.addDeckFromJson("not valid json {")

        assertTrue(result.isFailure)
        assertEquals(0, repository.getLanguageCombinations().size)
    }

    @Test
    fun deleteDeck_removesDeckAndCards() {
        val combo = LanguageCombination(
            id = "en-fr",
            name = "English – French",
            sideAName = "English",
            sideBName = "French",
            decks = emptyList(),
        )
        val deck = Deck(
            id = "to-delete",
            name = "Temp Deck",
            cards = listOf(Card("c1", "A", "B")),
        )
        repository.addDeck(deck, combo)

        val deleted = repository.deleteDeck("to-delete")

        assertTrue(deleted)
        assertNull(repository.getDeck("to-delete"))
        assertEquals(0, repository.getLanguageCombinations().size)
    }

    @Test
    fun deleteDeck_whenOtherDecksExist_removesOnlyDeckAndKeepsCombo() {
        val combo = LanguageCombination(
            id = "en-fr",
            name = "English – French",
            sideAName = "English",
            sideBName = "French",
            decks = emptyList(),
        )
        val deck1 = Deck("deck-1", "Basics", listOf(Card("c1", "Hi", "Salut")))
        val deck2 = Deck("deck-2", "Advanced", listOf(Card("c2", "Bye", "Au revoir")))
        repository.addDeck(deck1, combo)
        repository.addDeck(deck2, combo)

        val deleted = repository.deleteDeck("deck-1")

        assertTrue(deleted)
        assertNull(repository.getDeck("deck-1"))
        assertNotNull(repository.getDeck("deck-2"))
        val combos = repository.getLanguageCombinations()
        assertEquals(1, combos.size)
        assertEquals(1, combos[0].decks.size)
        assertEquals("deck-2", combos[0].decks[0].id)
    }

    @Test
    fun deleteDeck_nonexistentDeck_returnsFalse() {
        val deleted = repository.deleteDeck("nonexistent")

        assertFalse(deleted)
    }

    @Test
    fun deleteDeck_multiLineCard_removesAllCardLines() {
        val combo = LanguageCombination(
            id = "en-fr",
            name = "English – French",
            sideAName = "English",
            sideBName = "French",
            decks = emptyList(),
        )
        val multiLineCard = Card(
            id = "fc-d1",
            lines = listOf(
                CardLine("Hi, Paul. Is everything going well?", listOf("Salut, Paul, Ça va bien?")),
                CardLine("Very well / Great, thanks", listOf("Très bien, merci")),
            ),
        )
        val deck = Deck("deck-1", "Conversation", listOf(multiLineCard))
        repository.addDeck(deck, combo)

        repository.deleteDeck("deck-1")

        assertEquals(0, repository.getLanguageCombinations().size)
    }
}
