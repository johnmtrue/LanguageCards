package net.thetrues.languagecards.session

import kotlin.random.Random
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.PracticeDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        assertEquals(emptyList(), result)
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
        assertEquals(result.toSet().size, result.size) // no duplicates
    }

    @Test
    fun weightedSelection_favorsHighMissCards_withFixedRandom() {
        // Card 1: 0 misses (weight 1), Card 2: 4 misses (weight 5), Card 3: 9 misses (weight 10)
        // With fixed seed, weighted half should pick card 3 most often, then 2, then 1
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
        // With weight 10, card 3 should appear in weighted half (first 5 picks)
        // Run multiple times with same seed to verify determinism
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
        repeat(5) { seed ->
            val result = CardSelector.selectCards(
                deck = deck,
                direction = PracticeDirection.A_TO_B,
                sessionSize = 10,
                statsByCard = emptyMap(),
                random = Random(seed.toLong()),
            )
            assertEquals(result.toSet().size, result.size, "No duplicates expected for seed $seed")
        }
    }

    @Test
    fun weightedSelection_favorsLeastPracticedAndWeakCards_overManyRuns() {
        // weak: 0 hits 9 misses (weight 10), underpracticed: 1 hit 1 miss (weight 6), strong: 10 hits 0 misses (weight 1)
        // 17 unplayed (weight 7 each). Deck size 20, sessionSize 10 → 5 weighted + 5 random.
        // Weak and underpracticed have higher weights, so they appear more often than strong over many runs.
        val weak = card("weak")
        val underpracticed = card("underpracticed")
        val strong = card("strong")
        val deck = deck(
            weak,
            underpracticed,
            strong,
            *((1..17).map { card("u$it") }).toTypedArray(),
        )
        val statsByCard = mapOf(
            "weak" to stats("weak", hits = 0, misses = 9),
            "underpracticed" to stats("underpracticed", hits = 1, misses = 1),
            "strong" to stats("strong", hits = 10, misses = 0),
        )
        var weakCount = 0
        var underpracticedCount = 0
        var strongCount = 0
        repeat(200) { seed ->
            val result = CardSelector.selectCards(
                deck = deck,
                direction = PracticeDirection.A_TO_B,
                sessionSize = 10,
                statsByCard = statsByCard,
                random = Random(seed.toLong()),
            )
            if (weak in result) weakCount++
            if (underpracticed in result) underpracticedCount++
            if (strong in result) strongCount++
        }
        assertTrue(weakCount > strongCount) {
            "Weak cards (high misses) should appear more often than strong (high hits): weak=$weakCount strong=$strongCount"
        }
        assertTrue(underpracticedCount > strongCount) {
            "Under-practiced cards should appear more often than strong: underpracticed=$underpracticedCount strong=$strongCount"
        }
    }
}
