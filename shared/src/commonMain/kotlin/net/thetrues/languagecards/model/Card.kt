package net.thetrues.languagecards.model

/**
 * A vocabulary card with one or more lines. Semantics come from the language combination
 * (e.g. sideA=English, sideB=French). For multi-line cards (e.g. dialogues), the last line
 * is quizzed; earlier lines are context.
 */
data class Card(
    val id: String,
    val lines: List<CardLine>,
) {
    init {
        require(id.isNotBlank()) { "Card id cannot be blank" }
        require(lines.isNotEmpty()) { "Card must have at least one line" }
    }

    /** Convenience constructor for a single-line card with one answer on sideB. */
    constructor(id: String, sideA: String, sideB: String) : this(id, listOf(CardLine(sideA, sideB)))

    /** Convenience constructor for a single-line card with multiple answers on sideB. */
    constructor(id: String, sideA: String, sideB: List<String>) : this(id, listOf(CardLine(sideA, sideB)))

    /** The quizzed line (last line). For single-line cards, this is the only line. */
    val quizLine: CardLine get() = lines.last()

    /** Last line's sideA. Use quizLine for multi-line cards. */
    val sideA: String get() = quizLine.sideA

    /** Last line's sideB. Use quizLine for multi-line cards. */
    val sideB: List<String> get() = quizLine.sideB
}
