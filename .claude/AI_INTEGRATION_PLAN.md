# 🤖 План Интеграции AI-Ассистента с RAG и DeepSeek

## 📋 Текущее Состояние

### ✅ Что уже реализовано

1. **Документация проекта** (9 MD файлов):
   - `README.md` - основная информация
   - `ARCHITECTURE.md` - архитектура проекта
   - `PROJECT_STATUS.md` - текущее состояние
   - `QUICKSTART.md` - быстрый старт
   - `AI_HELP_SYSTEM.md` - система помощи
   - `AI_HELP_CHEATSHEET.md` - шпаргалка
   - `DOCS_NAVIGATION.md` - навигация по документам
   - `INDEX.md` - индекс документации
   - `kmp-prompt.md` - промпт для AI

2. **Статическая система /help** - работает через внешние AI-ассистенты (Copilot, ChatGPT)

3. **Clean Architecture** - готовая структура для добавления новой фичи

---

## 🎯 Задание (исходное)

> **Используйте RAG для подключения к документации вашего проекта (README, API, схемы данных)**
> - Через MCP подключите ассистента к текущему git-репозиторию (например, чтобы он понимал текущую ветку или открытые файлы)
> - Настройте команду /help, которая отвечает на вопросы о проекте, подсказывая фрагменты кода или правила стиля

---

## 🚀 План Реализации

### Этап 1: RAG Система для Документации

#### 1.1 Выбор подхода

**Вариант А: Локальная Embeddings Модель** (рекомендуется для KMP)
- ✅ Работает оффлайн
- ✅ Полная кроссплатформенность
- ✅ Приватность данных
- ❌ Требует больше ресурсов

**Библиотека:** 
```kotlin
// Используем LangChain4j Kotlin или собственную реализацию
implementation("dev.langchain4j:langchain4j:0.35.0")
implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:0.35.0")
```

**Вариант Б: API Embeddings** (проще для MVP)
- ✅ Меньше кода
- ✅ Быстрее реализовать
- ❌ Требует интернет
- ❌ Зависимость от API

**API Options:**
- OpenAI Embeddings API
- Cohere Embeddings API
- DeepSeek Embeddings (если доступно)

#### 1.2 Архитектура RAG модуля

```
features/ai-assistant/
├── data/
│   ├── local/
│   │   ├── DocumentationDataSource.kt      # Чтение MD файлов
│   │   ├── VectorStore.kt                  # Хранение векторов
│   │   └── EmbeddingsCache.kt              # Кэш эмбеддингов
│   ├── remote/
│   │   ├── DeepSeekApiService.kt           # API клиент DeepSeek
│   │   └── EmbeddingsService.kt            # Embeddings API
│   └── repository/
│       ├── DocumentationRepositoryImpl.kt
│       └── ChatRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── DocumentChunk.kt                # Часть документа
│   │   ├── ChatMessage.kt                  # Сообщение чата
│   │   ├── SearchResult.kt                 # Результат поиска
│   │   └── GitContext.kt                   # Git контекст (MCP)
│   ├── repository/
│   │   ├── DocumentationRepository.kt
│   │   └── ChatRepository.kt
│   └── usecase/
│       ├── IndexDocumentationUseCase.kt    # Индексация документов
│       ├── SearchDocumentationUseCase.kt   # Поиск по документам
│       ├── SendMessageUseCase.kt           # Отправка сообщения
│       └── GetGitContextUseCase.kt         # Получение Git контекста
└── presentation/
    ├── chat/
    │   ├── AIChatScreen.kt                 # Экран чата
    │   ├── AIChatViewModel.kt
    │   └── components/
    │       ├── MessageBubble.kt
    │       ├── CodeBlock.kt
    │       └── DocumentPreview.kt
    └── settings/
        └── AISettingsScreen.kt             # Настройки AI
```

#### 1.3 Процесс RAG

