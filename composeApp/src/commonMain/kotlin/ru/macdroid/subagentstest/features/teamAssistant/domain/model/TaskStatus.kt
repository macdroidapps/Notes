package ru.macdroid.subagentstest.features.teamAssistant.domain.model

/**
 * Task status enum representing the current state of a project task.
 */
enum class TaskStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done");

    companion object {
        /**
         * Convert string value to TaskStatus enum.
         * Returns TODO if value is not recognized.
         */
        fun fromString(value: String): TaskStatus {
            return entries.find { it.name == value } ?: TODO
        }
    }
}
