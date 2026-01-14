package ru.macdroid.subagentstest.features.support.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.support.data.local.SupportTicketLocalDataSource
import ru.macdroid.subagentstest.features.support.data.local.TicketMessageLocalDataSource
import ru.macdroid.subagentstest.features.support.domain.model.FAQCategory
import ru.macdroid.subagentstest.features.support.domain.model.SupportTicket
import ru.macdroid.subagentstest.features.support.domain.model.TicketMessage
import ru.macdroid.subagentstest.features.support.domain.model.TicketStatus
import ru.macdroid.subagentstest.features.support.domain.repository.SupportRepository

/**
 * Implementation of Support Repository
 */
class SupportRepositoryImpl(
    private val ticketDataSource: SupportTicketLocalDataSource,
    private val messageDataSource: TicketMessageLocalDataSource
) : SupportRepository {

    override fun getAllTickets(): Flow<List<SupportTicket>> {
        return ticketDataSource.getAllTickets()
    }

    override suspend fun getTicketById(id: Long): Result<SupportTicket?> {
        return runCatching {
            ticketDataSource.getTicketById(id)
        }
    }

    override fun getTicketsByStatus(status: TicketStatus): Flow<List<SupportTicket>> {
        return ticketDataSource.getTicketsByStatus(status)
    }

    override fun getTicketsByCategory(category: FAQCategory): Flow<List<SupportTicket>> {
        return ticketDataSource.getTicketsByCategory(category)
    }

    override suspend fun createTicket(ticket: SupportTicket): Result<Long> {
        return runCatching {
            ticketDataSource.insertTicket(ticket)
        }
    }

    override suspend fun updateTicketStatus(
        id: Long,
        status: TicketStatus,
        resolvedAt: Instant?
    ): Result<Unit> {
        return runCatching {
            ticketDataSource.updateTicketStatus(
                id = id,
                status = status,
                updatedAt = Clock.System.now(),
                resolvedAt = resolvedAt
            )
        }
    }

    override suspend fun deleteTicket(id: Long): Result<Unit> {
        return runCatching {
            ticketDataSource.deleteTicket(id)
        }
    }

    override fun getTicketMessages(ticketId: Long): Flow<List<TicketMessage>> {
        return messageDataSource.getMessagesByTicketId(ticketId)
    }

    override suspend fun addMessage(message: TicketMessage): Result<Long> {
        return runCatching {
            messageDataSource.insertMessage(message)
        }
    }
}

