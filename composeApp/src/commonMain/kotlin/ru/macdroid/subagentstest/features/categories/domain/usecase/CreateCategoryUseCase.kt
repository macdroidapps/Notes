package ru.macdroid.subagentstest.features.categories.domain.usecase

import ru.macdroid.subagentstest.features.categories.domain.repository.CategoryRepository

/**
 * Use case for creating a new category
 */
class CreateCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(name: String, color: String): Result<Long> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Category name cannot be empty"))
        }
        return repository.createCategory(name, color)
    }
}
