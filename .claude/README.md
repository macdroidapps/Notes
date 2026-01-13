# 🤖 Claude Code CLI Integration for SubAgentsTest

Полная интеграция **RAG** (Retrieval Augmented Generation) с **Claude Code CLI** для работы с документацией проекта.

---

## 📋 Что это?

Система, которая позволяет Claude Code CLI:
- 📚 **Искать по документации** - 9 MD файлов с 485+ фрагментами
- 🔧 **Понимать Git контекст** - текущая ветка, измененные файлы, коммиты
- 💬 **Отвечать на команды /help** - быстрая справка по проекту
- 🎯 **Давать контекстные ответы** - на основе реальной документации

---

## 🚀 Быстрый Старт

### 1. Индексация документации

Запустите индексацию один раз (или когда обновляете документацию):

```bash
cd /Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest
python3 .claude/index_docs.py
```

**Результат:**
```
✅ Indexing complete!
   Total documents: 9
   Total chunks: 485
   Total keywords: 16
```

### 2. Использование с Claude

#### Вариант A: Прямой поиск

```bash
# Поиск по документации
python3 .claude/search_docs.py "Clean Architecture"

# Команда /help
python3 .claude/search_docs.py "/help koin"
```

#### Вариант B: Полный контекст для Claude

```bash
# Получить контекст + Git информация
python3 .claude/claude_helper.py "How to add a new feature?"

# Только /help
python3 .claude/claude_helper.py "/help architecture"

# Только контекст (без форматирования)
python3 .claude/claude_helper.py --context "UseCase pattern"
```

#### Вариант C: Интеграция с Claude CLI (Рекомендуется!)

Если у вас установлен Claude Code CLI:

```bash
# Прямой вопрос с контекстом
python3 .claude/claude_helper.py "How to add UseCase?" | claude

# Использование в pipe
echo "Explain the architecture" | python3 .claude/claude_helper.py --context "architecture" | claude
```

#### Вариант D: С Alias (Самый Удобный!)

Добавьте в `~/.zshrc`:

```bash
# SubAgentsTest AI Helper
export SUBAGENTS_ROOT="/Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest"

# Быстрый поиск
alias help-search='python3 $SUBAGENTS_ROOT/.claude/search_docs.py'

# С автоматической передачей в Claude
function ask() {
    python3 $SUBAGENTS_ROOT/.claude/claude_helper.py "$@" | claude
}
```

Применить: `source ~/.zshrc`

Использование:
```bash
# Команда /help с AI ответом
ask "/help architecture"

# Обычный вопрос
ask "How to add UseCase?"

# Быстрый поиск (без AI)
help-search "ViewModel pattern"
```

**📖 Подробнее:** См. [HOW_TO_USE_HELP_WITH_AI.md](../HOW_TO_USE_HELP_WITH_AI.md) для всех способов интеграции с AI агентами (Copilot, Cursor, Claude Desktop, и др.)

---

## 📁 Структура .claude/

```
.claude/
├── config.json              # Конфигурация MCP
├── project_context.md       # Краткий контекст проекта
├── indexed_docs.json        # Индексированная документация (485 chunks)
├── index_docs.py           # Скрипт индексации
├── search_docs.py          # Поиск по документам
└── claude_helper.py        # Главный скрипт с RAG
```

---

## 🔍 Что индексируется?

| Файл | Фрагментов | Описание |
|------|-----------|----------|
| `README.md` | 72 | Общая информация |
| `ARCHITECTURE.md` | 52 | Детали архитектуры |
| `PROJECT_STATUS.md` | 48 | Текущий статус |
| `QUICKSTART.md` | 34 | Быстрый старт |
| `AI_HELP_SYSTEM.md` | 44 | Система помощи |
| `kmp-prompt.md` | 114 | Промпт для AI |
| `INDEX.md` | 39 | Индекс документов |
| `DOCS_NAVIGATION.md` | 38 | Навигация |
| `AI_HELP_CHEATSHEET.md` | 8 | Шпаргалка |
| **ИТОГО** | **485** | |

---

