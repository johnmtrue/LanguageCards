package net.thetrues.languagecards.answer

import net.thetrues.languagecards.model.TextAnswerMode

/**
 * Compares user input to one or more acceptable answers after normalization.
 * - [TextAnswerMode.STRICT]: trim, lowercase; diacritics must match.
 * - [TextAnswerMode.NON_STRICT]: same, then strip combining marks on both sides.
 */
object AnswerMatcher {

    fun matches(userInput: String, correctAnswers: List<String>, mode: TextAnswerMode): Boolean {
        if (correctAnswers.isEmpty()) return false
        val normalizedUser = normalize(userInput, mode)
        return correctAnswers.any { normalize(it, mode) == normalizedUser }
    }

    internal fun normalize(text: String, mode: TextAnswerMode): String {
        val trimmedLower = text.trim().lowercase()
        return when (mode) {
            TextAnswerMode.STRICT -> trimmedLower
            TextAnswerMode.NON_STRICT -> stripDiacriticsForComparison(trimmedLower)
        }
    }
}
