package ru.macdroid.subagentstest.features.teamAssistant.domain.model

/**
 * Domain model representing the Team Assistant's response to a user message.
 * Contains the formatted message, related data, and metadata about the response.
 */
data class AssistantResponse(
    /**
     * Formatted response message to display to the user
     */
    val message: String,

    /**
     * Related tasks to display with the response (if applicable)
     */
    val tasks: List<ProjectTask> = emptyList(),

    /**
     * Task statistics (if requested)
     */
    val statistics: TaskStatistics? = null,

    /**
     * Confidence level of the AI's understanding (0.0 - 1.0)
     * < 0.5 = low confidence (unknown intent)
     * 0.5-0.8 = medium confidence
     * > 0.8 = high confidence
     */
    val confidence: Float,

    /**
     * Indicates whether an action was performed (task created, status updated, etc.)
     */
    val actionPerformed: Boolean = false
)
