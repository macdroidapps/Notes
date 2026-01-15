package ru.macdroid.subagentstest.features.teamAssistant.domain.ai

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.macdroid.subagentstest.features.teamAssistant.domain.model.*
import ru.macdroid.subagentstest.features.teamAssistant.domain.repository.TeamAssistantRepository
import kotlin.time.Duration.Companion.days

/**
 * Team Assistant - RAG-based AI engine for project task management.
 * Processes user messages, recognizes intents, and generates contextual responses.
 */
class TeamAssistant(
    private val repository: TeamAssistantRepository,
    private val intentRecognizer: IntentRecognizer,
    private val queryParser: QueryParser
) {
    /**
     * Main entry point for processing user messages.
     * Returns a Result with AssistantResponse containing the formatted message and related data.
     */
    suspend fun processMessage(message: String): Result<AssistantResponse> {
        return runCatching {
            println("🤖 [TeamAssistant] Processing: '$message'")

            // Step 1: Recognize intent
            val intent = intentRecognizer.recognize(message)
            println("🎯 [TeamAssistant] Intent: ${intent::class.simpleName}")

            // Step 2: Process based on intent
            when (intent) {
                is AssistantIntent.QueryTasks -> handleQueryTasks(intent.filter)
                is AssistantIntent.QueryStatistics -> handleQueryStatistics()
                is AssistantIntent.QueryOverdue -> handleQueryOverdue()
                is AssistantIntent.CreateTask -> handleCreateTask(intent)
                is AssistantIntent.UpdateTaskStatus -> handleUpdateStatus(intent)
                is AssistantIntent.GetRecommendations -> handleGetRecommendations()
                is AssistantIntent.Unknown -> handleUnknown(message)
            }
        }
    }

    private suspend fun handleQueryTasks(filter: TaskFilter): AssistantResponse {
        val tasks = when {
            filter.overdue -> repository.getOverdueTasks(Clock.System.now()).first()
            filter.status != null -> repository.getTasksByStatus(filter.status).first()
            filter.priority != null -> repository.getTasksByPriority(filter.priority).first()
            else -> repository.getAllTasks().first()
        }

        val message = buildString {
            when {
                filter.overdue -> appendLine("⏰ Просроченные задачи:")
                filter.status != null -> appendLine("📋 Задачи со статусом '${filter.status.displayName}':")
                filter.priority != null -> appendLine("${filter.priority.emoji} Задачи с приоритетом '${filter.priority.displayName}':")
                else -> appendLine("📋 Все задачи:")
            }
            appendLine()

            if (tasks.isEmpty()) {
                append("✅ Задачи не найдены!")
            } else {
                append("Найдено задач: ${tasks.size}")
                appendLine()
                appendLine()
                tasks.take(10).forEach { task ->
                    append("${task.priority.emoji} ")
                    append("[${task.status.displayName}] ")
                    appendLine(task.title)
                    if (task.dueDate != null) {
                        appendLine("  Срок: ${formatDate(task.dueDate)}")
                    }
                    appendLine()
                }

                if (tasks.size > 10) {
                    appendLine("... и ещё ${tasks.size - 10}")
                }
            }
        }

        return AssistantResponse(
            message = message,
            tasks = tasks.take(10),
            confidence = 0.95f
        )
    }

    private suspend fun handleQueryStatistics(): AssistantResponse {
        val stats = repository.getStatistics().getOrThrow()

        val message = buildString {
            appendLine("📊 Статистика по задачам:")
            appendLine()
            appendLine("Всего задач: ${stats.totalTasks}")
            appendLine()
            appendLine("По статусам:")
            appendLine("  📝 To Do: ${stats.todoCount}")
            appendLine("  ⚡ В работе: ${stats.inProgressCount}")
            appendLine("  ✅ Выполнено: ${stats.doneCount}")
            appendLine()
            appendLine("По приоритетам (незавершённые):")
            appendLine("  🔴 Критические: ${stats.criticalCount}")
            appendLine("  🟠 Высокие: ${stats.highCount}")
            appendLine("  🟡 Средние: ${stats.mediumCount}")
            appendLine("  🟢 Низкие: ${stats.lowCount}")

            if (stats.overdueCount > 0) {
                appendLine()
                appendLine("⚠️ Просроченных задач: ${stats.overdueCount}")
            }
        }

        return AssistantResponse(
            message = message,
            statistics = stats,
            confidence = 1.0f
        )
    }

    private suspend fun handleQueryOverdue(): AssistantResponse {
        val overdueTasks = repository.getOverdueTasks(Clock.System.now()).first()

        val message = buildString {
            appendLine("⏰ Просроченные задачи:")
            appendLine()

            if (overdueTasks.isEmpty()) {
                append("🎉 Отлично! Нет просроченных задач.")
            } else {
                appendLine("Найдено: ${overdueTasks.size}")
                appendLine()
                overdueTasks.take(5).forEach { task ->
                    append("${task.priority.emoji} ${task.title}")
                    appendLine()
                    task.dueDate?.let { dueDate ->
                        val overdueDays = calculateOverdueDays(dueDate)
                        appendLine("  Просрочено на: $overdueDays дн.")
                    }
                    appendLine()
                }

                if (overdueTasks.size > 5) {
                    appendLine("... и ещё ${overdueTasks.size - 5}")
                }
            }
        }

        return AssistantResponse(
            message = message,
            tasks = overdueTasks.take(5),
            confidence = 1.0f
        )
    }

    private suspend fun handleCreateTask(intent: AssistantIntent.CreateTask): AssistantResponse {
        val result = repository.createTask(
            title = intent.title,
            description = intent.description ?: "",
            status = TaskStatus.TODO,
            priority = intent.priority,
            dueDate = intent.dueDate
        )

        return if (result.isSuccess) {
            val message = buildString {
                appendLine("✅ Задача успешно создана!")
                appendLine()
                appendLine("${intent.priority.emoji} ${intent.title}")
                appendLine("Приоритет: ${intent.priority.displayName}")
                if (intent.dueDate != null) {
                    appendLine("Срок: ${formatDate(intent.dueDate)}")
                }
            }

            AssistantResponse(
                message = message,
                confidence = 0.95f,
                actionPerformed = true
            )
        } else {
            AssistantResponse(
                message = "❌ Не удалось создать задачу: ${result.exceptionOrNull()?.message}",
                confidence = 0.5f,
                actionPerformed = false
            )
        }
    }

    private suspend fun handleUpdateStatus(intent: AssistantIntent.UpdateTaskStatus): AssistantResponse {
        val completedAt = if (intent.newStatus == TaskStatus.DONE) Clock.System.now() else null
        val result = repository.updateTaskStatus(intent.taskId, intent.newStatus, completedAt)

        return if (result.isSuccess) {
            AssistantResponse(
                message = "✅ Статус задачи обновлён на '${intent.newStatus.displayName}'",
                confidence = 0.95f,
                actionPerformed = true
            )
        } else {
            AssistantResponse(
                message = "❌ Не удалось обновить статус задачи",
                confidence = 0.5f,
                actionPerformed = false
            )
        }
    }

    private suspend fun handleGetRecommendations(): AssistantResponse {
        val overdueTasks = repository.getOverdueTasks(Clock.System.now()).first()
        val allTasks = repository.getAllTasks().first()

        val recommendations = when {
            overdueTasks.isNotEmpty() -> {
                // Recommend overdue tasks first
                overdueTasks.sortedBy { it.priority.weight }.take(3)
            }
            else -> {
                // Recommend by priority and due date
                allTasks
                    .filter { it.status != TaskStatus.DONE }
                    .sortedWith(
                        compareBy<ProjectTask> { it.priority.weight }
                            .thenBy { it.dueDate ?: Instant.DISTANT_FUTURE }
                    )
                    .take(3)
            }
        }

        val message = buildString {
            appendLine("💡 Рекомендации: что делать дальше?")
            appendLine()

            if (recommendations.isEmpty()) {
                append("🎉 Отличная работа! Все задачи выполнены.")
            } else {
                appendLine("Предлагаю начать с этих задач:")
                appendLine()
                recommendations.forEachIndexed { index, task ->
                    append("${index + 1}. ${task.priority.emoji} ${task.title}")
                    appendLine()
                    when {
                        task.dueDate != null && task.dueDate < Clock.System.now() -> {
                            appendLine("   ⏰ Просрочено!")
                        }
                        task.priority in listOf(TaskPriority.CRITICAL, TaskPriority.HIGH) -> {
                            appendLine("   ⚠️ Высокий приоритет")
                        }
                    }
                    appendLine()
                }
            }
        }

        return AssistantResponse(
            message = message,
            tasks = recommendations,
            confidence = 0.9f
        )
    }

    private fun handleUnknown(message: String): AssistantResponse {
        return AssistantResponse(
            message = buildString {
                appendLine("🤔 Не совсем понял ваш запрос.")
                appendLine()
                appendLine("Попробуйте:")
                appendLine("• \"Покажи все задачи\"")
                appendLine("• \"Сколько задач в работе?\"")
                appendLine("• \"Создай задачу: Написать отчёт, высокий приоритет\"")
                appendLine("• \"Что мне делать дальше?\"")
                appendLine("• \"Покажи просроченные задачи\"")
            },
            confidence = 0.3f
        )
    }

    private fun formatDate(instant: Instant): String {
        // Simple formatting - показываем дату в ISO формате
        return instant.toString().substringBefore('T')
    }

    private fun calculateOverdueDays(dueDate: Instant): Long {
        val now = Clock.System.now()
        return ((now - dueDate).inWholeDays)
    }
}