```
1. Индексация (при старте приложения):
   ┌─────────────────┐
   │ MD Files Reader │
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ Text Chunking   │  ← Разбивка на части (512 tokens)
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ Generate        │
   │ Embeddings      │  ← Векторное представление
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ Vector Store    │  ← Сохранение в SQLDelight
   └─────────────────┘

2. Поиск (при запросе пользователя):
   ┌─────────────────┐
   │ User Query      │
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ Generate        │
   │ Query Embedding │
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ Cosine          │
   │ Similarity      │  ← Поиск похожих векторов
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ Top-K Results   │  ← 3-5 наиболее релевантных фрагментов
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ DeepSeek API    │  ← Отправка контекста + вопрос
   └────────┬────────┘
            │
            ↓
   ┌─────────────────┐
   │ AI Response     │
   └─────────────────┘
```

---

### Этап 2: Интеграция DeepSeek API

#### 2.1 API Client

```kotlin
// commonMain
interface DeepSeekApi {
    suspend fun chat(
        messages: List<ChatMessage>,
        context: String,
        temperature: Float = 0.7f
    ): ChatResponse
}

// DeepSeek API Documentation: https://platform.deepseek.com/api-docs/
class DeepSeekApiImpl(
    private val httpClient: HttpClient,
    private val apiKey: String
) : DeepSeekApi {
    
    private val baseUrl = "https://api.deepseek.com/v1"
    
    override suspend fun chat(
        messages: List<ChatMessage>,
        context: String,
        temperature: Float
    ): ChatResponse {
        return httpClient.post("$baseUrl/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(
                model = "deepseek-chat",
                messages = buildList {
                    add(ChatMessage(
                        role = "system",
                        content = """
                            Ты AI-ассистент для проекта SubAgentsTest.
                            
                            КОНТЕКСТ ИЗ ДОКУМЕНТАЦИИ:
                            $context
                            
                            Отвечай на основе предоставленного контекста.
                            Если информации нет в контексте, скажи об этом.
                        """.trimIndent()
                    ))
                    addAll(messages)
                },
                temperature = temperature,
                stream = false
            ))
        }.body()
    }
}
```

#### 2.2 Зависимости

```kotlin
// build.gradle.kts (commonMain)
commonMain.dependencies {
    // HTTP Client для API запросов
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    
    // Для работы с векторами (опционально)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

// Platform-specific HTTP clients
androidMain.dependencies {
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
}
iosMain.dependencies {
    implementation("io.ktor:ktor-client-darwin:2.3.7")
}
desktopMain.dependencies {
    implementation("io.ktor:ktor-client-cio:2.3.7")
}
jsMain.dependencies {
    implementation("io.ktor:ktor-client-js:2.3.7")
}
```

---

### Этап 3: MCP (Model Context Protocol)

#### 3.1 Git Интеграция

**Вариант А: JGit (для Android/Desktop)**
```kotlin
// desktopMain / androidMain
class GitContextProvider {
    fun getCurrentBranch(): String {
        val repo = Git.open(File(".git"))
        return repo.repository.branch
    }
    
    fun getRecentCommits(limit: Int = 5): List<CommitInfo> {
        val repo = Git.open(File(".git"))
        return repo.log().setMaxCount(limit).call().map {
            CommitInfo(
                hash = it.name,
                message = it.fullMessage,
                author = it.authorIdent.name
            )
        }
    }
    
    fun getModifiedFiles(): List<String> {
        val repo = Git.open(File(".git"))
        return repo.status().call().modified.toList()
    }
}
```

**Вариант Б: Exec git команды** (кроссплатформенно)
```kotlin
// commonMain (expect/actual)
expect class GitContextProvider() {
    fun getCurrentBranch(): String
    fun getModifiedFiles(): List<String>
}

// desktopMain / androidMain (actual)
actual class GitContextProvider {
    actual fun getCurrentBranch(): String {
        return Runtime.getRuntime()
            .exec("git branch --show-current")
            .inputStream.bufferedReader().readText().trim()
    }
    
    actual fun getModifiedFiles(): List<String> {
        return Runtime.getRuntime()
            .exec("git status --short")
            .inputStream.bufferedReader().readLines()
            .map { it.substring(3) } // Remove status prefix
    }
}
```

