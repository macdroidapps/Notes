package ru.macdroid.subagentstest.features.support.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.support.domain.model.FAQCategory
import ru.macdroid.subagentstest.features.support.domain.model.SupportTicket
import ru.macdroid.subagentstest.features.support.domain.model.TicketMessage
import ru.macdroid.subagentstest.features.support.domain.model.TicketStatus

/**
 * Repository interface for Support Ticket operations
 */
interface SupportRepository {
    fun getAllTickets(): Flow<List<SupportTicket>>
    suspend fun getTicketById(id: Long): Result<SupportTicket?>
    fun getTicketsByStatus(status: TicketStatus): Flow<List<SupportTicket>>
    fun getTicketsByCategory(category: FAQCategory): Flow<List<SupportTicket>>
    suspend fun createTicket(ticket: SupportTicket): Result<Long>
    suspend fun updateTicketStatus(id: Long, status: TicketStatus, resolvedAt: Instant? = null): Result<Unit>
    suspend fun deleteTicket(id: Long): Result<Unit>

    // Messages
    fun getTicketMessages(ticketId: Long): Flow<List<TicketMessage>>
    suspend fun addMessage(message: TicketMessage): Result<Long>
}

