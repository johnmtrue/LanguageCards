package net.thetrues.languagecards.model

/**
 * Direction of practice: which side of the card is shown as prompt and which as answer.
 * - A_TO_B: prompt = sideA (e.g. English), answer = sideB (e.g. French)
 * - B_TO_A: prompt = sideB (e.g. French), answer = sideA (e.g. English)
 */
enum class PracticeDirection {
    A_TO_B,
    B_TO_A,
}
