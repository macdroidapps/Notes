package ru.macdroid.subagentstest.features.notes.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.notes.domain.model.Note
import ru.macdroid.subagentstest.features.notes.domain.repository.NoteRepository

/**
 * Use case for searching notes by text
 */
class SearchNotesUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(searchText: String, categoryId: Long? = null): Flow<List<Note>> {
        return repository.searchNotes(searchText, categoryId)
    }
}
