package ru.macdroid.subagentstest.features.notes.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.notes.domain.model.Note
import ru.macdroid.subagentstest.features.notes.domain.repository.NoteRepository

/**
 * Use case for getting all notes in a category
 */
class GetNotesByCategoryUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(categoryId: Long): Flow<List<Note>> {
        return repository.getNotesByCategory(categoryId)
    }
}
