# 🎓 Как Использовать AI-Ассистент (Claude + RAG)

Пошаговое руководство по использованию AI-ассистента с документацией проекта.

---

## 🚀 Быстрый Старт (3 шага)

### Шаг 1: Индексация документации

```bash
cd /Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest
python3 .claude/index_docs.py
```

**Ожидаемый результат:**
```
✅ Indexing complete!
   Total documents: 9
   Total chunks: 485
```

### Шаг 2: Проверка работы

```bash
python3 .claude/search_docs.py "/help architecture"
```

**Должно показать** фрагменты из ARCHITECTURE.md с релевантной информацией.

### Шаг 3: Использование

```bash
# Вариант A: Прямой поиск
python3 .claude/search_docs.py "How to add UseCase"

# Вариант B: С полным контекстом
python3 .claude/claude_helper.py "Explain Clean Architecture"

# Вариант C: Команда /help
python3 .claude/claude_helper.py "/help koin"
```

---

## 💬 Примеры Использования

### Пример 1: Вопрос об архитектуре

```bash
python3 .claude/claude_helper.py "What are the layers in Clean Architecture?"
```

**Что получите:**
```
================================================================================
CONTEXT FOR CLAUDE
================================================================================
# Project Context

**Type**: Kotlin Multiplatform (KMP) Note-taking application
**Architecture**: Clean Architecture with Feature Slicing
...

# Git Context
**Branch:** main
**Modified files:**
- (список файлов)

# Relevant Documentation

## From ARCHITECTURE.md - Clean Architecture Layers
...слои архитектуры...

================================================================================
USER QUERY: What are the layers in Clean Architecture?
================================================================================
```

### Пример 2: Как добавить фичу

```bash
python3 .claude/claude_helper.py "How to add a new feature?"
```

**Найдет:**
- Структуру feature из QUICKSTART.md
- Примеры кода
- Best practices из ARCHITECTURE.md

### Пример 3: Команды /help

```bash
# Список всех команд
python3 .claude/claude_helper.py "/help"

# Конкретная тема
python3 .claude/claude_helper.py "/help sqldelight"
```

---

## 🔍 Доступные Команды /help

| Команда | Что найдет |
|---------|-----------|
| `/help` | Список всех доступных команд |
| `/help architecture` | Clean Architecture, слои, поток данных |
| `/help feature` | Как добавить новую фичу |
| `/help koin` | Dependency Injection, модули Koin |
| `/help sqldelight` | Database queries, схемы, миграции |
| `/help compose` | Compose Multiplatform UI паттерны |
| `/help testing` | Unit тесты, паттерны тестирования |
| `/help git` | Git workflow, branching strategy |

---

## 🎯 Типовые Задачи

### Задача 1: Создать новый UseCase

**Команда:**
```bash
python3 .claude/claude_helper.py "How to create a UseCase?"
```

**Или специфичнее:**
```bash
python3 .claude/search_docs.py "UseCase pattern example"
```

**Найдет:**
- Шаблон UseCase из QUICKSTART.md
- Примеры из features/categories и features/notes
- Best practices из ARCHITECTURE.md

### Задача 2: Настроить Koin модуль

**Команда:**
```bash
python3 .claude/claude_helper.py "/help koin"
```

**Найдет:**
- Настройку Koin для KMP
- Примеры модулей (single, factory, viewModel)
- Platform-specific зависимости

### Задача 3: Понять поток данных

**Команда:**
```bash
python3 .claude/claude_helper.py "Explain data flow in the app"
```

**Найдет:**
- Диаграммы потока данных из ARCHITECTURE.md
- Примеры от UI → ViewModel → UseCase → Repository
- Паттерны StateFlow/SharedFlow

### Задача 4: Добавить SQLDelight query

**Команда:**
```bash
python3 .claude/claude_helper.py "/help sqldelight"
```

**Найдет:**
- Синтаксис .sq файлов
- Примеры queries (select, insert, update, delete)
- Использование в DataSource

---

## 🔧 Интеграция с Claude CLI

Если у вас установлен **Claude Code CLI**:

### Вариант 1: Прямой pipe

```bash
python3 .claude/claude_helper.py "your question" | claude
```

### Вариант 2: Сохранить контекст в файл

```bash
python3 .claude/claude_helper.py --context "UseCase" > /tmp/context.txt
claude --context-file /tmp/context.txt "Explain UseCase pattern"
```

### Вариант 3: Alias для удобства

Добавьте в `~/.zshrc`:

```bash
# Claude helper alias
alias claude-help='python3 /Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest/.claude/claude_helper.py'

# Usage: claude-help "your question"
```

Затем используйте:
```bash
claude-help "How to add feature?"
```

---

## 📊 Что Индексируется?

### Полный список файлов:

1. **README.md** (72 фрагмента)
   - Обзор проекта
   - Установка и запуск
   - Функционал приложения

