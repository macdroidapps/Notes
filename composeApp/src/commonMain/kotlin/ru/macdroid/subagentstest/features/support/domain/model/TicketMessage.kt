package ru.macdroid.subagentstest.features.support.domain.model

import kotlinx.datetime.Instant

/**
 * Message in ticket conversation
 */
data class TicketMessage(
    val id: Long = 0,
    val ticketId: Long,
    val senderType: SenderType,
    val content: String,
    val createdAt: Instant
)

/**
 * Message sender type
 */
enum class SenderType(val displayName: String) {
    USER("Пользователь"),
    ASSISTANT("AI Ассистент"),
    SUPPORT("Поддержка");

    companion object {
        fun fromString(value: String): SenderType {
            return entries.find { it.name == value } ?: USER
        }
    }
}

