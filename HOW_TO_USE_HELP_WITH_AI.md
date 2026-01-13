# 🤖 Использование /help через AI Агентов

## 📋 Способы Использования

Есть несколько способов использовать систему `/help` через AI агентов:

---

## 1️⃣ Через Claude Code CLI (Рекомендуется)

### Установка Claude CLI

Если у вас еще нет Claude CLI:

```bash
# macOS
brew install anthropic/claude/claude

# Или скачайте с https://claude.ai/download
```

### Настройка

```bash
# Авторизация
claude auth login

# Проверка
claude --version
```

### Использование с /help

#### Вариант A: Прямой pipe

```bash
# Получить контекст и отправить в Claude
python3 .claude/claude_helper.py "/help architecture" | claude

# Или задать вопрос
python3 .claude/claude_helper.py "How to add UseCase?" | claude
```

#### Вариант B: С файлом контекста

```bash
# Сохранить контекст в файл
python3 .claude/claude_helper.py --context "architecture" > /tmp/context.txt

# Использовать с Claude
claude --context-file /tmp/context.txt "Explain the architecture"
```

#### Вариант C: Создать alias (удобнее всего)

Добавьте в `~/.zshrc`:

```bash
# Alias для быстрого доступа
alias cai='python3 /Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest/.claude/claude_helper.py'

# Alias с автоматической передачей в Claude
function ask-claude() {
    python3 /Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest/.claude/claude_helper.py "$@" | claude
}
```

Примените изменения:
```bash
source ~/.zshrc
```

Теперь используйте:
```bash
# Прямой запрос
cai "/help koin"

# С Claude
ask-claude "How to add feature?"

# Команда /help через Claude
ask-claude "/help architecture"
```

---

## 2️⃣ Через GitHub Copilot Chat (В IDE)

### Настройка

1. Откройте проект в **VS Code** или **JetBrains IDE**
2. Убедитесь что **GitHub Copilot Chat** активен
3. Создайте файл `.github/copilot-instructions.md`:

```markdown
# Project Context for Copilot

This is a Kotlin Multiplatform project with Clean Architecture.

## Context Files

When answering questions about the project, use these files:
- `.claude/project_context.md` - Quick project overview
- `.claude/indexed_docs.json` - Full documentation index (485 chunks)

## Available Documentation

- README.md - Project overview
- ARCHITECTURE.md - Architecture details
- QUICKSTART.md - Quick reference
- PROJECT_STATUS.md - Current status

## How to Get Context

Before answering, search the documentation:

```bash
python3 .claude/search_docs.py "<user question>"
```

Use the results to provide accurate answers.
```

### Использование в Copilot Chat

Теперь в Copilot Chat спрашивайте:

```
@workspace /help architecture
@workspace Explain Clean Architecture in this project
@workspace How to add a new UseCase?
```

Copilot автоматически прочитает `.github/copilot-instructions.md` и `.claude/project_context.md`.

---

## 3️⃣ Через Claude Desktop App

### Настройка MCP

1. Откройте настройки Claude Desktop App
2. Добавьте MCP сервер для вашего проекта:

```json
{
  "mcpServers": {
    "subagentstest": {
      "command": "python3",
      "args": [
        "/Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest/.claude/claude_helper.py",
        "--mcp"
      ],
      "env": {
        "PROJECT_ROOT": "/Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest"
      }
    }
  }
}
```

### Использование

В Claude Desktop App пишите:

```
/help architecture
Explain Clean Architecture
How to add UseCase?
```

Claude автоматически получит контекст через MCP.

---

## 4️⃣ Через Cursor (AI IDE)

### Настройка

1. Откройте проект в **Cursor**
2. Создайте `.cursorrules` в корне проекта:

```markdown
# Project Rules for Cursor AI

## Context Files

- `.claude/project_context.md` - Project overview
- `.claude/indexed_docs.json` - Documentation index

## Before Answering

Search documentation:
```bash
python3 .claude/search_docs.py "<query>"
```

## Architecture

This project uses Clean Architecture with:
- Presentation Layer (UI)
- Domain Layer (Business Logic)
- Data Layer (Repository + DataSource)

See ARCHITECTURE.md for details.
```

