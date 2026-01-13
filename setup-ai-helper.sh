#!/bin/bash
# Быстрая настройка AI Helper для SubAgentsTest

echo "🚀 Настройка AI Helper для SubAgentsTest..."
echo

# Получаем текущую директорию
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Проверяем наличие Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 не найден. Установите Python 3.8+"
    exit 1
fi

echo "✅ Python найден: $(python3 --version)"

# Проверяем наличие Claude CLI
if ! command -v claude &> /dev/null; then
    echo "⚠️  Claude CLI не найден"
    echo "   Установите: brew install anthropic/claude/claude"
    echo "   Или скачайте: https://claude.ai/download"
    echo
    read -p "Продолжить без Claude CLI? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
    CLAUDE_AVAILABLE=false
else
    echo "✅ Claude CLI найден: $(claude --version 2>&1 | head -1)"
    CLAUDE_AVAILABLE=true
fi

# Индексация документации
echo
echo "📚 Индексация документации..."
python3 "$PROJECT_ROOT/.claude/index_docs.py"

if [ $? -ne 0 ]; then
    echo "❌ Ошибка при индексации"
    exit 1
fi

# Создаем alias в .zshrc
echo
echo "⚙️  Настройка alias в ~/.zshrc..."

ALIAS_BLOCK="
# ============================================
# SubAgentsTest AI Helper
# ============================================
export SUBAGENTS_ROOT=\"$PROJECT_ROOT\"

# Поиск по документации (без AI)
alias help-search='python3 \$SUBAGENTS_ROOT/.claude/search_docs.py'

# Получить контекст (без AI)
alias help-context='python3 \$SUBAGENTS_ROOT/.claude/claude_helper.py'
"

# Добавляем функцию ask только если Claude CLI установлен
if [ "$CLAUDE_AVAILABLE" = true ]; then
    ALIAS_BLOCK+="
# Вопрос с автоматической передачей в Claude AI
function ask() {
    python3 \$SUBAGENTS_ROOT/.claude/claude_helper.py \"\$@\" | claude
}
"
fi

ALIAS_BLOCK+="
# ============================================
"

# Проверяем есть ли уже наши alias
if grep -q "SubAgentsTest AI Helper" ~/.zshrc 2>/dev/null; then
    echo "⚠️  Alias уже существуют в ~/.zshrc"
    read -p "Перезаписать? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # Удаляем старый блок
        sed -i.bak '/# SubAgentsTest AI Helper/,/# ============================================/d' ~/.zshrc
        echo "$ALIAS_BLOCK" >> ~/.zshrc
        echo "✅ Alias обновлены"
    else
        echo "⏭️  Пропускаем обновление alias"
    fi
else
    echo "$ALIAS_BLOCK" >> ~/.zshrc
    echo "✅ Alias добавлены в ~/.zshrc"
fi

# Применяем изменения
source ~/.zshrc 2>/dev/null || true

# Финальная информация
echo
echo "🎉 Настройка завершена!"
echo
echo "📋 Доступные команды:"
echo
echo "  help-search <query>      - Поиск по документации"
echo "  help-context <query>     - Получить контекст (Project + Git + Docs)"

if [ "$CLAUDE_AVAILABLE" = true ]; then
    echo "  ask <question>           - Задать вопрос Claude AI с контекстом"
fi

echo
echo "💡 Примеры использования:"
echo
echo "  help-search \"/help architecture\""
echo "  help-context \"How to add UseCase?\""

if [ "$CLAUDE_AVAILABLE" = true ]; then
    echo "  ask \"Explain Clean Architecture\""
    echo "  ask \"/help koin\""
fi

echo
echo "📚 Документация:"
echo "  - HOW_TO_USE_HELP_WITH_AI.md - полное руководство"
echo "  - .claude/README.md - техническая документация"
echo "  - START_HERE.md - быстрый старт"
echo
echo "🔄 Чтобы применить alias в текущей сессии:"
echo "  source ~/.zshrc"
echo