## 💬 Команды /help

### Доступные команды

| Команда | Что делает |
|---------|-----------|
| `/help` | Список всех команд |
| `/help architecture` | Объяснение Clean Architecture |
| `/help feature` | Как добавить новую фичу |
| `/help koin` | Dependency Injection с Koin |
| `/help sqldelight` | Database запросы и схемы |
| `/help compose` | Compose Multiplatform UI |
| `/help testing` | Паттерны тестирования |
| `/help git` | Git workflow |

### Примеры использования

```bash
# Получить список команд
python3 .claude/claude_helper.py "/help"

# Узнать про архитектуру
python3 .claude/claude_helper.py "/help architecture"

# Узнать про Koin DI
python3 .claude/claude_helper.py "/help koin"
```

---

## 🔧 Git Интеграция (MCP)

Система автоматически собирает Git контекст:

```python
{
    "branch": "main",
    "modified_files": [
        "composeApp/src/commonMain/kotlin/features/ai/..."
    ],
    "recent_commits": [
        "abc123 Add AI assistant feature",
        "def456 Update documentation"
    ]
}
```

**Пример вывода:**

```markdown
# Git Context

**Branch:** main

**Modified files:**
- composeApp/src/.../AIChatViewModel.kt
- build.gradle.kts

**Recent commits:**
- abc123 Add AI assistant feature
- def456 Update documentation
```

---

## 🎯 Примеры Запросов

### 1. Общие вопросы о проекте

```bash
python3 .claude/claude_helper.py "What is the project structure?"
```

**Что получите:**
- Project Context из `.claude/project_context.md`
- Git context (ветка, файлы)
- Релевантные фрагменты из README.md и ARCHITECTURE.md

### 2. Технические вопросы

```bash
python3 .claude/claude_helper.py "How to create a new UseCase?"
```

**Что получите:**
- Примеры кода из QUICKSTART.md
- Паттерны из ARCHITECTURE.md
- Git контекст

### 3. Команды /help

```bash
python3 .claude/claude_helper.py "/help koin"
```

**Что получите:**
- Специфичная документация по Koin
- Примеры конфигурации
- Code snippets

---

## 🧪 Тестирование

### Проверка индексации

```bash
python3 .claude/index_docs.py
```

Должно показать:
```
✅ Indexing complete!
   Total documents: 9
   Total chunks: 485
```

### Проверка поиска

```bash
python3 .claude/search_docs.py "ViewModel"
```

Должно найти фрагменты с упоминанием ViewModel.

### Проверка Git контекста

```bash
python3 .claude/claude_helper.py --context "test" | grep "Branch:"
```

Должно показать текущую ветку.

---

## 📊 Статистика Индекса

**Top Keywords** (из 485 фрагментов):

| Keyword | Упоминаний |
|---------|-----------|
| Repository | 70 |
| ViewModel | 68 |
| Koin | 66 |
| UseCase | 61 |
| Compose | 60 |
| SQLDelight | 55 |
| Flow | 47 |
| Android | 46 |
| Clean Architecture | 31 |
| iOS | 30 |

---

## 🔄 Обновление Индекса

Когда обновляете документацию:

```bash
# 1. Переиндексируйте
python3 .claude/index_docs.py

# 2. Проверьте что всё работает
python3 .claude/search_docs.py "test query"
```

---

## 🛠️ Кастомизация

### Добавить новый документ для индексации

Отредактируйте `.claude/index_docs.py`:

```python
DOC_FILES = [
    "README.md",
    "ARCHITECTURE.md",
    # ... существующие
    "NEW_DOCUMENT.md"  # ← добавьте сюда
]
```

Затем переиндексируйте:
```bash
python3 .claude/index_docs.py
```

### Изменить размер фрагментов

В `.claude/index_docs.py`:

```python
def chunk_text(text: str, chunk_size: int = 512, overlap: int = 50):
    #                               ↑ измените размер
```

### Добавить новую команду /help

Отредактируйте `.claude/claude_helper.py`:

