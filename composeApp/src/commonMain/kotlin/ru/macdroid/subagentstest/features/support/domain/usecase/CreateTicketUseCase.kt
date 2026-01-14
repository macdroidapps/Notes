package ru.macdroid.subagentstest.features.support.domain.usecase

import kotlinx.datetime.Clock
import ru.macdroid.subagentstest.features.support.domain.model.FAQCategory
import ru.macdroid.subagentstest.features.support.domain.model.SupportTicket
import ru.macdroid.subagentstest.features.support.domain.model.TicketPriority
import ru.macdroid.subagentstest.features.support.domain.model.TicketStatus
import ru.macdroid.subagentstest.features.support.domain.repository.SupportRepository

/**
 * Use case for creating a new support ticket
 */
class CreateTicketUseCase(
    private val supportRepository: SupportRepository
) {
    suspend operator fun invoke(
        userName: String,
        userEmail: String,
        title: String,
        description: String,
        category: FAQCategory,
        priority: TicketPriority = TicketPriority.MEDIUM
    ): Result<Long> {
        return runCatching {
            val now = Clock.System.now()
            val ticket = SupportTicket(
                userName = userName,
                userEmail = userEmail,
                title = title,
                description = description,
                status = TicketStatus.OPEN,
                priority = priority,
                category = category,
                createdAt = now,
                updatedAt = now
            )

            supportRepository.createTicket(ticket).getOrThrow()
        }
    }
}