### Использование

В Cursor AI Chat:

```
Ctrl+L (открыть чат)
> /help architecture
> How to add UseCase?
> Explain Koin DI
```

Cursor прочитает `.cursorrules` и `.claude/project_context.md`.

---

## 5️⃣ Через Python скрипт (Программно)

### Создайте wrapper скрипт

Сохраните как `ask-ai.py`:

```python
#!/usr/bin/env python3
"""
AI Assistant wrapper для SubAgentsTest
"""
import sys
import subprocess
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent
CLAUDE_HELPER = PROJECT_ROOT / ".claude" / "claude_helper.py"

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 ask-ai.py <question>")
        sys.exit(1)
    
    query = " ".join(sys.argv[1:])
    
    # Получить контекст
    context_result = subprocess.run(
        ["python3", str(CLAUDE_HELPER), query],
        capture_output=True,
        text=True
    )
    
    context = context_result.stdout
    
    # Отправить в Claude CLI
    if subprocess.run(["which", "claude"], capture_output=True).returncode == 0:
        subprocess.run(
            ["claude"],
            input=f"{context}\n\nUser Question: {query}",
            text=True
        )
    else:
        # Если Claude CLI нет, просто показать контекст
        print(context)
        print("\n💡 Tip: Install Claude CLI to get AI answers")
        print("   brew install anthropic/claude/claude")

if __name__ == "__main__":
    main()
```

Сделайте исполняемым:
```bash
chmod +x ask-ai.py
```

### Использование

```bash
./ask-ai.py "/help architecture"
./ask-ai.py "How to add UseCase?"
```

---

## 6️⃣ Через Web UI (Будущее)

### Запуск простого web сервера

Создайте `web-ui.py`:

```python
#!/usr/bin/env python3
"""
Simple web UI for documentation search
"""
from flask import Flask, request, jsonify, render_template_string
import json
from pathlib import Path

app = Flask(__name__)
PROJECT_ROOT = Path(__file__).parent
INDEX_FILE = PROJECT_ROOT / ".claude" / "indexed_docs.json"

HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <title>SubAgentsTest - AI Help</title>
    <style>
        body { font-family: Arial; max-width: 800px; margin: 50px auto; }
        input { width: 100%; padding: 10px; font-size: 16px; }
        #results { margin-top: 20px; }
        .result { background: #f5f5f5; padding: 15px; margin: 10px 0; }
    </style>
</head>
<body>
    <h1>🤖 SubAgentsTest AI Help</h1>
    <input id="query" type="text" placeholder="Ask a question or use /help command...">
    <button onclick="search()">Search</button>
    <div id="results"></div>
    
    <script>
        function search() {
            const query = document.getElementById('query').value;
            fetch('/search?q=' + encodeURIComponent(query))
                .then(r => r.json())
                .then(data => {
                    const results = document.getElementById('results');
                    results.innerHTML = data.map(r => 
                        `<div class="result">
                            <strong>${r.source}</strong> - ${r.section}
                            <pre>${r.content}</pre>
                        </div>`
                    ).join('');
                });
        }
    </script>
</body>
</html>
"""

@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

@app.route('/search')
def search():
    query = request.args.get('q', '')
    with open(INDEX_FILE) as f:
        index = json.load(f)
    
    # Simple search
    results = []
    for chunk in index['chunks'][:10]:  # Top 10
        if query.lower() in chunk['content'].lower():
            results.append({
                'source': chunk['source'],
                'section': chunk['metadata'].get('section_title', ''),
                'content': chunk['content'][:200] + '...'
            })
    
    return jsonify(results)

if __name__ == '__main__':
    app.run(debug=True, port=8080)
```

Установите Flask:
```bash
pip3 install flask
```

Запустите:
```bash
python3 web-ui.py
```

Откройте: http://localhost:8080

---

## 📋 Сравнение Методов

