package ru.macdroid.subagentstest.features.notes.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.notes.domain.model.Note

/**
 * Repository interface for Note operations
 * Domain layer defines the contract, Data layer implements it
 */
interface NoteRepository {
    /**
     * Get all notes for a specific category
     */
    fun getNotesByCategory(categoryId: Long): Flow<List<Note>>

    /**
     * Get a single note by ID
     */
    suspend fun getNoteById(id: Long): Note?

    /**
     * Search notes by text (title or content)
     */
    fun searchNotes(searchText: String, categoryId: Long? = null): Flow<List<Note>>

    /**
     * Create a new note
     */
    suspend fun createNote(
        categoryId: Long,
        title: String,
        content: String
    ): Result<Long>

    /**
     * Update an existing note
     */
    suspend fun updateNote(
        id: Long,
        title: String,
        content: String
    ): Result<Unit>

    /**
     * Delete a note by ID
     */
    suspend fun deleteNote(id: Long): Result<Unit>
}
