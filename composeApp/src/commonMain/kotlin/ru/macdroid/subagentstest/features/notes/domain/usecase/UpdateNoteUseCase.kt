package ru.macdroid.subagentstest.features.notes.domain.usecase

import ru.macdroid.subagentstest.features.notes.domain.repository.NoteRepository

/**
 * Use case for updating an existing note
 */
class UpdateNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(
        id: Long,
        title: String,
        content: String
    ): Result<Unit> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Note title cannot be empty"))
        }
        return repository.updateNote(id, title, content)
    }
}
