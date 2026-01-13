package ru.macdroid.subagentstest.features.categories.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a category
 */
data class Category(
    val id: Long,
    val name: String,
    val color: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
