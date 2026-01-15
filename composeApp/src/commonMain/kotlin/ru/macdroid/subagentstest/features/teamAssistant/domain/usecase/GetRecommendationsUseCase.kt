package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskStatus
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for getting task recommendations (what to work on next).
 * Recommendation algorithm:
 * 1. Priority 1: Overdue tasks (if any) → top 3
 * 2. Priority 2: Sort by priority weight → due date → creation date → top 3
 */
class GetRecommendationsUseCase(
    private val repository: TeamAssistantRepository
) {
    suspend operator fun invoke(): Result<List<ProjectTask>> {
        return runCatching {
            val now = Clock.System.now()

            // Priority 1: Overdue tasks
            val overdueTasks = repository.getOverdueTasks(now).first()
            if (overdueTasks.isNotEmpty()) {
                return@runCatching overdueTasks
                    .sortedBy { it.priority.weight }
                    .take(3)
            }

            // Priority 2: Urgent tasks (Critical/High priority, sorted by due date)
            val allTasks = repository.getAllTasks().first()
            val urgentTasks = allTasks
                .filter { it.status != TaskStatus.DONE }
                .sortedWith(
                    compareBy<ProjectTask> { it.priority.weight }
                        .thenBy { it.dueDate ?: Instant.DISTANT_FUTURE }
                        .thenBy { it.createdAt }
                )
                .take(3)

            urgentTasks
        }
    }
}
