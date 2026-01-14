package ru.macdroid.subagentstest.features.support.data.repository

import kotlinx.coroutines.flow.Flow
import ru.macdroid.subagentstest.features.support.data.local.FAQLocalDataSource
import ru.macdroid.subagentstest.features.support.domain.model.FAQ
import ru.macdroid.subagentstest.features.support.domain.model.FAQCategory
import ru.macdroid.subagentstest.features.support.domain.repository.FAQRepository

/**
 * Implementation of FAQ Repository
 */
class FAQRepositoryImpl(
    private val localDataSource: FAQLocalDataSource
) : FAQRepository {

    override fun getAllFAQs(): Flow<List<FAQ>> {
        return localDataSource.getAllFAQs()
    }

    override fun getFAQsByCategory(category: FAQCategory): Flow<List<FAQ>> {
        return localDataSource.getFAQsByCategory(category)
    }

    override suspend fun searchFAQs(query: String): Result<List<FAQ>> {
        return runCatching {
            localDataSource.searchFAQs(query)
        }
    }

    override suspend fun getFAQById(id: Long): Result<FAQ?> {
        return runCatching {
            localDataSource.getFAQById(id)
        }
    }

    override suspend fun insertFAQ(faq: FAQ): Result<Long> {
        return runCatching {
            localDataSource.insertFAQ(faq)
        }
    }

    override suspend fun incrementViewCount(id: Long): Result<Unit> {
        return runCatching {
            localDataSource.incrementViewCount(id)
        }
    }

    override suspend fun incrementHelpfulCount(id: Long): Result<Unit> {
        return runCatching {
            localDataSource.incrementHelpfulCount(id)
        }
    }

    override suspend fun deleteFAQ(id: Long): Result<Unit> {
        return runCatching {
            localDataSource.deleteFAQ(id)
        }
    }
}

