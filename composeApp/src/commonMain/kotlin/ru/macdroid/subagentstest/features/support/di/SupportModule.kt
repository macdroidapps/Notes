package ru.macdroid.subagentstest.features.support.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.macdroid.subagentstest.features.support.data.local.FAQLocalDataSource
import ru.macdroid.subagentstest.features.support.data.local.SupportTicketLocalDataSource
import ru.macdroid.subagentstest.features.support.data.local.TicketMessageLocalDataSource
import ru.macdroid.subagentstest.features.support.data.repository.FAQRepositoryImpl
import ru.macdroid.subagentstest.features.support.data.repository.SupportRepositoryImpl
import ru.macdroid.subagentstest.features.support.domain.ai.SupportAIAssistant
import ru.macdroid.subagentstest.features.support.domain.repository.FAQRepository
import ru.macdroid.subagentstest.features.support.domain.repository.SupportRepository
import ru.macdroid.subagentstest.features.support.domain.usecase.CreateTicketUseCase
import ru.macdroid.subagentstest.features.support.domain.usecase.GetAIAnswerUseCase
import ru.macdroid.subagentstest.features.support.domain.usecase.InitializeFAQUseCase
import ru.macdroid.subagentstest.features.support.presentation.viewmodel.SupportChatViewModel

/**
 * Koin module for Support feature
 * Registers all dependencies following Clean Architecture layers
 */
val supportModule = module {
    // Data Layer - Data Sources
    single { FAQLocalDataSource(faqQueries = get<ru.macdroid.subagentstest.database.NotesDatabase>().fAQQueries) }
    single { SupportTicketLocalDataSource(ticketQueries = get<ru.macdroid.subagentstest.database.NotesDatabase>().supportTicketQueries) }
    single { TicketMessageLocalDataSource(messageQueries = get<ru.macdroid.subagentstest.database.NotesDatabase>().ticketMessageQueries) }

    // Data Layer - Repositories
    singleOf(::FAQRepositoryImpl) bind FAQRepository::class
    single<SupportRepository> {
        SupportRepositoryImpl(
            ticketDataSource = get(),
            messageDataSource = get()
        )
    }

    // Domain Layer - AI Assistant
    singleOf(::SupportAIAssistant)

    // Domain Layer - Use Cases
    factoryOf(::GetAIAnswerUseCase)
    factoryOf(::CreateTicketUseCase)
    factoryOf(::InitializeFAQUseCase)

    // Presentation Layer - ViewModels
    viewModelOf(::SupportChatViewModel)
}

