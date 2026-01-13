package ru.macdroid.subagentstest.features.notes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.macdroid.subagentstest.features.notes.domain.model.Note
import ru.macdroid.subagentstest.features.notes.domain.usecase.*

/**
 * ViewModel for Notes list screen
 */
class NotesViewModel(
    private val categoryId: Long,
    private val getNotesByCategoryUseCase: GetNotesByCategoryUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val searchNotesUseCase: SearchNotesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotesEvent>()
    val events: SharedFlow<NotesEvent> = _events.asSharedFlow()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        getNotesByCategoryUseCase(categoryId)
                    } else {
                        searchNotesUseCase(query, categoryId)
                    }
                }
                .catch { e ->
                    _uiState.value = NotesUiState.Error(e.message ?: "Unknown error")
                }
                .collect { notes ->
                    _uiState.value = if (notes.isEmpty()) {
                        NotesUiState.Empty
                    } else {
                        NotesUiState.Success(notes)
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCreateNote(title: String, content: String) {
        viewModelScope.launch {
            createNoteUseCase(categoryId, title, content)
                .onSuccess {
                    _events.emit(NotesEvent.NoteCreated)
                }
                .onFailure { e ->
                    _events.emit(NotesEvent.Error(e.message ?: "Failed to create note"))
                }
        }
    }

    fun onUpdateNote(id: Long, title: String, content: String) {
        viewModelScope.launch {
            updateNoteUseCase(id, title, content)
                .onSuccess {
                    _events.emit(NotesEvent.NoteUpdated)
                }
                .onFailure { e ->
                    _events.emit(NotesEvent.Error(e.message ?: "Failed to update note"))
                }
        }
    }

    fun onDeleteNote(id: Long) {
        viewModelScope.launch {
            deleteNoteUseCase(id)
                .onSuccess {
                    _events.emit(NotesEvent.NoteDeleted)
                }
                .onFailure { e ->
                    _events.emit(NotesEvent.Error(e.message ?: "Failed to delete note"))
                }
        }
    }

    fun onNoteClick(noteId: Long) {
        viewModelScope.launch {
            _events.emit(NotesEvent.NavigateToEditor(noteId))
        }
    }
}

/**
 * UI State for Notes screen
 */
sealed interface NotesUiState {
    data object Loading : NotesUiState
    data object Empty : NotesUiState
    data class Success(val notes: List<Note>) : NotesUiState
    data class Error(val message: String) : NotesUiState
}

/**
 * One-time events for Notes screen
 */
sealed interface NotesEvent {
    data object NoteCreated : NotesEvent
    data object NoteUpdated : NotesEvent
    data object NoteDeleted : NotesEvent
    data class NavigateToEditor(val noteId: Long) : NotesEvent
    data class Error(val message: String) : NotesEvent
}
