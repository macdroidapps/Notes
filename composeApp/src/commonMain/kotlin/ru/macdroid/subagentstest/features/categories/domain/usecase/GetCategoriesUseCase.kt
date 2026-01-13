package ru.macdroid.subagentstest.features.categories.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.categories.domain.model.Category
import ru.macdroid.subagentstest.features.categories.domain.repository.CategoryRepository

/**
 * Use case for getting all categories
 */
class GetCategoriesUseCase(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return repository.getCategories()
    }
}
