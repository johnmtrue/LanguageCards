package net.thetrues.languagecards.platform

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.thetrues.languagecards.db.LanguageCardsDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Returns the path to the database file in the iOS Documents directory.
 */
fun getDatabasePath(): String {
    val documentDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return "$documentDir/languagecards.db"
}

actual fun createSqlDriver(platformContext: Any?): app.cash.sqldelight.db.SqlDriver {
    val path = (platformContext as? String) ?: getDatabasePath()
    return NativeSqliteDriver(
        schema = LanguageCardsDatabase.Schema,
        name = path,
    )
}
