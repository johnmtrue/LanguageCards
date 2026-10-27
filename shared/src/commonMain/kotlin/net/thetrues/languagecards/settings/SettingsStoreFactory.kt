package net.thetrues.languagecards.settings

/**
 * Creates a platform [SettingsStore]. Pass Android [android.content.Context];
 * on iOS pass null.
 */
expect fun createSettingsStore(platformContext: Any?): SettingsStore
