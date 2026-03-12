package net.thetrues.languagecards.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.thetrues.languagecards.db.LanguageCardsDatabase

actual fun createTestDriver(): SqlDriver {
    Class.forName("org.sqlite.JDBC")
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    LanguageCardsDatabase.Schema.create(driver)
    return driver
}
