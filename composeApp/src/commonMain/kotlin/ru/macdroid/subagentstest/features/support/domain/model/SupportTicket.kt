package ru.macdroid.subagentstest.features.support.domain.model

import kotlinx.datetime.Instant

/**
 * Support ticket domain model
 */
data class SupportTicket(
    val id: Long = 0,
    val userName: String,
    val userEmail: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val priority: TicketPriority,
    val category: FAQCategory,
    val createdAt: Instant,
    val updatedAt: Instant,
    val resolvedAt: Instant? = null
)

/**
 * Ticket status lifecycle
 */
enum class TicketStatus(val displayName: String) {
    OPEN("Открыт"),
    IN_PROGRESS("В ��аботе"),
    RESOLVED("Решён"),
    CLOSED("Закрыт");

    companion object {
        fun fromString(value: String): TicketStatus {
            return entries.find { it.name == value } ?: OPEN
        }
    }
}

/**
 * Ticket priority levels
 */
enum class TicketPriority(val displayName: String) {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий"),
    CRITICAL("Критический");

    companion object {
        fun fromString(value: String): TicketPriority {
            return entries.find { it.name == value } ?: MEDIUM
        }
    }
}

