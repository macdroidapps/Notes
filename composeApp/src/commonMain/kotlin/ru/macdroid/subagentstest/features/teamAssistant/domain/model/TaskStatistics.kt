package ru.macdroid.subagentstest.features.teamAssistant.domain.model

/**
 * Domain model containing aggregated task statistics.
 * Used by the Team Assistant to provide insights about task distribution and progress.
 */
data class TaskStatistics(
    val totalTasks: Int,
    val todoCount: Int,
    val inProgressCount: Int,
    val doneCount: Int,
    val overdueCount: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int
)
