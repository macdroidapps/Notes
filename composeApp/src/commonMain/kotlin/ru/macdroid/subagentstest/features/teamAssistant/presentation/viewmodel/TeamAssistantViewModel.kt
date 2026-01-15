package ru.macdroid.subagentstest.features.teamAssistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.usecase.GetAssistantResponseUseCase

/**
 * ViewModel for Team Assistant Chat Screen.
 * Manages chat state, message history, and user interactions with the AI assistant.
 */
class TeamAssistantViewModel(
    private val getAssistantResponseUseCase: GetAssistantResponseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamAssistantUiState>(TeamAssistantUiState.Initial)
    val uiState: StateFlow<TeamAssistantUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            content = """
                👋 Привет! Я Team Assistant — твой AI помощник по управлению задачами.

                Я могу помочь тебе:
                • 📋 Показать все задачи и статистику
                • ✅ Создать новую задачу
                • 💡 Дать рекомендации, что делать дальше
                • ⏰ Показать просроченные задачи

                Примеры команд:
                • "Покажи все задачи"
                • "Создай задачу: Написать отчёт, высокий приоритет"
                • "Что мне делать дальше?"
                • "Сколько задач в работе?"
            """.trimIndent(),
            isUser = false,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            relatedTasks = emptyList()
        )
        _messages.value = listOf(welcomeMessage)
    }

    /**
     * Send a message to the Team Assistant.
     * Adds user message to chat, processes it through the AI, and adds the response.
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Add user message
        val userMessage = ChatMessage(
            content = message,
            isUser = true,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            relatedTasks = emptyList()
        )
        _messages.value = _messages.value + userMessage

        // Show loading
        _uiState.value = TeamAssistantUiState.Loading

        viewModelScope.launch {
            getAssistantResponseUseCase(message)
                .onSuccess { response ->
                    val assistantMessage = ChatMessage(
                        content = response.message,
                        isUser = false,
                        timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                        relatedTasks = response.tasks
                    )
                    _messages.value = _messages.value + assistantMessage
                    _uiState.value = TeamAssistantUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = TeamAssistantUiState.Error(
                        error.message ?: "Произошла ошибка"
                    )
                }
        }
    }

    /**
     * Clear chat history and reset to initial state with welcome message.
     */
    fun clearChat() {
        _messages.value = emptyList()
        _uiState.value = TeamAssistantUiState.Initial
        addWelcomeMessage()
    }
}

/**
 * UI state for Team Assistant screen.
 */
sealed class TeamAssistantUiState {
    data object Initial : TeamAssistantUiState()
    data object Loading : TeamAssistantUiState()
    data object Success : TeamAssistantUiState()
    data class Error(val message: String) : TeamAssistantUiState()
}

/**
 * Chat message data class for display in the UI.
 */
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val relatedTasks: List<ProjectTask>
)
