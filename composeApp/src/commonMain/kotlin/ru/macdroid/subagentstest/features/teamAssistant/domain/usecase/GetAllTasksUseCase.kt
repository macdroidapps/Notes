package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for retrieving all project tasks.
 * Returns tasks sorted by priority (CRITICAL first) and creation date.
 */
class GetAllTasksUseCase(
    private val repository: TeamAssistantRepository
) {
    operator fun invoke(): Flow<List<ProjectTask>> {
        return repository.getAllTasks()
    }
}
