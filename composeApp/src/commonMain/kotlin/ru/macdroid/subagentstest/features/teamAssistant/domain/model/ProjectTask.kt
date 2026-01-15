package ru.macdroid.subagentstest.features.teamAssistant.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a project task.
 * This is a pure Kotlin data class with no platform dependencies.
 */
data class ProjectTask(
    val id: Long = 0,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val createdAt: Instant,
    val updatedAt: Instant,
    val dueDate: Instant? = null,
    val completedAt: Instant? = null
)
