package ru.macdroid.subagentstest.features.support.domain.usecase

import ru.macdroid.subagentstest.features.support.domain.repository.FAQRepository

/**
 * Use case for initializing FAQ database with initial data
 */
class InitializeFAQUseCase(
    private val faqRepository: FAQRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            // Проверяем, есть ли уже данные
            val existingFAQs = faqRepository.searchFAQs("").getOrNull() ?: emptyList()

            // Добавляем начальные FAQ только если база пустая
            if (existingFAQs.isEmpty()) {
                InitialFAQData.faqs.forEach { faq ->
                    faqRepository.insertFAQ(faq)
                }
            }
        }
    }
}

