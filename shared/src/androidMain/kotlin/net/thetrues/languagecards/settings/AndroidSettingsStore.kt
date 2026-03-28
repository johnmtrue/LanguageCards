package net.thetrues.languagecards.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import net.thetrues.languagecards.model.GameMode
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.PromptDisplay
import net.thetrues.languagecards.model.TextAnswerMode
import net.thetrues.languagecards.session.SessionFlow

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "language_cards_app_settings",
)

private object PrefsKeys {
    val GAME_MODE = stringPreferencesKey("game_mode")
    val PROMPT_DISPLAY = stringPreferencesKey("prompt_display")
    val TEXT_ANSWER_MODE = stringPreferencesKey("text_answer_mode")
    val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    val DEFAULT_SESSION_SIZE = intPreferencesKey("default_session_size")
    val DEFAULT_DIRECTION = stringPreferencesKey("default_direction")
}

class AndroidSettingsStore(
    private val context: Context,
) : SettingsStore {

    override suspend fun read(): AppSettings {
        val prefs = context.appSettingsDataStore.data.first()
        return AppSettings(
            gameMode = prefs[PrefsKeys.GAME_MODE].parseEnum(GameMode.GUESS),
            promptDisplay = prefs[PrefsKeys.PROMPT_DISPLAY].parseEnum(PromptDisplay.TEXT_AND_AUDIO),
            textAnswerMode = prefs[PrefsKeys.TEXT_ANSWER_MODE].parseEnum(TextAnswerMode.STRICT),
            ttsEnabled = prefs[PrefsKeys.TTS_ENABLED] ?: true,
            defaultSessionSize = prefs[PrefsKeys.DEFAULT_SESSION_SIZE]
                ?: SessionFlow.DEFAULT_SESSION_SIZE,
            defaultDirection = prefs[PrefsKeys.DEFAULT_DIRECTION].parseEnum(PracticeDirection.A_TO_B),
        )
    }

    override suspend fun write(settings: AppSettings) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[PrefsKeys.GAME_MODE] = settings.gameMode.name
            prefs[PrefsKeys.PROMPT_DISPLAY] = settings.promptDisplay.name
            prefs[PrefsKeys.TEXT_ANSWER_MODE] = settings.textAnswerMode.name
            prefs[PrefsKeys.TTS_ENABLED] = settings.ttsEnabled
            prefs[PrefsKeys.DEFAULT_SESSION_SIZE] = settings.defaultSessionSize
            prefs[PrefsKeys.DEFAULT_DIRECTION] = settings.defaultDirection.name
        }
    }
}

private inline fun <reified T : Enum<T>> String?.parseEnum(default: T): T {
    if (this == null) return default
    return enumValues<T>().find { it.name == this } ?: default
}

actual fun createSettingsStore(platformContext: Any?): SettingsStore {
    val ctx = platformContext as Context
    return AndroidSettingsStore(ctx.applicationContext)
}
