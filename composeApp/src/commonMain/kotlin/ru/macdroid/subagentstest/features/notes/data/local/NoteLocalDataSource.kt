package ru.macdroid.subagentstest.features.notes.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.database.NotesDatabase
import ru.macdroid.subagentstest.features.notes.domain.model.Note
import ru.macdroid.subagentstest.database.Note as NoteEntity

/**
 * Local data source for Note using SQLDelight
 */
class NoteLocalDataSource(
    private val database: NotesDatabase
) {
    private val queries = database.noteQueries

    fun getNotesByCategory(categoryId: Long): Flow<List<Note>> {
        return queries.selectByCategory(categoryId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    suspend fun getNoteById(id: Long): Note? {
        return withContext(Dispatchers.IO) {
            queries.selectById(id)
                .executeAsOneOrNull()
                ?.toDomainModel()
        }
    }

    fun searchNotes(searchText: String, categoryId: Long?): Flow<List<Note>> {
        return queries.searchNotes(searchText, categoryId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    suspend fun insertNote(categoryId: Long, title: String, content: String): Long {
        return withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.insert(
                category_id = categoryId,
                title = title,
                content = content,
                created_at = now,
                updated_at = now
            )
            // Get last inserted ID
            queries.selectAll().executeAsList().lastOrNull()?.id ?: -1L
        }
    }

    suspend fun updateNote(id: Long, title: String, content: String) {
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.update(
                id = id,
                title = title,
                content = content,
                updatedAt = now
            )
        }
    }

    suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }

    private fun NoteEntity.toDomainModel(): Note {
        return Note(
            id = id,
            categoryId = category_id,
            title = title,
            content = content,
            createdAt = Instant.fromEpochMilliseconds(created_at),
            updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )
    }
}
