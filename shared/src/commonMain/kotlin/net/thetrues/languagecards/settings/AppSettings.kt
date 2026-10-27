package net.thetrues.languagecards.settings

import net.thetrues.languagecards.model.GameMode
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.PromptDisplay
import net.thetrues.languagecards.model.SessionOptions
import net.thetrues.languagecards.model.TextAnswerMode
import net.thetrues.languagecards.session.SessionFlow

/**
 * Persisted defaults for practice and audio. Mirrors [SessionOptions] for the core mode fields.
 */
data class AppSettings(
    val gameMode: GameMode = GameMode.GUESS,
    val promptDisplay: PromptDisplay = PromptDisplay.TEXT_AND_AUDIO,
    val textAnswerMode: TextAnswerMode = TextAnswerMode.STRICT,
    val ttsEnabled: Boolean = true,
    val defaultSessionSize: Int = SessionFlow.DEFAULT_SESSION_SIZE,
    val defaultDirection: PracticeDirection = PracticeDirection.A_TO_B,
) {
    companion object {
        val Default = AppSettings()
    }

    fun toSessionOptions(): SessionOptions = SessionOptions(
        gameMode = gameMode,
        promptDisplay = promptDisplay,
        textAnswerMode = textAnswerMode,
    )
}
