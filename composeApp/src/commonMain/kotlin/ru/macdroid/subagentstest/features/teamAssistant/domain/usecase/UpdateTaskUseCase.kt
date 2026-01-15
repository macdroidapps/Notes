package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for updating an existing project task.
 */
class UpdateTaskUseCase(
    private val repository: TeamAssistantRepository
) {
    suspend operator fun invoke(task: ProjectTask): Result<Unit> {
        // Validation
        if (task.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be empty"))
        }

        if (task.title.length > 200) {
            return Result.failure(IllegalArgumentException("Task title is too long (max 200 characters)"))
        }

        return repository.updateTask(task)
    }
}
