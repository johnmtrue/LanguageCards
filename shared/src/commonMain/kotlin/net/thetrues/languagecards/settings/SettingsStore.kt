package net.thetrues.languagecards.settings

/**
 * Persists [AppSettings] (defaults for new sessions and global toggles like TTS).
 */
interface SettingsStore {
    suspend fun read(): AppSettings
    suspend fun write(settings: AppSettings)
}
