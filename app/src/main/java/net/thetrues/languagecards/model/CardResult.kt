package net.thetrues.languagecards.model

/**
 * Result of one card in a practice session: hit (correct) or miss.
 */
data class CardResult(
    val cardId: String,
    val wasHit: Boolean,
)
