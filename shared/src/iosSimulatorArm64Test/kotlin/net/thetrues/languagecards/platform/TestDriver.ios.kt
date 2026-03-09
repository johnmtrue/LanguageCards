package net.thetrues.languagecards.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.thetrues.languagecards.db.LanguageCardsDatabase

actual fun createTestDriver(): SqlDriver {
    return NativeSqliteDriver(
        schema = LanguageCardsDatabase.Schema,
        name = ":memory:",
    )
}
