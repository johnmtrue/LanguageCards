package net.thetrues.languagecards.model

/**
 * How typed or spoken answers are compared to expected text.
 * - STRICT: trim, case-insensitive; diacritics must match
 * - NON_STRICT: same as strict, plus accents/diacritics ignored (e.g. café = cafe)
 */
enum class TextAnswerMode {
    STRICT,
    NON_STRICT,
}
