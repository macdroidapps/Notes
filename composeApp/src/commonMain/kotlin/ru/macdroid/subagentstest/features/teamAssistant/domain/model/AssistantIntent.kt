package ru.macdroid.subagentstest.features.teamAssistant.domain.model

import kotlinx.datetime.Instant

/**
 * Sealed class representing user intent recognized by the Team Assistant.
 * Used by IntentRecognizer to classify user messages into actionable intents.
 */
sealed class AssistantIntent {
    /**
     * Query tasks with optional filters
     */
    data class QueryTasks(val filter: TaskFilter) : AssistantIntent()

    /**
     * Request task statistics
     */
    data object QueryStatistics : AssistantIntent()

    /**
     * Query overdue tasks
     */
    data object QueryOverdue : AssistantIntent()

    /**
     * Create a new task
     */
    data class CreateTask(
        val title: String,
        val description: String?,
        val priority: TaskPriority,
        val dueDate: Instant?
    ) : AssistantIntent()

    /**
     * Update task status
     */
    data class UpdateTaskStatus(
        val taskId: Long,
        val newStatus: TaskStatus
    ) : AssistantIntent()

    /**
     * Get task recommendations (what to work on next)
     */
    data object GetRecommendations : AssistantIntent()

    /**
     * Unknown or unrecognized intent
     */
    data object Unknown : AssistantIntent()
}

/**
 * Filter criteria for querying tasks
 */
data class TaskFilter(
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val overdue: Boolean = false
)
