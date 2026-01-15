package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskPriority
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for retrieving tasks filtered by priority.
 */
class GetTasksByPriorityUseCase(
    private val repository: TeamAssistantRepository
) {
    operator fun invoke(priority: TaskPriority): Flow<List<ProjectTask>> {
        return repository.getTasksByPriority(priority)
    }
}
