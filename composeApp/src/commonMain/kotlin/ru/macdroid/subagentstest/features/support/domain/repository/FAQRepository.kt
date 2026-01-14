package ru.macdroid.subagentstest.features.support.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.support.domain.model.FAQ
import ru.macdroid.subagentstest.features.support.domain.model.FAQCategory

/**
 * Repository interface for FAQ operations
 */
interface FAQRepository {
    fun getAllFAQs(): Flow<List<FAQ>>
    fun getFAQsByCategory(category: FAQCategory): Flow<List<FAQ>>
    suspend fun searchFAQs(query: String): Result<List<FAQ>>
    suspend fun getFAQById(id: Long): Result<FAQ?>
    suspend fun insertFAQ(faq: FAQ): Result<Long>
    suspend fun incrementViewCount(id: Long): Result<Unit>
    suspend fun incrementHelpfulCount(id: Long): Result<Unit>
    suspend fun deleteFAQ(id: Long): Result<Unit>
}

