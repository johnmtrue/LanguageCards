package net.thetrues.languagecards.answer

import java.text.Normalizer

internal actual fun stripDiacriticsForComparison(input: String): String {
    val nfd = Normalizer.normalize(input, Normalizer.Form.NFD)
    return nfd.replace(Regex("\\p{M}+"), "")
}
