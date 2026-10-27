package net.thetrues.languagecards.model

/**
 * How the quiz prompt (first line) is presented.
 * - TEXT_AND_AUDIO: prompt shown as text and spoken via TTS when available
 * - AUDIO_ONLY: prompt spoken only; text hidden (listening focus)
 */
enum class PromptDisplay {
    TEXT_AND_AUDIO,
    AUDIO_ONLY,
}
