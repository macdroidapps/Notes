package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskStatus
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for retrieving tasks filtered by status.
 */
class GetTasksByStatusUseCase(
    private val repository: TeamAssistantRepository
) {
    operator fun invoke(status: TaskStatus): Flow<List<ProjectTask>> {
        return repository.getTasksByStatus(status)
    }
}
