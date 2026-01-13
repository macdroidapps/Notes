package ru.macdroid.subagentstest.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ru.macdroid.subagentstest.database.NotesDatabase
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".notes/notes.db")
        databasePath.parentFile?.mkdirs()

        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        NotesDatabase.Schema.create(driver)
        return driver
    }
}
