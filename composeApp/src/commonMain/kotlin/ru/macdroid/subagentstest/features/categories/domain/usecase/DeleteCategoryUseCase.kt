package ru.macdroid.subagentstest.features.categories.domain.usecase

import ru.macdroid.subagentstest.features.categories.domain.repository.CategoryRepository

/**
 * Use case for deleting a category
 */
class DeleteCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.deleteCategory(id)
    }
}
