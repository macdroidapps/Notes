package ru.macdroid.subagentstest.features.categories.domain.usecase

import ru.macdroid.subagentstest.features.categories.domain.repository.CategoryRepository

/**
 * Use case for updating an existing category
 */
class UpdateCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(id: Long, name: String, color: String): Result<Unit> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Category name cannot be empty"))
        }
        return repository.updateCategory(id, name, color)
    }
}
