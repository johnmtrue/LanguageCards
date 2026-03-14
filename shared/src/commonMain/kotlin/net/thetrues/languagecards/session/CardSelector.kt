package net.thetrues.languagecards.session

import kotlin.random.Random
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.PracticeDirection

/**
 * Selects cards for a practice session using a hybrid approach:
 * - Half: weighted random toward weak cards (high misses, low success rate) and least-practiced cards
 * - Half: random from the remaining cards
 *
 * Uses per-direction stats (pass [statsByCard] from [StatsRepository.getStatsForCards] for the chosen direction).
 * Weight = 1 + misses + underPracticedBonus, where underPracticedBonus favors cards with few attempts.
 */
object CardSelector {

    /** Cards with fewer than this many attempts get an extra weight bonus (prioritize least-practiced). */
    private const val MIN_ATTEMPTS_FOR_BONUS = 6

    /**
     * Selects [sessionSize] cards from the deck.
     * When deck has <= [sessionSize] cards, returns all cards shuffled.
     * Otherwise: half weighted by weakness (misses) and least-practiced (few attempts), half random from remainder, then shuffled.
     */
    fun selectCards(
        deck: Deck,
        direction: PracticeDirection,
        sessionSize: Int,
        statsByCard: Map<String, CardStats>,
        random: Random = Random.Default,
    ): List<Card> {
        val cards = deck.cards
        if (cards.isEmpty()) return emptyList()
        val takeCount = minOf(sessionSize, cards.size)
        if (cards.size <= sessionSize) {
            return cards.shuffled(random).take(takeCount)
        }

        val weightedCount = sessionSize / 2
        val randomCount = sessionSize - weightedCount

        val weights = cards.map { card ->
            val s = statsByCard[card.id]
            val hits = s?.hits ?: 0
            val misses = s?.misses ?: 0
            val attempts = hits + misses
            val underPracticedBonus = maxOf(0, MIN_ATTEMPTS_FOR_BONUS - attempts)
            1 + misses + underPracticedBonus
        }
        val weightedHalf = weightedSample(cards, weights, weightedCount, random)
        val remainder = cards.filter { it !in weightedHalf }
        val randomHalf = remainder.shuffled(random).take(randomCount)

        return (weightedHalf + randomHalf).shuffled(random)
    }

    /**
     * Weighted random sampling without replacement.
     * Higher weight = higher probability of selection.
     */
    private fun weightedSample(
        cards: List<Card>,
        weights: List<Int>,
        count: Int,
        random: Random,
    ): List<Card> {
        val pairs = cards.zip(weights).toMutableList()
        val result = mutableListOf<Card>()
        repeat(minOf(count, pairs.size)) {
            val total = pairs.sumOf { it.second }
            if (total <= 0) {
                val (card, _) = pairs.removeAt(0)
                result.add(card)
                return@repeat
            }
            var r = random.nextInt(total)
            for (i in pairs.indices) {
                r -= pairs[i].second
                if (r < 0) {
                    result.add(pairs[i].first)
                    pairs.removeAt(i)
                    break
                }
            }
        }
        return result
    }
}
