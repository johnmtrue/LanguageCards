package net.thetrues.languagecards.data

import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination
import net.thetrues.languagecards.platform.IgnoreIosSimulator
import net.thetrues.languagecards.platform.createTestDriver
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Builds a single-line card using primary constructors only.
 * Avoids Kotlin/Native issues with Card(id, sideA, sideB) secondary constructor.
 */
private fun card(id: String, sideA: String, sideB: String): Card =
    Card(id = id, lines = listOf(CardLine(sideA = sideA, sideB = listOf(sideB))))

/**
 * Tests add/delete deck flows for the shared DeckRepository.
 * Runs on Android/JVM (androidUnitTest). Skipped on iOS simulator due to Kotlin/Native
 * IllegalArgumentException until root cause is fixed.
 */
@IgnoreIosSimulator
class DeckRepositoryTest {

    private lateinit var database: LanguageCardsDatabase
    private lateinit var repository: SqlDelightDeckRepository

    @BeforeTest
    fun setUp() {
        val driver = createTestDriver()
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
                card("c1", "Hello", "Bonjour"),
                card("c2", "Thank you", "Merci"),
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
            cards = listOf(card("c1", "A", "B")),
        )
        repository.addDeck(deck, combo)

        val deleted = repository.deleteDeck("to-delete")

        assertTrue(deleted)
        assertNull(repository.getDeck("to-delete"))
        assertEquals(0, repository.getLanguageCombinations().size)
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
    fun deleteDeck_whenOtherDecksExist_removesOnlyDeckAndKeepsCombo() {
        val combo = LanguageCombination(
            id = "en-fr",
            name = "English – French",
            sideAName = "English",
            sideBName = "French",
            decks = emptyList(),
        )
        val deck1 = Deck("deck-1", "Basics", listOf(card("c1", "Hi", "Salut")))
        val deck2 = Deck("deck-2", "Advanced", listOf(card("c2", "Bye", "Au revoir")))
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
    fun addDeleteRestore_deckFlow() {
        val frenchBasicsJson = """
            {
              "languageCombo": {"id": "en-fr", "name": "English – French", "sideAName": "English", "sideBName": "French"},
              "deck": {"id": "french-1", "name": "French — Basics"},
              "cards": [{"id": "1", "lines": [{"sideA": "Hello", "sideB": ["Bonjour"]}]}]
            }
        """.trimIndent()
        val spanishBasicsJson = """
            {
              "languageCombo": {"id": "en-es", "name": "English – Spanish", "sideAName": "English", "sideBName": "Spanish"},
              "deck": {"id": "spanish-1", "name": "Spanish — Basics"},
              "cards": [{"id": "es-1", "lines": [{"sideA": "Hello", "sideB": ["Hola"]}]}]
            }
        """.trimIndent()

        assertTrue(repository.addDeckFromJson(frenchBasicsJson).isSuccess)
        assertTrue(repository.addDeckFromJson(spanishBasicsJson).isSuccess)
        var combos = repository.getLanguageCombinations()
        assertEquals(2, combos.size)
        assertEquals(setOf("en-fr", "en-es"), combos.map { it.id }.toSet())

        combos.flatMap { it.decks }.forEach { repository.deleteDeck(it.id) }
        combos = repository.getLanguageCombinations()
        assertEquals(0, combos.size)

        assertTrue(repository.addDeckFromJson(frenchBasicsJson).isSuccess)
        assertTrue(repository.addDeckFromJson(spanishBasicsJson).isSuccess)
        combos = repository.getLanguageCombinations()
        assertEquals(2, combos.size)
        assertEquals("french-1", repository.getDeck("french-1")?.id)
        assertEquals("spanish-1", repository.getDeck("spanish-1")?.id)
    }
}
