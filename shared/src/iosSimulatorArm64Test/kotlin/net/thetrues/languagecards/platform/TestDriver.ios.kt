package net.thetrues.languagecards.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.thetrues.languagecards.db.LanguageCardsDatabase
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSProcessInfo

/**
 * Creates a new, isolated SQLite driver for each test.
 * Uses a unique file path per invocation to avoid "table already exists" when tests
 * run in parallel or when the native in-memory driver would share state.
 */
actual fun createTestDriver(): SqlDriver {
    val uniqueId = NSProcessInfo.processInfo.globallyUniqueString()
    val path = "${NSTemporaryDirectory()}test_$uniqueId.db"
    return NativeSqliteDriver(
        schema = LanguageCardsDatabase.Schema,
        name = path,
    )
}
