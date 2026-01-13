package ru.macdroid.subagentstest.features.notes.data.repository

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.notes.data.local.NoteLocalDataSource
import ru.macdroid.subagentstest.features.notes.domain.model.Note
import ru.macdroid.subagentstest.features.notes.domain.repository.NoteRepository

/**
 * Implementation of NoteRepository
 * Delegates to LocalDataSource for data operations
 */
class NoteRepositoryImpl(
    private val localDataSource: NoteLocalDataSource
) : NoteRepository {

    override fun getNotesByCategory(categoryId: Long): Flow<List<Note>> {
        return localDataSource.getNotesByCategory(categoryId)
    }

    override suspend fun getNoteById(id: Long): Note? {
        return try {
            localDataSource.getNoteById(id)
        } catch (e: Exception) {
            null
        }
    }

    override fun searchNotes(searchText: String, categoryId: Long?): Flow<List<Note>> {
        return localDataSource.searchNotes(searchText, categoryId)
    }

    override suspend fun createNote(
        categoryId: Long,
        title: String,
        content: String
    ): Result<Long> {
        return try {
            val id = localDataSource.insertNote(categoryId, title, content)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(id: Long, title: String, content: String): Result<Unit> {
        return try {
            localDataSource.updateNote(id, title, content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: Long): Result<Unit> {
        return try {
            localDataSource.deleteNote(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
