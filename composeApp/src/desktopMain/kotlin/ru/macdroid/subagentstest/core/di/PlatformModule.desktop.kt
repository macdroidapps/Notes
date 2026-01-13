package ru.macdroid.subagentstest.core.di

import org.koin.dsl.module
import ru.macdroid.subagentstest.core.database.DriverFactory

val platformModule = module {
    single { DriverFactory() }
}
