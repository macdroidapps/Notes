package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for retrieving overdue tasks.
 * Returns tasks where due_date < current time and status != DONE.
 */
class GetOverdueTasksUseCase(
    private val repository: TeamAssistantRepository
) {
    operator fun invoke(currentTime: Instant = Clock.System.now()): Flow<List<ProjectTask>> {
        return repository.getOverdueTasks(currentTime)
    }
}
