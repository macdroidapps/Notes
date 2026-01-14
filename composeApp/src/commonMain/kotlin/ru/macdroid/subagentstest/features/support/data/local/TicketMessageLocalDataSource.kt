package ru.macdroid.subagentstest.features.support.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.database.TicketMessage as MessageEntity
import ru.macdroid.subagentstest.database.TicketMessageQueries
import ru.macdroid.subagentstest.features.support.domain.model.SenderType
import ru.macdroid.subagentstest.features.support.domain.model.TicketMessage

/**
 * Local data source for Ticket Message operations
 */
class TicketMessageLocalDataSource(
    private val messageQueries: TicketMessageQueries
) {
    fun getMessagesByTicketId(ticketId: Long): Flow<List<TicketMessage>> {
        return messageQueries.selectByTicketId(ticketId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun insertMessage(message: TicketMessage): Long = withContext(Dispatchers.Default) {
        messageQueries.insert(
            ticket_id = message.ticketId,
            sender_type = message.senderType.name,
            content = message.content,
            created_at = message.createdAt.toEpochMilliseconds()
        )
        messageQueries.lastInsertRowId().executeAsOne()
    }

    suspend fun deleteMessagesByTicketId(ticketId: Long) = withContext(Dispatchers.Default) {
        messageQueries.deleteByTicketId(ticketId)
    }

    private fun MessageEntity.toDomain(): TicketMessage {
        return TicketMessage(
            id = id,
            ticketId = ticket_id,
            senderType = SenderType.fromString(sender_type),
            content = content,
            createdAt = Instant.fromEpochMilliseconds(created_at)
        )
    }
}

