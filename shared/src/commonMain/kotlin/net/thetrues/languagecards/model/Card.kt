package net.thetrues.languagecards.model

/**
 * A single vocabulary card with two sides. Semantics come from the language combination
 * (e.g. sideA=English, sideB=French). sideB supports one or more correct answers.
 */
data class Card(
    val id: String,
    val sideA: String,
    val sideB: List<String>,
) {
    /** Convenience constructor for a single answer on sideB. */
    constructor(id: String, sideA: String, sideB: String) : this(id, sideA, listOf(sideB))
}
