package net.thetrues.languagecards.model

/**
 * Per-session practice options (game mode, prompt presentation, answer matching).
 * [textAnswerMode] applies when [gameMode] is [GameMode.TEXT_ANSWER] or [GameMode.AUDIO_ANSWER].
 */
data class SessionOptions(
    val gameMode: GameMode,
    val promptDisplay: PromptDisplay,
    val textAnswerMode: TextAnswerMode,
) {
    companion object {
        val Default = SessionOptions(
            gameMode = GameMode.GUESS,
            promptDisplay = PromptDisplay.TEXT_AND_AUDIO,
            textAnswerMode = TextAnswerMode.STRICT,
        )
    }
}
