package net.thetrues.languagecards.model

/**
 * A single line of a card with two sides. For multi-line cards (e.g. dialogues),
 * each line has corresponding text in language A and language B.
 * [sideB] supports one or more correct answers.
 */
data class CardLine(
    val sideA: String,
    val sideB: List<String>,
) {
    /** Convenience constructor for a single answer on sideB. */
    constructor(sideA: String, sideB: String) : this(sideA, listOf(sideB))
}
