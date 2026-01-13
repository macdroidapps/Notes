package ru.macdroid.subagentstest.features.notes.domain.usecase

import ru.macdroid.subagentstest.features.notes.domain.repository.NoteRepository

/**
 * Use case for creating a new note
 */
class CreateNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        title: String,
        content: String
    ): Result<Long> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Note title cannot be empty"))
        }
        return repository.createNote(categoryId, title, content)
    }
}
