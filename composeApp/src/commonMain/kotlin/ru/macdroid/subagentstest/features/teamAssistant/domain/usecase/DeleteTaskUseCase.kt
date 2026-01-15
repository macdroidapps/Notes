package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for deleting a project task.
 */
class DeleteTaskUseCase(
    private val repository: TeamAssistantRepository
) {
    suspend operator fun invoke(taskId: Long): Result<Unit> {
        return repository.deleteTask(taskId)
    }
}
