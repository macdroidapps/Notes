package ru.macdroid.subagentstest.features.notes.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a note
 * Notes are always nested within a category
 */
data class Note(
    val id: Long,
    val categoryId: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
