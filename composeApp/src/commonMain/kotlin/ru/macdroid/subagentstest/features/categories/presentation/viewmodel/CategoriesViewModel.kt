package ru.macdroid.subagentstest.features.categories.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.macdroid.subagentstest.features.categories.domain.model.Category
import ru.macdroid.subagentstest.features.categories.domain.usecase.CreateCategoryUseCase
import ru.macdroid.subagentstest.features.categories.domain.usecase.DeleteCategoryUseCase
import ru.macdroid.subagentstest.features.categories.domain.usecase.GetCategoriesUseCase

/**
 * ViewModel for Categories list screen
 * Follows Clean Architecture principles:
 * - No direct database/repository access
 * - All business logic through Use Cases
 * - Immutable state via StateFlow
 */
class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CategoriesEvent>()
    val events: SharedFlow<CategoriesEvent> = _events.asSharedFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .catch { e ->
                    _uiState.value = CategoriesUiState.Error(e.message ?: "Unknown error")
                }
                .collect { categories ->
                    _uiState.value = if (categories.isEmpty()) {
                        CategoriesUiState.Empty
                    } else {
                        CategoriesUiState.Success(categories)
                    }
                }
        }
    }

    fun onCreateCategory(name: String, color: String) {
        viewModelScope.launch {
            createCategoryUseCase(name, color)
                .onSuccess {
                    _events.emit(CategoriesEvent.CategoryCreated)
                }
                .onFailure { e ->
                    _events.emit(CategoriesEvent.Error(e.message ?: "Failed to create category"))
                }
        }
    }

    fun onDeleteCategory(id: Long) {
        viewModelScope.launch {
            deleteCategoryUseCase(id)
                .onSuccess {
                    _events.emit(CategoriesEvent.CategoryDeleted)
                }
                .onFailure { e ->
                    _events.emit(CategoriesEvent.Error(e.message ?: "Failed to delete category"))
                }
        }
    }

    fun onCategoryClick(categoryId: Long) {
        viewModelScope.launch {
            _events.emit(CategoriesEvent.NavigateToNotes(categoryId))
        }
    }
}

/**
 * UI State for Categories screen
 * Sealed interface for type-safe state handling
 */
sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Empty : CategoriesUiState
    data class Success(val categories: List<Category>) : CategoriesUiState
    data class Error(val message: String) : CategoriesUiState
}

/**
 * One-time events for Categories screen
 * Separate from state to handle navigation, toasts, etc.
 */
sealed interface CategoriesEvent {
    data object CategoryCreated : CategoriesEvent
    data object CategoryDeleted : CategoriesEvent
    data class NavigateToNotes(val categoryId: Long) : CategoriesEvent
    data class Error(val message: String) : CategoriesEvent
}
