package net.thetrues.languagecards.session

import kotlin.random.Random
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.PracticeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardSelectorTest {

    private fun card(id: String, sideA: String = "a", sideB: String = "b") =
        Card(id, sideA, sideB)

    private fun deck(vararg cards: Card) = Deck("deck1", "Test Deck", cards.toList())

    private fun stats(cardId: String, hits: Int = 0, misses: Int = 0) =
        CardStats(cardId, PracticeDirection.A_TO_B, hits, misses)

    @Test
    fun emptyDeck_returnsEmptyList() {
        val deck = deck()
        val result = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = emptyMap(),
        )
        assertEquals(emptyList<Card>(), result)
    }

    @Test
    fun deckSmallerThanSessionSize_returnsAllCardsShuffled() {
        val cards = listOf(card("1"), card("2"), card("3"))
        val deck = deck(*cards.toTypedArray())
        val random = Random(42)
        val result = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = emptyMap(),
            random = random,
        )
        assertEquals(3, result.size)
        assertEquals(cards.toSet(), result.toSet())
    }

    @Test
    fun deckSizeOne_returnsSingleCard() {
        val c = card("1")
        val deck = deck(c)
        val result = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = emptyMap(),
        )
        assertEquals(listOf(c), result)
    }

    @Test
    fun deckLargerThanSessionSize_returnsExactlySessionSizeCards() {
        val cards = (1..20).map { card(it.toString()) }
        val deck = deck(*cards.toTypedArray())
        val result = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = emptyMap(),
            random = Random(123),
        )
        assertEquals(10, result.size)
        assertTrue(result.all { it in cards })
        assertEquals(result.toSet().size, result.size)
    }

    @Test
    fun weightedSelection_favorsHighMissCards_withFixedRandom() {
        val c1 = card("1")
        val c2 = card("2")
        val c3 = card("3")
        val deck = deck(c1, c2, c3, card("4"), card("5"), card("6"), card("7"), card("8"), card("9"), card("10"))
        val statsByCard = mapOf(
            "1" to stats("1", hits = 10, misses = 0),
            "2" to stats("2", hits = 5, misses = 4),
            "3" to stats("3", hits = 0, misses = 9),
        )
        val random = Random(999)
        val result = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = statsByCard,
            random = random,
        )
        assertEquals(10, result.size)
        val result2 = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = statsByCard,
            random = Random(999),
        )
        assertEquals(result.map { it.id }, result2.map { it.id })
    }

    @Test
    fun allCardsUnplayed_equalWeights_selectionIsDeterministicWithFixedSeed() {
        val cards = (1..15).map { card(it.toString()) }
        val deck = deck(*cards.toTypedArray())
        val random = Random(7)
        val result1 = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = emptyMap(),
            random = random,
        )
        val random2 = Random(7)
        val result2 = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = emptyMap(),
            random = random2,
        )
        assertEquals(result1.map { it.id }, result2.map { it.id })
    }

    @Test
    fun resultContainsOnlyDeckCards() {
        val cards = (1..25).map { card(it.toString()) }
        val deck = deck(*cards.toTypedArray())
        val statsByCard = mapOf(
            "5" to stats("5", misses = 10),
            "12" to stats("12", misses = 8),
        )
        val result = CardSelector.selectCards(
            deck = deck,
            direction = PracticeDirection.A_TO_B,
            sessionSize = 10,
            statsByCard = statsByCard,
            random = Random(111),
        )
        assertEquals(10, result.size)
        assertTrue(result.all { it in cards })
    }

    @Test
    fun noDuplicateCardsInResult() {
        val cards = (1..20).map { card(it.toString()) }
        val deck = deck(*cards.toTypedArray())
        for (seed in 0..4) {
            val result = CardSelector.selectCards(
                deck = deck,
                direction = PracticeDirection.A_TO_B,
                sessionSize = 10,
                statsByCard = emptyMap(),
                random = Random(seed.toLong()),
            )
            assertEquals("No duplicates expected for seed $seed", result.toSet().size, result.size)
        }
    }
}
