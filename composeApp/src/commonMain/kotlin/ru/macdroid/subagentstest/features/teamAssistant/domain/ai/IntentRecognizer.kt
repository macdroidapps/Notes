package ru.macdroid.subagentstest.features.teamAssistant.domain.ai

import ru.macdroid.subagentstest.features.teamAssistant.domain.model.*

/**
 * Intent recognizer for the Team Assistant.
 * Analyzes user messages and classifies them into actionable intents using pattern matching.
 */
class IntentRecognizer {
    /**
     * Recognize the intent from a user message.
     * Returns the most appropriate AssistantIntent based on keyword patterns.
     */
    fun recognize(message: String): AssistantIntent {
        val lowerMessage = message.lowercase().trim()

        return when {
            // Create task intent (highest priority to avoid conflicts)
            isCreateIntent(lowerMessage) -> parseCreateIntent(message)

            // Query statistics
            isStatisticsIntent(lowerMessage) -> AssistantIntent.QueryStatistics

            // Query overdue tasks
            isOverdueIntent(lowerMessage) -> AssistantIntent.QueryOverdue

            // Get recommendations
            isRecommendationIntent(lowerMessage) -> AssistantIntent.GetRecommendations

            // Query tasks by status
            isStatusQueryIntent(lowerMessage) -> {
                val status = extractStatus(lowerMessage)
                AssistantIntent.QueryTasks(TaskFilter(status = status))
            }

            // Query tasks by priority
            isPriorityQueryIntent(lowerMessage) -> {
                val priority = extractPriority(lowerMessage)
                AssistantIntent.QueryTasks(TaskFilter(priority = priority))
            }

            // General task query
            isTaskQueryIntent(lowerMessage) -> AssistantIntent.QueryTasks(TaskFilter())

            // Unknown intent
            else -> AssistantIntent.Unknown
        }
    }

    private fun isCreateIntent(message: String): Boolean {
        return message.contains(Regex("созда(й|ть|ние)|добав|нов(ая|ую) задач"))
    }

    private fun isStatisticsIntent(message: String): Boolean {
        return message.contains(Regex("статистик|сколько|количество|итог"))
    }

    private fun isOverdueIntent(message: String): Boolean {
        return message.contains(Regex("просрочен|опазды|дедлайн прошел"))
    }

    private fun isRecommendationIntent(message: String): Boolean {
        return message.contains(Regex("что (мне )?дел|рекоменд|предлож|с чего начать|что делать"))
    }

    private fun isStatusQueryIntent(message: String): Boolean {
        return message.contains(Regex("задач(и|).*?(todo|в работе|выполнен|готов)"))
    }

    private fun isPriorityQueryIntent(message: String): Boolean {
        return message.contains(Regex("задач(и|).*?(высок|критич|средн|низк).*?приоритет"))
    }

    private fun isTaskQueryIntent(message: String): Boolean {
        return message.contains(Regex("покаж|список|все задач|задач(и|а)"))
    }

    private fun parseCreateIntent(message: String): AssistantIntent {
        val queryParser = QueryParser()
        return queryParser.parseCreateCommand(message)
    }

    private fun extractStatus(message: String): TaskStatus? {
        return when {
            message.contains("todo") || message.contains("сделать") -> TaskStatus.TODO
            message.contains("в работе") || message.contains("прогресс") -> TaskStatus.IN_PROGRESS
            message.contains("выполнен") || message.contains("готов") -> TaskStatus.DONE
            else -> null
        }
    }

    private fun extractPriority(message: String): TaskPriority? {
        return when {
            message.contains("критич") -> TaskPriority.CRITICAL
            message.contains("высок") -> TaskPriority.HIGH
            message.contains("средн") -> TaskPriority.MEDIUM
            message.contains("низк") -> TaskPriority.LOW
            else -> null
        }
    }
}