| Метод | Удобство | Скорость | AI Ответы | Оффлайн |
|-------|----------|----------|-----------|---------|
| Claude CLI | ⭐⭐⭐⭐⭐ | Быстро | ✅ Да | ❌ Нет |
| Copilot Chat | ⭐⭐⭐⭐ | Средне | ✅ Да | ❌ Нет |
| Claude Desktop | ⭐⭐⭐⭐⭐ | Быстро | ✅ Да | ❌ Нет |
| Cursor | ⭐⭐⭐⭐ | Средне | ✅ Да | ❌ Нет |
| Python скрипт | ⭐⭐⭐ | Быстро | ⚠️ Опционально | ✅ Да |
| Web UI | ⭐⭐⭐ | Медленно | ❌ Нет | ✅ Да |

---

## 🎯 Рекомендации

### Для ежедневной работы:
**Claude CLI с alias** - самый быстрый способ

```bash
# Настройка (один раз)
echo 'function ask() { python3 ~/.../.claude/claude_helper.py "$@" | claude; }' >> ~/.zshrc
source ~/.zshrc

# Использование
ask "/help koin"
ask "How to add feature?"
```

### Для работы в IDE:
**GitHub Copilot Chat** или **Cursor** - интеграция прямо в редактор

### Для демонстрации:
**Claude Desktop App** - красивый UI

---

## 💡 Примеры Реальных Сценариев

### Сценарий 1: Утренний Workflow

```bash
# Проверяю что изменил вчера
ask "What did I change?"

# Нужна справка по архитектуре
ask "/help architecture"

# Как добавить новый UseCase
ask "Show me UseCase example"
```

### Сценарий 2: Code Review

```bash
# Проверяю соответствие паттернам
ask "What are the ViewModel best practices?"

# Уточняю про DI
ask "/help koin"

# Проверяю структуру фичи
ask "Show feature structure example"
```

### Сценарий 3: Новый разработчик

```bash
# Обзор проекта
ask "Give me project overview"

# Архитектура
ask "/help architecture"

# Как начать разработку
ask "How to add new feature step by step"
```

---

## 🚀 Быстрая Настройка (5 минут)

### Шаг 1: Установите Claude CLI

```bash
brew install anthropic/claude/claude
claude auth login
```

### Шаг 2: Создайте alias

```bash
cat >> ~/.zshrc << 'EOF'

# SubAgentsTest AI Helper
export SUBAGENTS_ROOT="/Users/vladimirzhdanov/AndroidStudioProjects/SubAgentsTest"

# Поиск по документации
alias help-search='python3 $SUBAGENTS_ROOT/.claude/search_docs.py'

# Полный контекст
alias help-context='python3 $SUBAGENTS_ROOT/.claude/claude_helper.py'

# С автоматической передачей в Claude
function ask() {
    python3 $SUBAGENTS_ROOT/.claude/claude_helper.py "$@" | claude
}

EOF

source ~/.zshrc
```

### Шаг 3: Используйте!

```bash
# Быстрый поиск
help-search "/help koin"

# С AI ответом
ask "How to add UseCase?"

# Команда /help с AI
ask "/help architecture"
```

---

## 🎓 Дополнительные Возможности

### Добавить в Git hooks

Создайте `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# Проверка что изменения соответствуют архитектуре

echo "🔍 Checking architecture compliance..."
python3 .claude/claude_helper.py "Are my changes following Clean Architecture?" | claude
```

### Создать команду для commit message

```bash
function smart-commit() {
    local message=$(python3 .claude/claude_helper.py "Suggest commit message for my changes" | claude)
    git commit -m "$message"
}
```

---

## 📞 Поддержка

Если что-то не работает:

1. **Проверьте Python**: `python3 --version` (нужен 3.8+)
2. **Проверьте индекс**: `ls -lh .claude/indexed_docs.json`
3. **Переиндексируйте**: `python3 .claude/index_docs.py`
4. **Проверьте Claude CLI**: `claude --version`

---

**Готово! Теперь вы можете использовать /help через AI агентов! 🎉**

