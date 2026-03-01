package net.thetrues.languagecards.model

/**
 * Hit/miss counts for a single card.
 */
data class CardStats(
    val cardId: String,
    var hits: Int = 0,
    var misses: Int = 0,
)
