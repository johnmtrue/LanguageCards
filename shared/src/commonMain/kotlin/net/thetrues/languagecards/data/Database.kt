package net.thetrues.languagecards.data

import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.platform.createSqlDriver

/**
 * Creates the SQLite database instance using the platform driver.
 * Call once at app startup; the returned instance can be held as a singleton.
 * @param platformContext Android: Context; iOS: path String or null for default
 */
fun createDatabase(platformContext: Any?): LanguageCardsDatabase {
    val driver = createSqlDriver(platformContext)
    val database = LanguageCardsDatabase(driver)
    seedDatabaseIfEmpty(database)
    return database
}
