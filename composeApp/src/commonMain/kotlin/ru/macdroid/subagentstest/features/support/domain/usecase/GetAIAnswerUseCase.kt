package ru.macdroid.subagentstest.features.support.domain.usecase

import kotlinx.datetime.Clock
import ru.macdroid.subagentstest.features.support.domain.model.SenderType
import ru.macdroid.subagentstest.features.support.domain.model.SupportTicket
import ru.macdroid.subagentstest.features.support.domain.model.TicketMessage
import ru.macdroid.subagentstest.features.support.domain.ai.SupportAIAssistant
import ru.macdroid.subagentstest.features.support.domain.repository.SupportRepository

/**
 * Use case for getting AI-powered answer to user question
 */
class GetAIAnswerUseCase(
    private val aiAssistant: SupportAIAssistant,
    private val supportRepository: SupportRepository
) {
    suspend operator fun invoke(
        question: String,
        ticketId: Long? = null
    ): Result<String> {
        return runCatching {
            // Получаем контекст тикета если указан
            val ticket = ticketId?.let {
                supportRepository.getTicketById(it).getOrNull()
            }

            // Получаем AI ответ
            val aiAnswer = aiAssistant.getAnswer(question, ticket).getOrThrow()

            // Сохраняем вопрос и ответ в историю (если есть тикет)
            ticketId?.let { id ->
                // Сохраняем вопрос пользователя
                supportRepository.addMessage(
                    TicketMessage(
                        ticketId = id,
                        senderType = SenderType.USER,
                        content = question,
                        createdAt = Clock.System.now()
                    )
                )

                // Сох��аняем ответ ассистента
                supportRepository.addMessage(
                    TicketMessage(
                        ticketId = id,
                        senderType = SenderType.ASSISTANT,
                        content = aiAnswer.answer,
                        createdAt = Clock.System.now()
                    )
                )
            }

            aiAnswer.answer
        }
    }
}

