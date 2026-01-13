package ru.macdroid.subagentstest.core.di

import org.koin.dsl.module
import ru.macdroid.subagentstest.core.database.DriverFactory
import ru.macdroid.subagentstest.database.NotesDatabase

val databaseModule = module {
    single {
        val driver = get<DriverFactory>().createDriver()
        NotesDatabase(driver)
    }
}
