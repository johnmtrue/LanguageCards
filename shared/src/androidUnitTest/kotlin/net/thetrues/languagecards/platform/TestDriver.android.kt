package net.thetrues.languagecards.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
actual fun createTestDriver(): SqlDriver {
    Class.forName("org.sqlite.JDBC")
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
}
