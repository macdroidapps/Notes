package ru.macdroid.subagentstest.features.support.domain.ai

import ru.macdroid.subagentstest.features.support.domain.model.AIAnswer
import ru.macdroid.subagentstest.features.support.domain.model.FAQ
import ru.macdroid.subagentstest.features.support.domain.model.SupportTicket
import ru.macdroid.subagentstest.features.support.domain.repository.FAQRepository

/**
 * AI Assistant for support system
 * Implements RAG (Retrieval-Augmented Generation) pattern
 */
class SupportAIAssistant(
    private val faqRepository: FAQRepository
) {
    /**
     * Get AI-powered answer for user question
     */
    suspend fun getAnswer(
        question: String,
        ticket: SupportTicket? = null
    ): Result<AIAnswer> {
        return runCatching {
            println("🤖 [SupportAI] Processing question: '$question'")

            // 1. Поиск релевантных FAQ (Retrieval phase)
            val relevantFAQs = searchRelevantFAQ(question)
            println("📚 [SupportAI] Found ${relevantFAQs.size} relevant FAQs")

            // 2. Формирование ответа на основе найденных FAQ (Generation phase)
            val answer = if (relevantFAQs.isNotEmpty()) {
                println("✅ [SupportAI] Generating answer from FAQs")
                generateAnswerFromFAQs(question, relevantFAQs, ticket)
            } else {
                println("⚠️ [SupportAI] No FAQs found, generating fallback answer")
                generateFallbackAnswer(question, ticket)
            }

            // 3. Вычисление уверенности ответа
            val confidence = calculateConfidence(question, relevantFAQs)
            println("📊 [SupportAI] Confidence: $confidence")

            AIAnswer(
                answer = answer,
                relatedFAQs = relevantFAQs.take(3), // Топ-3 релевантных FAQ
                confidence = confidence
            )
        }
    }

    /**
     * Search for relevant FAQs using keyword matching and ranking
     */
    private suspend fun searchRelevantFAQ(query: String): List<FAQ> {
        println("🔍 [SupportAI] Searching FAQ for query: '$query'")

        val searchResults = faqRepository.searchFAQs(query).getOrNull() ?: emptyList()
        println("🔍 [SupportAI] Found ${searchResults.size} FAQs from search")

        // Ранжирование по релевантности
        val ranked = searchResults.sortedByDescending { faq ->
            val score = calculateRelevanceScore(query, faq)
            println("📊 [SupportAI] FAQ: '${faq.question.take(50)}...' Score: $score")
            score
        }.take(5)

        println("✅ [SupportAI] Returning top ${ranked.size} relevant FAQs")
        return ranked
    }

    /**
     * Calculate relevance score using TF-IDF-like approach
     */
    private fun calculateRelevanceScore(query: String, faq: FAQ): Double {
        val queryTokens = tokenize(query)
        val faqTokens = tokenize(faq.question + " " + faq.answer)

        // Количество совпадающих токенов
        val matchingTokens = queryTokens.intersect(faqTokens.toSet()).size

        // Базовый скор на основе совпадений
        var score = matchingTokens.toDouble()

        // Бонус за популярность (view_count, helpful_count)
        score += faq.viewCount * 0.1
        score += faq.helpfulCount * 0.5

        // Бонус если совпадение в вопросе (важнее чем в ответе)
        val questionTokens = tokenize(faq.question)
        val questionMatches = queryTokens.intersect(questionTokens.toSet()).size
        score += questionMatches * 2.0

        return score
    }

    /**
     * Generate answer based on found FAQs
     */
    private fun generateAnswerFromFAQs(
        question: String,
        faqs: List<FAQ>,
        ticket: SupportTicket?
    ): String {
        val topFAQ = faqs.first()

        val contextInfo = ticket?.let {
            "\n\n📋 Контекст вашего тикета:\n" +
            "Категория: ${it.category.displayName}\n" +
            "Статус: ${it.status.displayName}"
        } ?: ""

        return buildString {
            appendLine("✅ Нашёл ответ в базе знаний:")
            appendLine()
            appendLine(topFAQ.answer)

            if (faqs.size > 1) {
                appendLine()
                appendLine("📚 Также может быть полезно:")
                faqs.drop(1).take(2).forEachIndexed { index, faq ->
                    appendLine("${index + 2}. ${faq.question}")
                }
            }

            append(contextInfo)

            appendLine()
            appendLine()
            appendLine("💡 Помог ли вам этот ответ? Пожалуйста, отметьте полезность.")
        }
    }

    /**
     * Generate fallback answer when no relevant FAQs found
     */
    private fun generateFallbackAnswer(question: String, ticket: SupportTicket?): String {
        val categoryHint = ticket?.category?.let {
            "\n\nВаш вопрос относится к категории: ${it.displayName}"
        } ?: ""

        return buildString {
            appendLine("🤔 К сожалению, я не нашёл точного ответа в базе знаний.")
            appendLine()
            appendLine("Но вот что я могу предложить:")
            appendLine("• Попробуйте переформулировать вопрос")
            appendLine("• Опишите проблему более детально")
            appendLine("• Укажите, когда проблема возникла и какие шаги вы уже предприняли")
            append(categoryHint)
            appendLine()
            appendLine()
            appendLine("📞 Ваш тикет передан специалистам поддержки, они ответят в ближайшее время.")
        }
    }

    /**
     * Calculate confidence level of the answer
     */
    private fun calculateConfidence(query: String, relevantFAQs: List<FAQ>): Float {
        if (relevantFAQs.isEmpty()) return 0f

        val topScore = calculateRelevanceScore(query, relevantFAQs.first())
        val queryTokenCount = tokenize(query).size

        // Нормализация скора в диапазон 0-1
        val normalizedScore = (topScore / (queryTokenCount * 3.0)).coerceIn(0.0, 1.0)

        return normalizedScore.toFloat()
    }

    /**
     * Simple tokenization (можно улучшить стеммингом/лемматизацией)
     */
    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^а-яёa-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 } // Убираем короткие слова
            .distinct()
    }
}

