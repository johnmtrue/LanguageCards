package net.thetrues.languagecards.model

/**
 * How the user answers during a session.
 * - GUESS: self-report ("I know" / "Show answer" then correct/incorrect)
 * - TEXT_ANSWER: typed answer compared to the expected translation
 * - AUDIO_ANSWER: spoken answer (speech-to-text) compared to the expected translation
 */
enum class GameMode {
    GUESS,
    TEXT_ANSWER,
    AUDIO_ANSWER,
}