#### 3.2 Использование Git Context в промпте

```kotlin
class AIChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val searchDocumentationUseCase: SearchDocumentationUseCase,
    private val getGitContextUseCase: GetGitContextUseCase
) : ViewModel() {
    
    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            // 1. Получаем Git контекст
            val gitContext = getGitContextUseCase()
            
            // 2. Ищем релевантную документацию
            val docs = searchDocumentationUseCase(userMessage)
            
            // 3. Формируем контекст
            val context = buildString {
                appendLine("=== GIT CONTEXT ===")
                appendLine("Branch: ${gitContext.currentBranch}")
                appendLine("Modified files: ${gitContext.modifiedFiles.joinToString()}")
                appendLine()
                appendLine("=== DOCUMENTATION ===")
                docs.forEach { doc ->
                    appendLine("## ${doc.source}")
                    appendLine(doc.content)
                    appendLine()
                }
            }
            
            // 4. Отправляем в DeepSeek
            val response = sendMessageUseCase(
                message = userMessage,
                context = context
            )
            
            // 5. Обновляем UI
            _messages.value += ChatMessage(
                role = "assistant",
                content = response.text,
                sources = docs.map { it.source }
            )
        }
    }
}
```

---

### Этап 4: SQLDelight Schema для RAG

```sql
-- database/VectorStore.sq

CREATE TABLE DocumentChunk (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    source TEXT NOT NULL,                -- Имя файла (README.md)
    content TEXT NOT NULL,               -- Текст фрагмента
    embedding BLOB NOT NULL,             -- Векторное представление (serialized)
    chunk_index INTEGER NOT NULL,        -- Порядковый номер части
    created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
);

CREATE INDEX idx_document_source ON DocumentChunk(source);

-- Queries
selectAll:
SELECT * FROM DocumentChunk;

selectBySource:
SELECT * FROM DocumentChunk WHERE source = ?;

insertChunk:
INSERT INTO DocumentChunk(source, content, embedding, chunk_index)
VALUES (?, ?, ?, ?);

deleteBySource:
DELETE FROM DocumentChunk WHERE source = ?;

deleteAll:
DELETE FROM DocumentChunk;

-- Для поиска по косинусному сходству (в коде)
-- SQLite не поддерживает векторные операции напрямую,
-- поэтому загружаем все эмбеддинги и считаем в памяти
```

---

### Этап 5: UI для AI-Чата

#### 5.1 Chat Screen

```kotlin
@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel = koinViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = { Text("AI Assistant") },
            navigationIcon = {
                IconButton(onClick = { /* back */ }) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
            actions = {
                IconButton(onClick = { /* settings */ }) {
                    Icon(Icons.Default.Settings, null)
                }
            }
        )
        
        // Messages List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
            
            if (isLoading) {
                item {
                    LoadingIndicator()
                }
            }
        }
        
        // Input Field
        MessageInputField(
            onSendMessage = { text ->
                viewModel.sendMessage(text)
            }
        )
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Sources (если есть)
                message.sources?.let { sources ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📚 Sources: ${sources.joinToString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

#### 5.2 Команда /help интеграция

```kotlin
class AIChatViewModel(...) {
    
    private val helpCommands = mapOf(
        "/help" to "Показать все доступные команды",
        "/help architecture" to "Информация об архитектуре проекта",
        "/help koin" to "Dependency Injection с Koin",
        "/help compose" to "Compose Multiplatform best practices",
        "/help database" to "SQLDelight запросы и схемы",
        // ... другие команды из AI_HELP_SYSTEM.md
    )
    
