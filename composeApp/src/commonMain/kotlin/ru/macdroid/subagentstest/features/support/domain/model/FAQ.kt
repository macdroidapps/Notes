package ru.macdroid.subagentstest.features.support.domain.model

import kotlinx.datetime.Instant

/**
 * FAQ item domain model
 */
data class FAQ(
    val id: Long = 0,
    val question: String,
    val answer: String,
    val category: FAQCategory,
    val keywords: List<String>,
    val viewCount: Int = 0,
    val helpfulCount: Int = 0,
    val createdAt: Instant
)

/**
 * FAQ categories matching common support topics
 */
enum class FAQCategory(val displayName: String) {
    AUTH("Авторизация"),
    SYNC("Си��хронизация"),
    NOTES("Заметки"),
    CATEGORIES("Категории"),
    UI("Интерфейс"),
    PERFORMANCE("Производительность"),
    OTHER("Другое");

    companion object {
        fun fromString(value: String): FAQCategory {
            return entries.find { it.name == value } ?: OTHER
        }
    }
}

