package ru.macdroid.subagentstest.features.support.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.database.SupportTicket as TicketEntity
import ru.macdroid.subagentstest.database.SupportTicketQueries
import ru.macdroid.subagentstest.features.support.domain.model.FAQCategory
import ru.macdroid.subagentstest.features.support.domain.model.SupportTicket
import ru.macdroid.subagentstest.features.support.domain.model.TicketPriority
import ru.macdroid.subagentstest.features.support.domain.model.TicketStatus

/**
 * Local data source for Support Ticket operations
 */
class SupportTicketLocalDataSource(
    private val ticketQueries: SupportTicketQueries
) {
    fun getAllTickets(): Flow<List<SupportTicket>> {
        return ticketQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun getTicketById(id: Long): SupportTicket? = withContext(Dispatchers.Default) {
        ticketQueries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    fun getTicketsByStatus(status: TicketStatus): Flow<List<SupportTicket>> {
        return ticketQueries.selectByStatus(status.name)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    fun getTicketsByCategory(category: FAQCategory): Flow<List<SupportTicket>> {
        return ticketQueries.selectByCategory(category.name)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun insertTicket(ticket: SupportTicket): Long = withContext(Dispatchers.Default) {
        ticketQueries.insert(
            user_name = ticket.userName,
            user_email = ticket.userEmail,
            title = ticket.title,
            description = ticket.description,
            status = ticket.status.name,
            priority = ticket.priority.name,
            category = ticket.category.name,
            created_at = ticket.createdAt.toEpochMilliseconds(),
            updated_at = ticket.updatedAt.toEpochMilliseconds(),
            resolved_at = ticket.resolvedAt?.toEpochMilliseconds()
        )
        ticketQueries.lastInsertRowId().executeAsOne()
    }

    suspend fun updateTicketStatus(
        id: Long,
        status: TicketStatus,
        updatedAt: Instant,
        resolvedAt: Instant? = null
    ) = withContext(Dispatchers.Default) {
        ticketQueries.updateStatus(
            id = id,
            status = status.name,
            updated_at = updatedAt.toEpochMilliseconds(),
            resolved_at = resolvedAt?.toEpochMilliseconds()
        )
    }

    suspend fun deleteTicket(id: Long) = withContext(Dispatchers.Default) {
        ticketQueries.deleteById(id)
    }

    private fun TicketEntity.toDomain(): SupportTicket {
        return SupportTicket(
            id = id,
            userName = user_name,
            userEmail = user_email,
            title = title,
            description = description,
            status = TicketStatus.fromString(status),
            priority = TicketPriority.fromString(priority),
            category = FAQCategory.fromString(category),
            createdAt = Instant.fromEpochMilliseconds(created_at),
            updatedAt = Instant.fromEpochMilliseconds(updated_at),
            resolvedAt = resolved_at?.let { Instant.fromEpochMilliseconds(it) }
        )
    }
}