    fun sendMessage(userMessage: String) {
        if (userMessage.startsWith("/help")) {
            handleHelpCommand(userMessage)
        } else {
            handleRegularMessage(userMessage)
        }
    }
    
    private fun handleHelpCommand(command: String) {
        viewModelScope.launch {
            when (command.trim()) {
                "/help" -> {
                    // Показать список всех команд
                    val response = buildString {
                        appendLine("📚 Доступные команды:")
                        helpCommands.forEach { (cmd, desc) ->
                            appendLine("• `$cmd` - $desc")
                        }
                    }
                    addMessage("assistant", response)
                }
                else -> {
                    // Поиск по документации
                    val docs = searchDocumentationUseCase(command)
                    val context = docs.joinToString("\n\n") { it.content }
                    
                    val response = sendMessageUseCase(
                        message = "Объясни: $command",
                        context = context
                    )
                    
                    addMessage("assistant", response.text, docs.map { it.source })
                }
            }
        }
    }
}
```

---

## 📦 Необходимые Библиотеки

### build.gradle.kts (корневой)
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    kotlin("plugin.serialization") version "2.0.21"
}
```

### gradle/libs.versions.toml
```toml
[versions]
ktor = "2.3.7"
langchain4j = "0.35.0"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }

# Опционально: для локальных embeddings
langchain4j-core = { module = "dev.langchain4j:langchain4j", version.ref = "langchain4j" }
langchain4j-embeddings = { module = "dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2", version.ref = "langchain4j" }
```

---

## 🎯 Порядок Реализации

### MVP (Минимальный Функционал)

**Шаг 1: Базовая инфраструктура** (2-3 часа)
- [ ] Создать структуру модуля `features/ai-assistant/`
- [ ] Добавить зависимости (Ktor, Koin modules)
- [ ] Настроить базовый UI экрана чата

**Шаг 2: DeepSeek API Интеграция** (2-3 часа)
- [ ] Создать API клиент для DeepSeek
- [ ] Настроить хранение API ключа
- [ ] Реализовать базовый чат без RAG
- [ ] Протестировать отправку/получение сообщений

**Шаг 3: Простой поиск по документации** (3-4 часа)
- [ ] Реализовать чтение MD файлов
- [ ] Простой текстовый поиск (keyword matching)
- [ ] Отправка найденного контекста в DeepSeek
- [ ] Показ источников в ответе

**Шаг 4: Команда /help** (1-2 часа)
- [ ] Парсинг /help команд
- [ ] Маппинг команд на разделы документации
- [ ] Интеграция с поиском

---

### Full Version (Полный Функционал)

**Шаг 5: RAG с Embeddings** (4-6 часов)
- [ ] Выбрать embeddings модель/API
- [ ] Реализовать индексацию документов
- [ ] Создать векторную БД в SQLDelight
- [ ] Реализовать семантический поиск
- [ ] Косинусное сходство для ранжирования

**Шаг 6: MCP - Git Integration** (2-3 часа)
- [ ] Создать GitContextProvider (expect/actual)
- [ ] Получение текущей ветки
- [ ] Список измененных файлов
- [ ] Интеграция в промпт для DeepSeek

**Шаг 7: Улучшения UI/UX** (3-4 часа)
- [ ] Syntax highlighting для кода в ответах
- [ ] Кликабельные источники документации
- [ ] История чата (сохранение в БД)
- [ ] Настройки AI (температура, модель)
- [ ] Экспорт диалогов

**Шаг 8: Оптимизация** (2-3 часа)
- [ ] Кэширование embeddings
- [ ] Пагинация истории чата
- [ ] Обработка длинных ответов
- [ ] Retry логика для API

---

## 🔐 Конфигурация API Ключа

### local.properties (не коммитить!)
```properties
deepseek.api.key=sk-xxxxxxxxxxxxxxxx
```

