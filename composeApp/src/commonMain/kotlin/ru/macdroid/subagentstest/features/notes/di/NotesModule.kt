package ru.macdroid.subagentstest.features.notes.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.macdroid.subagentstest.features.notes.data.local.NoteLocalDataSource
import ru.macdroid.subagentstest.features.notes.data.repository.NoteRepositoryImpl
import ru.macdroid.subagentstest.features.notes.domain.repository.NoteRepository
import ru.macdroid.subagentstest.features.notes.domain.usecase.*
import ru.macdroid.subagentstest.features.notes.presentation.viewmodel.NotesViewModel

/**
 * Koin module for Notes feature
 * Registers all dependencies following Clean Architecture layers
 */
val notesModule = module {
    // Data Layer
    singleOf(::NoteLocalDataSource)
    singleOf(::NoteRepositoryImpl) bind NoteRepository::class

    // Domain Layer - Use Cases
    factoryOf(::GetNotesByCategoryUseCase)
    factoryOf(::CreateNoteUseCase)
    factoryOf(::UpdateNoteUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::SearchNotesUseCase)

    // Presentation Layer - ViewModel (with parameter)
    factory { (categoryId: Long) ->
        NotesViewModel(
            categoryId = categoryId,
            getNotesByCategoryUseCase = get(),
            createNoteUseCase = get(),
            updateNoteUseCase = get(),
            deleteNoteUseCase = get(),
            searchNotesUseCase = get()
        )
    }
}
