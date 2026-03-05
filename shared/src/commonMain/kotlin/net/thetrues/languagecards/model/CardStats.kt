package net.thetrues.languagecards.model

/**
 * Hit/miss counts for a single card in a specific practice direction.
 */
data class CardStats(
    val cardId: String,
    val direction: PracticeDirection,
    var hits: Int = 0,
    var misses: Int = 0,
)
