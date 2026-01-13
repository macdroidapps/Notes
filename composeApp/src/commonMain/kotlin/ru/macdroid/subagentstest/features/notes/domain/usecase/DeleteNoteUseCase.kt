package ru.macdroid.subagentstest.features.notes.domain.usecase

import ru.macdroid.subagentstest.features.notes.domain.repository.NoteRepository

/**
 * Use case for deleting a note
 */
class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.deleteNote(id)
    }
}