```python
help_topics = {
    "architecture": "Clean Architecture with Feature Slicing",
    "mynewcommand": "My New Topic Description"  # ← добавьте
}
```

---

## 🎓 Как это работает?

### 1. Индексация (RAG)

```
MD Files → Text Chunking (512 chars) → Keyword Extraction → JSON Index
```

**Что хранится:**
- Исходный файл
- Содержимое фрагмента
- Метаданные (секция, уровень заголовка)
- Ключевые слова
- Индекс для быстрого поиска

### 2. Поиск

```
User Query → Keyword Matching → Score Calculation → Top-N Results
```

**Scoring:**
- Точное совпадение фразы: +10
- Совпадение слова в тексте: +2 за каждое
- Совпадение keyword: +5
- Совпадение в заголовке секции: +3

### 3. Контекст для Claude

```
Project Context + Git Context + Search Results → Combined Context → Claude
```

---

## 📝 Использование в IDE

### VS Code / Cursor

Если используете VS Code или Cursor с Claude extension:

1. Скопируйте содержимое `.claude/project_context.md`
2. Добавьте в workspace settings:

```json
{
  "claude.context": [
    ".claude/project_context.md",
    ".claude/indexed_docs.json"
  ]
}
```

### JetBrains (Android Studio / IntelliJ)

Если используете AI Assistant:

1. Откройте AI Assistant settings
2. Добавьте custom context:
   - File: `.claude/project_context.md`
   - Type: Project Documentation

---

## 🚦 Troubleshooting

### Ошибка: "Index not found"

```bash
# Создайте индекс
python3 .claude/index_docs.py
```

### Поиск ничего не находит

```bash
# Проверьте индекс
cat .claude/indexed_docs.json | jq '.total_chunks'

# Должно быть > 0
```

### Git контекст не работает

```bash
# Проверьте что вы в git репозитории
git status

# Если нет:
git init
git add .
git commit -m "Initial commit"
```

---

## 💡 Best Practices

### ✅ Хорошие запросы

```bash
# Специфичные вопросы
python3 .claude/claude_helper.py "How to create a repository in data layer?"

# Команды /help
python3 .claude/claude_helper.py "/help sqldelight"

# С контекстом
python3 .claude/claude_helper.py "Explain ViewModel pattern"
```

### ❌ Плохие запросы

```bash
# Слишком общие
python3 .claude/claude_helper.py "help"

# Не относятся к проекту
python3 .claude/claude_helper.py "What is Kotlin?"

# Без контекста
python3 .claude/claude_helper.py "how to fix"
```

---

## 🔗 Интеграция с другими инструментами

### GitHub Copilot

Добавьте в `.github/copilot-instructions.md`:

```markdown
Read project context from:
- .claude/project_context.md
- Use indexed documentation in .claude/indexed_docs.json
```

### ChatGPT / Claude Web

Загрузите файлы:
1. `.claude/project_context.md` - базовый контекст
2. Результат поиска из `search_docs.py`

---

## 📊 Метрики

| Метрика | Значение |
|---------|----------|
| Документов | 9 |
| Фрагментов | 485 |
| Ключевых слов | 16 |
| Размер индекса | ~1.2 MB |
| Время индексации | ~2 сек |
| Время поиска | ~0.1 сек |

---

## 🎯 Roadmap

- [ ] Semantic search с embeddings
- [ ] Web UI для поиска
- [ ] VS Code extension
- [ ] Claude Desktop app integration
- [ ] История запросов
- [ ] Авто-обновление индекса при изменении MD файлов

---

## 📞 Поддержка

Если что-то не работает:

1. Проверьте что Python 3.8+ установлен
2. Убедитесь что запускаете из корня проекта
3. Переиндексируйте: `python3 .claude/index_docs.py`
4. Проверьте что `.claude/indexed_docs.json` существует

---

## 🙏 Acknowledgments

- **Anthropic** - Claude API и MCP protocol
- **Python** - для скриптов индексации
- **Git** - для контекста репозитория

---

**Сделано с ❤️ для SubAgentsTest**

*Last updated: January 12, 2026*

