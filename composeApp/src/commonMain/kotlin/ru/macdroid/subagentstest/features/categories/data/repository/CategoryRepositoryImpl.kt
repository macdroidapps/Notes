package ru.macdroid.subagentstest.features.categories.data.repository

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.categories.data.local.CategoryLocalDataSource
import ru.macdroid.subagentstest.features.categories.domain.model.Category
import ru.macdroid.subagentstest.features.categories.domain.repository.CategoryRepository

/**
 * Implementation of CategoryRepository
 * Delegates to LocalDataSource for data operations
 */
class CategoryRepositoryImpl(
    private val localDataSource: CategoryLocalDataSource
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return localDataSource.getCategories()
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return try {
            localDataSource.getCategoryById(id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createCategory(name: String, color: String): Result<Long> {
        return try {
            val id = localDataSource.insertCategory(name, color)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(id: Long, name: String, color: String): Result<Unit> {
        return try {
            localDataSource.updateCategory(id, name, color)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(id: Long): Result<Unit> {
        return try {
            localDataSource.deleteCategory(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
