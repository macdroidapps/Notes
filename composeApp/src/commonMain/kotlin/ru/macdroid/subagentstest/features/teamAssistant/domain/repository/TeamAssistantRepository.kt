package ru.macdroid.subagentstest.features.teamAssistant.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.*

/**
 * Repository interface for Team Assistant feature.
 * Defines all data operations for project task management.
 * Implementations should handle error cases and return Result types for operations that can fail.
 */
interface TeamAssistantRepository {
    // ========== Query Operations (Reactive) ==========

    /**
     * Get all tasks, sorted by priority and creation date.
     * Returns a Flow that emits updates when tasks change.
     */
    fun getAllTasks(): Flow<List<ProjectTask>>

    /**
     * Get tasks filtered by status.
     * Returns a Flow that emits updates when tasks with the given status change.
     */
    fun getTasksByStatus(status: TaskStatus): Flow<List<ProjectTask>>

    /**
     * Get tasks filtered by priority.
     * Returns a Flow that emits updates when tasks with the given priority change.
     */
    fun getTasksByPriority(priority: TaskPriority): Flow<List<ProjectTask>>

    /**
     * Get tasks that are overdue (due_date < currentTime and status != DONE).
     * Returns a Flow that emits updates when overdue tasks change.
     */
    fun getOverdueTasks(currentTime: Instant): Flow<List<ProjectTask>>

    /**
     * Search tasks by title or description.
     * Returns a Flow that emits updates when matching tasks change.
     */
    fun searchTasks(query: String): Flow<List<ProjectTask>>

    // ========== Single Query Operations ==========

    /**
     * Get a single task by ID.
     * Returns Result.success with the task, or Result.success(null) if not found.
     */
    suspend fun getTaskById(id: Long): Result<ProjectTask?>

    // ========== Mutation Operations ==========

    /**
     * Create a new task.
     * Returns Result.success with the new task ID, or Result.failure on error.
     */
    suspend fun createTask(
        title: String,
        description: String,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: Instant? = null
    ): Result<Long>

    /**
     * Update an existing task.
     * Returns Result.success(Unit) on success, or Result.failure on error.
     */
    suspend fun updateTask(task: ProjectTask): Result<Unit>

    /**
     * Update only the status of a task (lightweight operation).
     * Automatically sets completedAt when status is DONE.
     * Returns Result.success(Unit) on success, or Result.failure on error.
     */
    suspend fun updateTaskStatus(
        id: Long,
        status: TaskStatus,
        completedAt: Instant?
    ): Result<Unit>

    /**
     * Delete a task by ID.
     * Returns Result.success(Unit) on success, or Result.failure on error.
     */
    suspend fun deleteTask(id: Long): Result<Unit>

    // ========== Statistics Operations ==========

    /**
     * Get aggregated task statistics.
     * Returns Result.success with TaskStatistics, or Result.failure on error.
     */
    suspend fun getStatistics(): Result<TaskStatistics>
}
