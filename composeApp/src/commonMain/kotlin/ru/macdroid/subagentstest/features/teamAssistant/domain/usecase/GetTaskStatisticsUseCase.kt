package ru.macdroid.subagentstest.features.teamAssistant.domain.usecase

import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskStatistics
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Use case for retrieving aggregated task statistics.
 */
class GetTaskStatisticsUseCase(
    private val repository: TeamAssistantRepository
) {
    suspend operator fun invoke(): Result<TaskStatistics> {
        return repository.getStatistics()
    }
}
