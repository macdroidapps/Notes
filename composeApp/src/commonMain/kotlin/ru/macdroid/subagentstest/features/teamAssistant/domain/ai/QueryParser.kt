package ru.macdroid.subagentstest.features.teamAssistant.domain.ai

import ru.macdroid.subagentstest.features.teamAssistant.domain.model.AssistantIntent
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.TaskPriority

/**
 * Parser for extracting structured data from user messages.
 * Specifically handles parsing of create task commands.
 */
class QueryParser {
    /**
     * Parse a create task command from a user message.
     * Example: "Создай задачу: Написать отчёт, высокий приоритет"
     * Returns AssistantIntent.CreateTask with extracted title and priority.
     */
    fun parseCreateCommand(message: String): AssistantIntent {
        // Pattern to extract task title
        // Matches: "созда*/добав* задач*: <title>" or "новая задача: <title>"
        val titlePattern = Regex(
            "(?:созда[йть]* задач[уа]?:?|добав[ьи]* задач[уа]?:?|нов[ауюой]* задач[уа]?:?)\\s*(.+?)(?:,|$)",
            RegexOption.IGNORE_CASE
        )

        // Pattern to extract priority
        val priorityPattern = Regex(
            "(критич|высок|средн|низк)\\w*\\s*приоритет",
            RegexOption.IGNORE_CASE
        )

        val titleMatch = titlePattern.find(message)
        val priorityMatch = priorityPattern.find(message)

        // Extract title (required)
        val title = titleMatch?.groupValues?.get(1)?.trim() ?: "Новая задача"

        // Extract priority (optional, default to MEDIUM)
        val priority = when {
            priorityMatch?.value?.contains("критич", ignoreCase = true) == true -> TaskPriority.CRITICAL
            priorityMatch?.value?.contains("высок", ignoreCase = true) == true -> TaskPriority.HIGH
            priorityMatch?.value?.contains("средн", ignoreCase = true) == true -> TaskPriority.MEDIUM
            priorityMatch?.value?.contains("низк", ignoreCase = true) == true -> TaskPriority.LOW
            else -> TaskPriority.MEDIUM
        }

        return AssistantIntent.CreateTask(
            title = title,
            description = null,
            priority = priority,
            dueDate = null
        )
    }
}
