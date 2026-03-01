package net.thetrues.languagecards.model

/**
 * A single vocabulary card: English (sideA) and French (sideB).
 * sideB supports one or two correct answers (e.g. "Bonjour" or "Salut" for "Hello").
 */
data class Card(
    val id: String,
    val sideA: String,  // English
    val sideB: List<String>,  // French - one or two correct answers
) {
    /** Convenience constructor for a single answer on sideB. */
    constructor(id: String, sideA: String, sideB: String) : this(id, sideA, listOf(sideB))
}
