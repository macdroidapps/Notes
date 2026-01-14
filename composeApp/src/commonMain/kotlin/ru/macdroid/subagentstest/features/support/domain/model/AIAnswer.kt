package ru.macdroid.subagentstest.features.support.domain.model

/**
 * AI Assistant response
 */
data class AIAnswer(
    val answer: String,
    val relatedFAQs: List<FAQ>,
    val confidence: Float = 0f
)

