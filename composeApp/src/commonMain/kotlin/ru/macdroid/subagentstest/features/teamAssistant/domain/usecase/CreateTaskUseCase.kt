package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskPriority
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskStatus
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for creating a new project task.
 * Includes validation logic for task title and description.
 */
class CreateTaskUseCase(
    private val repository: TeamAssistantRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String = "",
        priority: TaskPriority = TaskPriority.MEDIUM,
        dueDate: Instant? = null
    ): Result<Long> {
        // Validation: Title cannot be blank
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be empty"))
        }

        // Validation: Title length limit
        if (title.length > 200) {
            return Result.failure(IllegalArgumentException("Task title is too long (max 200 characters)"))
        }

        // Validation: Description length limit
        if (description.length > 5000) {
            return Result.failure(IllegalArgumentException("Task description is too long (max 5000 characters)"))
        }

        return repository.createTask(
            title = title.trim(),
            description = description.trim(),
            status = TaskStatus.TODO,
            priority = priority,
            dueDate = dueDate
        )
    }
}
