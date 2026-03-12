package net.thetrues.languagecards.model

/**
 * A language combination groups decks for a pair of languages (e.g. English–French).
 * [sideAName] and [sideBName] are used for practice direction labels (e.g. "English → French").
 */
data class LanguageCombination(
    val id: String,
    val name: String,
    val sideAName: String,
    val sideBName: String,
    val decks: List<Deck>,
) {
    // Validation of id/name/sideAName/sideBName is at repository boundary to avoid Kotlin/Native init-block issues.
}
