package net.thetrues.languagecards.answer

/**
 * iOS: Kotlin/Native does not expose NSString folding/transform used on Android's Normalizer.
 * This fold covers common European letters used in bundled decks (French, Spanish, German).
 * Input is expected to already be lowercased ([AnswerMatcher] does that first).
 */
internal actual fun stripDiacriticsForComparison(input: String): String = buildString(input.length + 4) {
    for (ch in input) {
        when (ch) {
            'ß' -> append("ss")
            'œ' -> append("oe")
            'æ' -> append("ae")
            else -> append(LATIN_ACCENT_FOLD[ch] ?: ch)
        }
    }
}

private val LATIN_ACCENT_FOLD: Map<Char, Char> = mapOf(
    'à' to 'a', 'á' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a', 'å' to 'a',
    'ā' to 'a', 'ă' to 'a', 'ą' to 'a', 'ǎ' to 'a', 'ȧ' to 'a',
    'è' to 'e', 'é' to 'e', 'ê' to 'e', 'ë' to 'e', 'ē' to 'e', 'ė' to 'e', 'ę' to 'e', 'ě' to 'e',
    'ì' to 'i', 'í' to 'i', 'î' to 'i', 'ï' to 'i', 'ī' to 'i', 'į' to 'i', 'ı' to 'i',
    'ò' to 'o', 'ó' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o', 'ø' to 'o',
    'ō' to 'o', 'ő' to 'o', 'ơ' to 'o',
    'ù' to 'u', 'ú' to 'u', 'û' to 'u', 'ü' to 'u', 'ū' to 'u', 'ů' to 'u', 'ű' to 'u', 'ư' to 'u',
    'ý' to 'y', 'ÿ' to 'y', 'ỳ' to 'y',
    'ñ' to 'n', 'ń' to 'n', 'ň' to 'n',
    'ç' to 'c', 'ć' to 'c', 'č' to 'c',
    'ď' to 'd', 'đ' to 'd',
    'ř' to 'r', 'ŕ' to 'r',
    'š' to 's', 'ś' to 's',
    'ť' to 't',
    'ž' to 'z', 'ź' to 'z', 'ż' to 'z',
    'ł' to 'l',
    'ğ' to 'g',
)
