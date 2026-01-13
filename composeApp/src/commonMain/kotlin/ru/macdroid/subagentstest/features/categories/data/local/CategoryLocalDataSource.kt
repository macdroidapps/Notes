package ru.macdroid.subagentstest.features.categories.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.database.NotesDatabase
import ru.macdroid.subagentstest.features.categories.domain.model.Category
import ru.macdroid.subagentstest.database.Category as CategoryEntity

/**
 * Local data source for Category using SQLDelight
 */
class CategoryLocalDataSource(
    private val database: NotesDatabase
) {
    private val queries = database.categoryQueries

    fun getCategories(): Flow<List<Category>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    suspend fun getCategoryById(id: Long): Category? {
        return withContext(Dispatchers.IO) {
            queries.selectById(id)
                .executeAsOneOrNull()
                ?.toDomainModel()
        }
    }

    suspend fun insertCategory(name: String, color: String): Long {
        return withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.insert(
                name = name,
                color = color,
                created_at = now,
                updated_at = now
            )
            // Get last inserted ID
            queries.selectAll().executeAsList().lastOrNull()?.id ?: -1L
        }
    }

    suspend fun updateCategory(id: Long, name: String, color: String) {
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.update(
                id = id,
                name = name,
                color = color,
                updatedAt = now
            )
        }
    }

    suspend fun deleteCategory(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }

    private fun CategoryEntity.toDomainModel(): Category {
        return Category(
            id = id,
            name = name,
            color = color ?: "#000000",
            createdAt = Instant.fromEpochMilliseconds(created_at),
            updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )
    }
}
