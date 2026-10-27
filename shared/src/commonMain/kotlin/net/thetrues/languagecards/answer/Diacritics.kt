package net.thetrues.languagecards.answer

/**
 * Removes combining marks (accents) for non-strict answer comparison.
 * Platform-specific (NFD + strip marks on Android; equivalent on iOS).
 */
internal expect fun stripDiacriticsForComparison(input: String): String
