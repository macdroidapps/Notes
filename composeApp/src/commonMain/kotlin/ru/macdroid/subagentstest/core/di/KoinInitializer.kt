package ru.macdroid.subagentstest.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import ru.macdroid.subagentstest.features.categories.di.categoriesModule
import ru.macdroid.subagentstest.features.notes.di.notesModule
import ru.macdroid.subagentstest.features.support.di.supportModule

fun initKoin(platformModule: Module, appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            platformModule,
            coreModule,
            databaseModule,
            categoriesModule,
            notesModule,
            supportModule
        )
    }
}
