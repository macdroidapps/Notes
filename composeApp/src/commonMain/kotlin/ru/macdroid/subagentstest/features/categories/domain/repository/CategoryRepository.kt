package ru.macdroid.subagentstest.features.categories.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.categories.domain.model.Category

/**
 * Repository interface for Category operations
 * Domain layer defines the contract, Data layer implements it
 */
interface CategoryRepository {
    /**
     * Get all categories as a Flow for reactive updates
     */
    fun getCategories(): Flow<List<Category>>

    /**
     * Get a single category by ID
     */
    suspend fun getCategoryById(id: Long): Category?

    /**
     * Create a new category
     */
    suspend fun createCategory(name: String, color: String): Result<Long>

    /**
     * Update an existing category
     */
    suspend fun updateCategory(id: Long, name: String, color: String): Result<Unit>

    /**
     * Delete a category by ID
     */
    suspend fun deleteCategory(id: Long): Result<Unit>
}
