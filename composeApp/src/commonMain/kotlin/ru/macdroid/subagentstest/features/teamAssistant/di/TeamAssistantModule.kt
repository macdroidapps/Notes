package ru.macdroid.subagentstest.features.teamAssistant.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.macdroid.subagentstest.features.teamAssistant.data.local.TeamAssistantLocalDataSource
import ru.macdroid.subagentstest.features.teamAssistant.data.repository.TeamAssistantRepositoryImpl
import ru.macdroid.subagentstest.features.teamAssistant.domain.ai.*
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository
import ru.macdroid.subagentstest.features.teamAssistant.domain.usecase.*
import ru.macdroid.subagentstest.features.teamAssistant.presentation.viewmodel.TeamAssistantViewModel

/**
 * Koin DI module for Team Assistant feature.
 * Registers all components: data layer, domain layer (use cases + AI), and presentation layer.
 */
val teamAssistantModule = module {
    // ========== Data Layer ==========
    // Singleton: Database access and repository
    singleOf(::TeamAssistantLocalDataSource)
    singleOf(::TeamAssistantRepositoryImpl) bind TeamAssistantRepository::class

    // ========== Domain Layer - AI Components ==========
    // Singleton: AI components (stateful RAG engine)
    singleOf(::IntentRecognizer)
    singleOf(::QueryParser)
    singleOf(::TeamAssistant)

    // ========== Domain Layer - Use Cases ==========
    // Factory: Use Cases (stateless, created on demand)
    factoryOf(::GetAllTasksUseCase)
    factoryOf(::GetTasksByStatusUseCase)
    factoryOf(::GetTasksByPriorityUseCase)
    factoryOf(::CreateTaskUseCase)
    factoryOf(::UpdateTaskUseCase)
    factoryOf(::DeleteTaskUseCase)
    factoryOf(::GetTaskStatisticsUseCase)
    factoryOf(::GetOverdueTasksUseCase)
    factoryOf(::GetAssistantResponseUseCase)
    factoryOf(::GetRecommendationsUseCase)

    // ========== Presentation Layer ==========
    // ViewModel: Lifecycle-aware
    viewModelOf(::TeamAssistantViewModel)
}
