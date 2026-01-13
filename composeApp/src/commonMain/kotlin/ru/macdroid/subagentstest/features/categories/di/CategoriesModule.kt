package ru.macdroid.subagentstest.features.categories.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.macdroid.subagentstest.features.categories.data.local.CategoryLocalDataSource
import ru.macdroid.subagentstest.features.categories.data.repository.CategoryRepositoryImpl
import ru.macdroid.subagentstest.features.categories.domain.repository.CategoryRepository
import ru.macdroid.subagentstest.features.categories.domain.usecase.CreateCategoryUseCase
import ru.macdroid.subagentstest.features.categories.domain.usecase.DeleteCategoryUseCase
import ru.macdroid.subagentstest.features.categories.domain.usecase.GetCategoriesUseCase
import ru.macdroid.subagentstest.features.categories.domain.usecase.UpdateCategoryUseCase
import ru.macdroid.subagentstest.features.categories.presentation.viewmodel.CategoriesViewModel

/**
 * Koin module for Categories feature
 * Registers all dependencies following Clean Architecture layers:
 * - Data: LocalDataSource, Repository
 * - Domain: Use Cases
 * - Presentation: ViewModel
 */
val categoriesModule = module {
    // Data Layer
    singleOf(::CategoryLocalDataSource)
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class

    // Domain Layer - Use Cases (factory for stateless classes)
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::CreateCategoryUseCase)
    factoryOf(::UpdateCategoryUseCase)
    factoryOf(::DeleteCategoryUseCase)

    // Presentation Layer - ViewModel
    viewModelOf(::CategoriesViewModel)
}
