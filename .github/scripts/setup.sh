#!/bin/bash
# Скрипт для быстрой настройки AI Code Review

set -e

echo "🤖 Настройка AI Code Review System"
echo "===================================="
echo ""

# Проверяем, что мы в корне проекта
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ Запустите скрипт из корня проекта"
    exit 1
fi

# Проверяем Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 не найден. Установите Python 3.8+"
    exit 1
fi

echo "✅ Python найден: $(python3 --version)"

# Устанавливаем зависимости
echo ""
echo "📦 Установка Python зависимостей..."
pip3 install -r .github/scripts/requirements.txt

# Проверяем API ключ
if [ -z "$ANTHROPIC_API_KEY" ]; then
    echo ""
    echo "⚠️  ANTHROPIC_API_KEY не установлен"
    echo ""
    echo "Для работы системы нужен API ключ от Anthropic Claude:"
    echo "1. Перейдите на https://console.anthropic.com/"
    echo "2. Создайте API ключ"
    echo "3. Добавьте в GitHub Secrets:"
    echo "   Settings → Secrets → Actions → New repository secret"
    echo "   Имя: ANTHROPIC_API_KEY"
    echo "   Значение: ваш ключ"
    echo ""
    echo "Для локального тестирования:"
    echo "   export ANTHROPIC_API_KEY='your-key'"
    echo ""
else
    echo ""
    echo "✅ ANTHROPIC_API_KEY установлен"

    # Тестируем API
    echo ""
    echo "🧪 Тестирование API подключения..."
    python3 - <<EOF
import os
from anthropic import Anthropic

try:
    client = Anthropic(api_key=os.environ.get('ANTHROPIC_API_KEY'))
    # Простой тест API
    response = client.messages.create(
        model="claude-3-5-sonnet-20241022",
        max_tokens=50,
        messages=[{"role": "user", "content": "Hello"}]
    )
    print("✅ API работает корректно")
except Exception as e:
    print(f"❌ Ошибка API: {e}")
    exit(1)
EOF
fi

# Делаем скрипты исполняемыми
echo ""
echo "🔧 Настройка прав доступа..."
chmod +x .github/scripts/ai_code_review.py
chmod +x .github/scripts/test_review.py

echo ""
echo "✅ Настройка завершена!"
echo ""
echo "📋 Следующие шаги:"
echo "1. Добавьте ANTHROPIC_API_KEY в GitHub Secrets"
echo "2. Убедитесь, что Workflow permissions = 'Read and write'"
echo "   (Settings → Actions → General)"
echo "3. Создайте Pull Request для тестирования"
echo ""
echo "🧪 Локальное тестирование:"
echo "   python3 .github/scripts/test_review.py"
echo ""
echo "📚 Документация:"
echo "   .github/AI_CODE_REVIEW_README.md"