2. **ARCHITECTURE.md** (52 фрагмента)
   - Clean Architecture детали
   - Паттерны и best practices
   - Примеры кода

3. **PROJECT_STATUS.md** (48 фрагментов)
   - Текущее состояние
   - Что реализовано
   - TODO список

4. **QUICKSTART.md** (34 фрагмента)
   - Быстрый старт
   - Шаблоны кода
   - Чеклисты

5. **AI_HELP_SYSTEM.md** (44 фрагмента)
   - Команды /help
   - Как получить помощь
   - Примеры использования

6. **kmp-prompt.md** (114 фрагментов)
   - Детальный промпт для AI
   - Контекст проекта
   - Паттерны разработки

7. **INDEX.md** (39 фрагментов)
   - Индекс документации
   - Навигация

8. **DOCS_NAVIGATION.md** (38 фрагментов)
   - Структура документов
   - Рекомендации по изучению

9. **AI_HELP_CHEATSHEET.md** (8 фрагментов)
   - Быстрая справка
   - Шпаргалка команд

**ИТОГО: 485 фрагментов**

---

## 🎓 Best Practices

### ✅ Хорошие практики

1. **Специфичные вопросы**
   ```bash
   python3 .claude/claude_helper.py "How to inject repository in ViewModel?"
   ```

2. **Использование команд /help**
   ```bash
   python3 .claude/claude_helper.py "/help compose"
   ```

3. **Контекстные вопросы**
   ```bash
   python3 .claude/claude_helper.py "Show me example of StateFlow in ViewModel"
   ```

### ❌ Избегайте

1. **Слишком общие вопросы**
   ```bash
   python3 .claude/claude_helper.py "help"  # Используйте /help
   ```

2. **Вне контекста проекта**
   ```bash
   python3 .claude/claude_helper.py "What is Kotlin?"  # Не про проект
   ```

3. **Без деталей**
   ```bash
   python3 .claude/claude_helper.py "fix error"  # Какая ошибка?
   ```

---

## 🔄 Обновление Индекса

Когда обновляете MD файлы:

```bash
# Переиндексируйте документацию
python3 .claude/index_docs.py

# Проверьте что всё работает
python3 .claude/search_docs.py "test"
```

---

## 🐛 Troubleshooting

### Проблема: "Index not found"

**Решение:**
```bash
python3 .claude/index_docs.py
```

### Проблема: Поиск ничего не находит

**Решение:**
```bash
# Проверьте индекс
python3 -c "import json; print(json.load(open('.claude/indexed_docs.json'))['total_chunks'])"

# Должно быть 485
```

### Проблема: Git context показывает ошибку

**Решение:**
```bash
# Проверьте что в git репозитории
git status

# Если нет репозитория:
git init
```

---

## 💡 Советы

1. **Комбинируйте команды**
   ```bash
   # Сначала найдите релевантную документацию
   python3 .claude/search_docs.py "ViewModel"
   
   # Затем задайте вопрос с контекстом
   python3 .claude/claude_helper.py "Explain ViewModel pattern"
   ```

2. **Используйте Git контекст**
   - Система автоматически добавляет информацию о вашей ветке
   - Показывает измененные файлы
   - Помогает понять что вы делаете

3. **Проверяйте источники**
   - В результатах поиска всегда указан файл-источник
   - Можно открыть и прочитать полностью

---

## 📚 Дополнительные Ресурсы

### Документация

- [Основной README](./../README.md) - обзор проекта
- [Claude README](./../.claude/README.md) - детали AI интеграции
- [Архитектура](./../ARCHITECTURE.md) - глубокие детали

### Скрипты

- `.claude/index_docs.py` - индексация документации
- `.claude/search_docs.py` - поиск по документам
- `.claude/claude_helper.py` - главный скрипт с RAG

---

## 🎉 Готовые Примеры

### Пример 1: Начало работы с проектом

```bash
# Узнать структуру проекта
python3 .claude/claude_helper.py "/help architecture"

# Понять как добавить фичу
python3 .claude/claude_helper.py "How to add new feature step by step"

# Изучить примеры кода
python3 .claude/search_docs.py "UseCase example"
```

### Пример 2: Работа с базой данных

```bash
# Узнать про SQLDelight
python3 .claude/claude_helper.py "/help sqldelight"

# Найти примеры queries
python3 .claude/search_docs.py "SQLDelight query"

# Понять схему БД
python3 .claude/claude_helper.py "Show database schema"
```

### Пример 3: UI разработка

```bash
# Compose паттерны
python3 .claude/claude_helper.py "/help compose"

# Примеры экранов
python3 .claude/search_docs.py "Compose screen example"

# State management
python3 .claude/claude_helper.py "How to use StateFlow in Compose"
```

---

**Готово! Теперь вы можете эффективно работать с документацией проекта через AI.**

📞 **Вопросы?** Изучите [.claude/README.md](./../.claude/README.md) для деталей.

---

*Last updated: January 12, 2026*

