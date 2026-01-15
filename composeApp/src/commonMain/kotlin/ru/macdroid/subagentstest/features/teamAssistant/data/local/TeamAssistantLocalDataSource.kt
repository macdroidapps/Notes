package ru.macdroid.subagentstest.features.teamAssistant.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.database.NotesDatabase
import ru.macdroid.subagentstest.database.ProjectTask
import ru.macdroid.subagentstest.database.SelectOverdueTasks
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.ProjectTask as DomainProjectTask
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskStatus
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskPriority
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskStatistics

/**
 * Local data source for Team Assistant feature.
 * Handles all SQLDelight database operations for project tasks.
 */
class TeamAssistantLocalDataSource(
    private val database: NotesDatabase
) {
    private val queries = database.projectTaskQueries

    // ========== Query Operations (Reactive) ==========

    fun getAllTasks(): Flow<List<DomainProjectTask>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { toDomainModel(it) } }
    }

    fun getTasksByStatus(status: TaskStatus): Flow<List<DomainProjectTask>> {
        return queries.selectByStatus(status.name)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { toDomainModel(it) } }
    }

    fun getTasksByPriority(priority: TaskPriority): Flow<List<DomainProjectTask>> {
        return queries.selectByPriority(priority.name)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { toDomainModel(it) } }
    }

    fun getOverdueTasks(currentTime: Instant): Flow<List<DomainProjectTask>> {
        return queries.selectOverdueTasks(currentTime.toEpochMilliseconds())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { toProjectTaskFromOverdue(it) }.map { toDomainModel(it) } }
    }

    fun searchTasks(query: String): Flow<List<DomainProjectTask>> {
        return queries.searchTasks(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { toDomainModel(it) } }
    }

    // ========== Single Query Operations ==========

    suspend fun getTaskById(id: Long): DomainProjectTask? {
        return withContext(Dispatchers.IO) {
            queries.selectById(id).executeAsOneOrNull()?.let { toDomainModel(it) }
        }
    }

    // ========== Mutation Operations ==========

    suspend fun insertTask(
        title: String,
        description: String,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: Instant?
    ): Long {
        return withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.insert(
                title = title,
                description = description,
                status = status.name,
                priority = priority.name,
                created_at = now,
                updated_at = now,
                due_date = dueDate?.toEpochMilliseconds(),
                completed_at = null
            )
            queries.lastInsertRowId().executeAsOne()
        }
    }

    suspend fun updateTask(task: DomainProjectTask) {
        withContext(Dispatchers.IO) {
            queries.update(
                id = task.id,
                title = task.title,
                description = task.description,
                status = task.status.name,
                priority = task.priority.name,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                dueDate = task.dueDate?.toEpochMilliseconds(),
                completedAt = task.completedAt?.toEpochMilliseconds()
            )
        }
    }

    suspend fun updateTaskStatus(id: Long, status: TaskStatus, completedAt: Instant?) {
        withContext(Dispatchers.IO) {
            queries.updateStatus(
                id = id,
                status = status.name,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                completedAt = completedAt?.toEpochMilliseconds()
            )
        }
    }

    suspend fun deleteTask(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }

    // ========== Statistics Operations ==========

    suspend fun getStatistics(): TaskStatistics {
        return withContext(Dispatchers.IO) {
            val statusCounts = queries.countByStatus().executeAsList()
            val priorityCounts = queries.countByPriority().executeAsList()
            val overdueCount = queries.countOverdue(Clock.System.now().toEpochMilliseconds()).executeAsOne()

            val todoCount = statusCounts.find { it.status == TaskStatus.TODO.name }?.COUNT ?: 0
            val inProgressCount = statusCounts.find { it.status == TaskStatus.IN_PROGRESS.name }?.COUNT ?: 0
            val doneCount = statusCounts.find { it.status == TaskStatus.DONE.name }?.COUNT ?: 0

            val criticalCount = priorityCounts.find { it.priority == TaskPriority.CRITICAL.name }?.COUNT ?: 0
            val highCount = priorityCounts.find { it.priority == TaskPriority.HIGH.name }?.COUNT ?: 0
            val mediumCount = priorityCounts.find { it.priority == TaskPriority.MEDIUM.name }?.COUNT ?: 0
            val lowCount = priorityCounts.find { it.priority == TaskPriority.LOW.name }?.COUNT ?: 0

            TaskStatistics(
                totalTasks = (todoCount + inProgressCount + doneCount).toInt(),
                todoCount = todoCount.toInt(),
                inProgressCount = inProgressCount.toInt(),
                doneCount = doneCount.toInt(),
                overdueCount = overdueCount.toInt(),
                criticalCount = criticalCount.toInt(),
                highCount = highCount.toInt(),
                mediumCount = mediumCount.toInt(),
                lowCount = lowCount.toInt()
            )
        }
    }

    // ========== Entity Mapping ==========

    private fun toDomainModel(entity: ProjectTask): DomainProjectTask {
        return DomainProjectTask(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            status = TaskStatus.fromString(entity.status),
            priority = TaskPriority.fromString(entity.priority),
            createdAt = Instant.fromEpochMilliseconds(entity.created_at),
            updatedAt = Instant.fromEpochMilliseconds(entity.updated_at),
            dueDate = entity.due_date?.let { Instant.fromEpochMilliseconds(it) },
            completedAt = entity.completed_at?.let { Instant.fromEpochMilliseconds(it) }
        )
    }

    private fun toProjectTaskFromOverdue(entity: SelectOverdueTasks): ProjectTask {
        return ProjectTask(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            status = entity.status,
            priority = entity.priority,
            created_at = entity.created_at,
            updated_at = entity.updated_at,
            due_date = entity.due_date,
            completed_at = entity.completed_at
        )
    }
}
