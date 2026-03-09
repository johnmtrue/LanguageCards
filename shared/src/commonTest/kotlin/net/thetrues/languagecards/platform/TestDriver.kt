package net.thetrues.languagecards.platform

import app.cash.sqldelight.db.SqlDriver

/**
 * Creates an in-memory SQLite driver for tests.
 * Actual implementations: JdbcSqliteDriver (JVM/Android), NativeSqliteDriver (iOS).
 */
expect fun createTestDriver(): SqlDriver
