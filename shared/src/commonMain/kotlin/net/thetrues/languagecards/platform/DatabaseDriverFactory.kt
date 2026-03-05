package net.thetrues.languagecards.platform

import app.cash.sqldelight.db.SqlDriver

/**
 * Creates the platform-specific SQLite driver.
 * - Android: pass [android.content.Context]
 * - iOS: pass database path [String] from NSDocumentDirectory, or null to use default
 */
expect fun createSqlDriver(platformContext: Any?): SqlDriver
