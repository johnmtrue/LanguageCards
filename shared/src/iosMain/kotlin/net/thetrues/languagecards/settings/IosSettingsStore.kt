package net.thetrues.languagecards.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.thetrues.languagecards.model.GameMode
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.PromptDisplay
import net.thetrues.languagecards.model.TextAnswerMode
import net.thetrues.languagecards.session.SessionFlow
import platform.Foundation.NSUserDefaults

private object Keys {
    const val GAME_MODE = "game_mode"
    const val PROMPT_DISPLAY = "prompt_display"
    const val TEXT_ANSWER_MODE = "text_answer_mode"
    const val TTS_ENABLED = "tts_enabled"
    const val DEFAULT_SESSION_SIZE = "default_session_size"
    const val DEFAULT_DIRECTION = "default_direction"
}

class IosSettingsStore : SettingsStore {

    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun read(): AppSettings = withContext(Dispatchers.Default) {
        AppSettings(
            gameMode = defaults.stringForKey(Keys.GAME_MODE).parseEnum(GameMode.GUESS),
            promptDisplay = defaults.stringForKey(Keys.PROMPT_DISPLAY).parseEnum(PromptDisplay.TEXT_AND_AUDIO),
            textAnswerMode = defaults.stringForKey(Keys.TEXT_ANSWER_MODE).parseEnum(TextAnswerMode.STRICT),
            ttsEnabled = if (defaults.objectForKey(Keys.TTS_ENABLED) != null) {
                defaults.boolForKey(Keys.TTS_ENABLED)
            } else {
                true
            },
            defaultSessionSize = if (defaults.objectForKey(Keys.DEFAULT_SESSION_SIZE) != null) {
                defaults.integerForKey(Keys.DEFAULT_SESSION_SIZE).toInt()
            } else {
                SessionFlow.DEFAULT_SESSION_SIZE
            },
            defaultDirection = defaults.stringForKey(Keys.DEFAULT_DIRECTION).parseEnum(PracticeDirection.A_TO_B),
        )
    }

    override suspend fun write(settings: AppSettings) = withContext(Dispatchers.Default) {
        defaults.setObject(settings.gameMode.name, Keys.GAME_MODE)
        defaults.setObject(settings.promptDisplay.name, Keys.PROMPT_DISPLAY)
        defaults.setObject(settings.textAnswerMode.name, Keys.TEXT_ANSWER_MODE)
        defaults.setBool(settings.ttsEnabled, Keys.TTS_ENABLED)
        defaults.setInteger(settings.defaultSessionSize.toLong(), Keys.DEFAULT_SESSION_SIZE)
        defaults.setObject(settings.defaultDirection.name, Keys.DEFAULT_DIRECTION)
    }
}

private inline fun <reified T : Enum<T>> String?.parseEnum(default: T): T {
    if (this == null) return default
    return enumValues<T>().find { it.name == this } ?: default
}

actual fun createSettingsStore(platformContext: Any?): SettingsStore = IosSettingsStore()
