package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import ru.macdroid.subagentstest.features.teamAssistant.domain.ai.TeamAssistant
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.AssistantResponse

/**
 * Use case for getting the Team Assistant's response to a user message.
 * This is the main entry point for the chat functionality.
 */
class GetAssistantResponseUseCase(
    private val teamAssistant: TeamAssistant
) {
    suspend operator fun invoke(message: String): Result<AssistantResponse> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }

        return teamAssistant.processMessage(message)
    }
}
