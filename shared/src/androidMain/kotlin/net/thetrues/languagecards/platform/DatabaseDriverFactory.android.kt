package net.thetrues.languagecards.platform

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.thetrues.languagecards.db.LanguageCardsDatabase

actual fun createSqlDriver(platformContext: Any?): app.cash.sqldelight.db.SqlDriver {
    val context = platformContext as Context
    return AndroidSqliteDriver(
        schema = LanguageCardsDatabase.Schema,
        context = context,
        name = "languagecards.db",
    )
}
