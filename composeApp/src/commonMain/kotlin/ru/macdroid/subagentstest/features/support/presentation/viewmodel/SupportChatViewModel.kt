package ru.macdroid.subagentstest.features.support.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.macdroid.subagentstest.features.support.domain.model.*
import ru.macdroid.subagentstest.features.support.domain.repository.FAQRepository
import ru.macdroid.subagentstest.features.support.domain.repository.SupportRepository
import ru.macdroid.subagentstest.features.support.domain.usecase.CreateTicketUseCase
import ru.macdroid.subagentstest.features.support.domain.usecase.GetAIAnswerUseCase
import ru.macdroid.subagentstest.features.support.domain.usecase.InitializeFAQUseCase

/**
 * ViewModel for Support Chat Screen
 */
class SupportChatViewModel(
    private val getAIAnswerUseCase: GetAIAnswerUseCase,
    private val createTicketUseCase: CreateTicketUseCase,
    private val initializeFAQUseCase: InitializeFAQUseCase,
    private val supportRepository: SupportRepository,
    private val faqRepository: FAQRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SupportChatUiState>(SupportChatUiState.Initial)
    val uiState: StateFlow<SupportChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentTicketId = MutableStateFlow<Long?>(null)

    init {
        initializeFAQ()
        addWelcomeMessage()
    }

    private fun initializeFAQ() {
        viewModelScope.launch {
            initializeFAQUseCase()
        }
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            content = """
                👋 Здравствуйте! Я AI-ассистент службы поддержки.
                
                Я помогу вам найти ответы на вопросы о приложении для заметок.
                
                Задайте ваш вопрос, и я постараюсь помочь!
                
                Например:
                • Почему не работает авторизация?
                • Как синхронизировать заметки?
                • Не могу создать категорию
            """.trimIndent(),
            isUser = false,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        _messages.value = listOf(welcomeMessage)
    }

    fun sendMessage(question: String) {
        if (question.isBlank()) return

        // Добавляем вопрос пользователя
        val userMessage = ChatMessage(
            content = question,
            isUser = true,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        _messages.value = _messages.value + userMessage

        // Показываем индикатор загрузки
        _uiState.value = SupportChatUiState.Loading

        viewModelScope.launch {
            // Создаём тикет если его ещё нет
            val ticketId = _currentTicketId.value ?: createInitialTicket(question)
            _currentTicketId.value = ticketId

            // Получаем AI ответ
            getAIAnswerUseCase(question, ticketId)
                .onSuccess { answer ->
                    val assistantMessage = ChatMessage(
                        content = answer,
                        isUser = false,
                        timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    )
                    _messages.value = _messages.value + assistantMessage
                    _uiState.value = SupportChatUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = SupportChatUiState.Error(
                        error.message ?: "Произошла ошибка при получении ответа"
                    )
                }
        }
    }

    private suspend fun createInitialTicket(firstQuestion: String): Long {
        return createTicketUseCase(
            userName = "Пользователь", // В реальном приложении брать из профиля
            userEmail = "user@example.com",
            title = "Вопрос поддержки: ${firstQuestion.take(50)}",
            description = firstQuestion,
            category = detectCategory(firstQuestion),
            priority = TicketPriority.MEDIUM
        ).getOrElse { 0L }
    }

    private fun detectCategory(question: String): FAQCategory {
        val lowerQuestion = question.lowercase()
        return when {
            lowerQuestion.contains("авториз") || lowerQuestion.contains("логин") ||
            lowerQuestion.contains("пароль") || lowerQuestion.contains("вход") -> FAQCategory.AUTH

            lowerQuestion.contains("синхрониз") || lowerQuestion.contains("облак") ||
            lowerQuestion.contains("устройств") -> FAQCategory.SYNC

            lowerQuestion.contains("заметк") || lowerQuestion.contains("создать") ||
            lowerQuestion.contains("удалить") -> FAQCategory.NOTES

            lowerQuestion.contains("категор") || lowerQuestion.contains("папк") -> FAQCategory.CATEGORIES

            lowerQuestion.contains("интерфейс") || lowerQuestion.contains("тем") ||
            lowerQuestion.contains("дизайн") -> FAQCategory.UI

            lowerQuestion.contains("медленн") || lowerQuestion.contains("тормоз") ||
            lowerQuestion.contains("лаг") -> FAQCategory.PERFORMANCE

            else -> FAQCategory.OTHER
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _currentTicketId.value = null
        _uiState.value = SupportChatUiState.Initial
        addWelcomeMessage()
    }
}

/**
 * UI State for Support Chat
 */
sealed class SupportChatUiState {
    object Initial : SupportChatUiState()
    object Loading : SupportChatUiState()
    object Success : SupportChatUiState()
    data class Error(val message: String) : SupportChatUiState()
}

/**
 * Chat message model for UI
 */
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)

