package net.thetrues.languagecards.model

/**
 * Hit/miss counts for a single card (in-memory for prototype).
 */
data class CardStats(
    val cardId: String,
    var hits: Int = 0,
    var misses: Int = 0,
)
