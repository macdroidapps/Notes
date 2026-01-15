package ru.macdroid.subagentstest.features.teamAssistant.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.teamAssistant.data.local.TeamAssistantLocalDataSource
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.*
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository

/**
 * Implementation of TeamAssistantRepository.
 * Wraps LocalDataSource calls with error handling and returns Result types for operations that can fail.
 */
class TeamAssistantRepositoryImpl(
    private val localDataSource: TeamAssistantLocalDataSource
) : TeamAssistantRepository {

    // ========== Query Operations (Reactive) ==========
    // Note: Flow operations handle errors via catch operator in the UI layer

    override fun getAllTasks(): Flow<List<ProjectTask>> {
        return localDataSource.getAllTasks()
    }

    override fun getTasksByStatus(status: TaskStatus): Flow<List<ProjectTask>> {
        return localDataSource.getTasksByStatus(status)
    }

    override fun getTasksByPriority(priority: TaskPriority): Flow<List<ProjectTask>> {
        return localDataSource.getTasksByPriority(priority)
    }

    override fun getOverdueTasks(currentTime: Instant): Flow<List<ProjectTask>> {
        return localDataSource.getOverdueTasks(currentTime)
    }

    override fun searchTasks(query: String): Flow<List<ProjectTask>> {
        return localDataSource.searchTasks(query)
    }

    // ========== Single Query Operations ==========

    override suspend fun getTaskById(id: Long): Result<ProjectTask?> {
        return runCatching {
            localDataSource.getTaskById(id)
        }
    }

    // ========== Mutation Operations ==========

    override suspend fun createTask(
        title: String,
        description: String,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: Instant?
    ): Result<Long> {
        return runCatching {
            localDataSource.insertTask(title, description, status, priority, dueDate)
        }
    }

    override suspend fun updateTask(task: ProjectTask): Result<Unit> {
        return runCatching {
            localDataSource.updateTask(task)
        }
    }

    override suspend fun updateTaskStatus(
        id: Long,
        status: TaskStatus,
        completedAt: Instant?
    ): Result<Unit> {
        return runCatching {
            localDataSource.updateTaskStatus(id, status, completedAt)
        }
    }

    override suspend fun deleteTask(id: Long): Result<Unit> {
        return runCatching {
            localDataSource.deleteTask(id)
        }
    }

    // ========== Statistics Operations ==========

    override suspend fun getStatistics(): Result<TaskStatistics> {
        return runCatching {
            localDataSource.getStatistics()
        }
    }
}
