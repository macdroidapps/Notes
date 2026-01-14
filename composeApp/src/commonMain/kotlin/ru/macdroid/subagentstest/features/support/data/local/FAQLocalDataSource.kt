package ru.macdroid.subagentstest.features.support.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.database.FAQ as FAQEntity
import ru.macdroid.subagentstest.database.FAQQueries
import ru.macdroid.subagentstest.features.support.domain.model.FAQ
import ru.macdroid.subagentstest.features.support.domain.model.FAQCategory

/**
 * Local data source for FAQ operations
 */
class FAQLocalDataSource(
    private val faqQueries: FAQQueries
) {
    fun getAllFAQs(): Flow<List<FAQ>> {
        return faqQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    fun getFAQsByCategory(category: FAQCategory): Flow<List<FAQ>> {
        return faqQueries.selectByCategory(category.name)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun searchFAQs(query: String): List<FAQ> = withContext(Dispatchers.Default) {
        faqQueries.searchByKeywords(query, query, query)
            .executeAsList()
            .map { it.toDomain() }
    }

    suspend fun getFAQById(id: Long): FAQ? = withContext(Dispatchers.Default) {
        faqQueries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun insertFAQ(faq: FAQ): Long = withContext(Dispatchers.Default) {
        faqQueries.insert(
            question = faq.question,
            answer = faq.answer,
            category = faq.category.name,
            keywords = faq.keywords.joinToString(","),
            created_at = faq.createdAt.toEpochMilliseconds()
        )
        faqQueries.lastInsertRowId().executeAsOne()
    }

    suspend fun incrementViewCount(id: Long) = withContext(Dispatchers.Default) {
        faqQueries.incrementViewCount(id)
    }

    suspend fun incrementHelpfulCount(id: Long) = withContext(Dispatchers.Default) {
        faqQueries.incrementHelpfulCount(id)
    }

    suspend fun deleteFAQ(id: Long) = withContext(Dispatchers.Default) {
        faqQueries.deleteById(id)
    }

    private fun FAQEntity.toDomain(): FAQ {
        return FAQ(
            id = id,
            question = question,
            answer = answer,
            category = FAQCategory.fromString(category),
            keywords = keywords.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            viewCount = view_count.toInt(),
            helpfulCount = helpful_count.toInt(),
            createdAt = Instant.fromEpochMilliseconds(created_at)
        )
    }
}

