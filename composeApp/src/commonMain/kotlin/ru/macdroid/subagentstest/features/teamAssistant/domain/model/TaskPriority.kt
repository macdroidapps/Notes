package ru.macdroid.subagentstest.features.teamAssistant.domain.model

/**
 * Task priority enum with visual representation and weight for sorting.
 * Weight: lower number = higher priority (CRITICAL=1, LOW=4)
 */
enum class TaskPriority(
    val displayName: String,
    val emoji: String,
    val weight: Int
) {
    LOW("Low", "🟢", 4),
    MEDIUM("Medium", "🟡", 3),
    HIGH("High", "🟠", 2),
    CRITICAL("Critical", "🔴", 1);

    companion object {
        /**
         * Convert string value to TaskPriority enum.
         * Returns MEDIUM as default if value is not recognized.
         */
        fun fromString(value: String): TaskPriority {
            return entries.find { it.name == value } ?: MEDIUM
        }
    }
}
