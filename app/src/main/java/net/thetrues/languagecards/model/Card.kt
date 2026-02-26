package net.thetrues.languagecards.model

/**
 * A single vocabulary card: English (sideA) and French (sideB).
 */
data class Card(
    val id: String,
    val sideA: String,  // English
    val sideB: String,  // French
)