### build.gradle.kts
```kotlin
android {
    defaultConfig {
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        
        buildConfigField(
            "String",
            "DEEPSEEK_API_KEY",
            "\"${properties.getProperty("deepseek.api.key", "")}\""
        )
    }
}
```

---

## 📊 Оценка Времени

| Задача | MVP | Full | Сложность |
|--------|-----|------|-----------|
| Инфраструктура | 2-3ч | 2-3ч | 🟢 Легко |
| DeepSeek API | 2-3ч | 2-3ч | 🟢 Легко |
| Простой поиск | 3-4ч | - | 🟡 Средне |
| /help команды | 1-2ч | 1-2ч | 🟢 Легко |
| RAG + Embeddings | - | 4-6ч | 🔴 Сложно |
| Git Integration | - | 2-3ч | 🟡 Средне |
| UI/UX | 1ч | 3-4ч | 🟡 Средне |
| Оптимизация | - | 2-3ч | 🟡 Средне |
| **ИТОГО** | **9-13ч** | **17-24ч** | |

---

## 🧪 Тестирование

### Unit Tests
```kotlin
class SearchDocumentationUseCaseTest {
    @Test
    fun `should find relevant documentation`() = runTest {
        // Arrange
        val repository = FakeDocumentationRepository()
        val useCase = SearchDocumentationUseCase(repository)
        
        // Act
        val results = useCase("Clean Architecture")
        
        // Assert
        assertTrue(results.isNotEmpty())
        assertTrue(results.first().content.contains("Clean Architecture"))
    }
}
```

### Integration Tests
```kotlin
class DeepSeekApiTest {
    @Test
    fun `should get response from DeepSeek`() = runTest {
        val api = DeepSeekApiImpl(httpClient, apiKey)
        val response = api.chat(
            messages = listOf(ChatMessage("user", "Hello")),
            context = ""
        )
        assertNotNull(response.text)
    }
}
```

---

## 📚 Дополнительные Ресурсы

### DeepSeek API
- [Документация](https://platform.deepseek.com/api-docs/)
- [Pricing](https://platform.deepseek.com/pricing)

### RAG Implementation
- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [Kotlin Vector Search](https://github.com/JetBrains/kotlin-ai-toolkit)

### MCP (Model Context Protocol)
- [MCP Specification](https://github.com/anthropics/model-context-protocol)
- [MCP Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk)

---

## 🤔 Альтернативные Подходы

### Подход 1: Без AI API (Оффлайн)
- Использовать локальную LLM модель (llama.cpp + Kotlin bindings)
- Простой keyword-based поиск
- Работает полностью оффлайн
- ⚠️ Хуже качество ответов

### Подход 2: Гибридный
- Простой поиск для offline режима
- DeepSeek API когда есть интернет
- Best of both worlds
- ⚠️ Больше кода для поддержки

### Подход 3: Только статические шаблоны
- Без AI вообще
- Заранее подготовленные ответы
- Очень быстро
- ⚠️ Не гибко

---

## ✅ Рекомендации

### Для MVP (если времени мало):
1. Начните с **Подхода 2** (простой поиск + DeepSeek)
2. Реализуйте базовый чат за 1 день
3. Добавьте /help команды
4. Можно обойтись без full RAG

### Для полной версии:
1. Реализуйте RAG с embeddings
2. Добавьте MCP для git контекста
3. Улучшите UI/UX
4. Добавьте тесты

---

## 🎓 Выводы

**Ваша текущая реализация:**
- ✅ Отличная документация
- ✅ Правильное понимание задачи
- ⚠️ Система работает через внешние AI (Copilot)
- ⚠️ Нет встроенного чата в приложении

**Что нужно для полного соответствия заданию:**
- 🔴 Встроенный AI-чат в приложение
- 🔴 RAG для поиска по документации
- 🔴 MCP для git контекста

**Мой совет:**
Начните с MVP (простой чат + keyword search), это займет 1-2 дня. Полную RAG систему можно добавить потом, если потребуется.

