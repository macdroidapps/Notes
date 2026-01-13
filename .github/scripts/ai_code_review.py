#!/usr/bin/env python3
"""
AI Code Review Script
Использует Claude API для анализа изменений в Pull Request
"""

import os
import sys
import json
import argparse
import yaml
from pathlib import Path
from typing import Dict, List, Optional
from anthropic import Anthropic


class CodeReviewAssistant:
    """AI ассистент для code review с контекстом проекта"""

    def __init__(self, api_key: str, config_path: Optional[Path] = None):
        self.client = Anthropic(api_key=api_key)
        self.config = self.load_config(config_path)
        self.model = self.config.get('model', 'claude-3-5-sonnet-20241022')
        self.max_tokens = self.config.get('max_tokens', 8000)
        self.temperature = self.config.get('temperature', 0.3)

    def load_config(self, config_path: Optional[Path]) -> Dict:
        """Загружает конфигурацию из YAML файла"""
        if config_path and config_path.exists():
            try:
                with open(config_path, 'r', encoding='utf-8') as f:
                    return yaml.safe_load(f) or {}
            except Exception as e:
                print(f"⚠️  Не удалось загрузить конфиг: {e}")
                return {}
        return {}

    def load_project_context(self, docs_dir: Path) -> str:
        """Загружает документацию проекта для контекста"""
        context_parts = []

        if not docs_dir.exists():
            return ""

        # Приоритетные файлы документации
        priority_files = [
            "ARCHITECTURE.md",
            "PROJECT_STATUS.md",
            "QUICKSTART.md",
            "INDEX.md"
        ]

        for filename in priority_files:
            file_path = docs_dir / filename
            if file_path.exists():
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    context_parts.append(f"### {filename}\n{content}\n")

        return "\n\n".join(context_parts)

    def build_review_prompt(
        self,
        diff: str,
        file_contents: str,
        pr_info: str,
        project_context: str
    ) -> str:
        """Создаёт промпт для Claude с учётом контекста проекта"""

        prompt = f"""Ты — Code Review Assistant для Kotlin Multiplatform проекта на Clean Architecture.

## 📋 Информация о Pull Request

{pr_info}

## 📚 Контекст проекта (из RAG)

{project_context[:15000]}  # Ограничиваем размер контекста

## 🔍 Diff изменений

```diff
{diff[:20000]}  # Ограничиваем размер diff
```

## 📄 Полное содержимое изменённых файлов

{file_contents[:30000]}  # Ограничиваем размер файлов

---

## 🎯 Твоя задача

Проанализируй изменения как senior Kotlin/KMP разработчик, учитывая:

### 1. Архитектура и Clean Architecture
- Соответствие паттернам проекта (Repository, Use Case, ViewModel)
- Разделение слоёв (Presentation/Domain/Data)
- Принципы SOLID
- Dependency Inversion
- Feature Slicing

### 2. Kotlin/KMP Best Practices
- Корректное использование корутин (Flow, suspend functions)
- Null safety
- Immutability (data classes, val/var)
- Type safety
- Scope functions (let, apply, run, etc.)
- Платформенная независимость (expect/actual)

### 3. Compose Multiplatform
- State management (StateFlow, remember, derivedStateOf)
- Composable best practices
- Recomposition optimization
- Side effects (LaunchedEffect, DisposableEffect)

### 4. Потенциальные проблемы
- Утечки памяти (viewModelScope, lifecycle)
- Thread safety
- Race conditions
- Error handling
- Edge cases

### 5. Код-стиль проекта
- Naming conventions
- Структура файлов
- Documentation
- Тестируемость

## 📝 Формат ответа

Используй следующий формат Markdown:

# 🔍 Code Review Summary

## 📊 Общая оценка

- **Критичных проблем:** X 🔴
- **Важных замечаний:** Y 🟡
- **Предложений:** Z 💡

## 🎯 Ключевые находки

[2-3 самых важных момента кратко]

---

## 📝 Детальные замечания

### 🔴 Critical Issues

[Если есть критичные проблемы]

**Формат:**
📍 **[Файл:строка]** — [Категория]

**Проблема:**
[Чёткое описание]

**Почему это важно:**
[Обоснование с отсылкой к документации/паттернам]

**Предложение:**
```kotlin
// Исправленный код
```

---

### 🟡 Important Notes

[Важные замечания]

---

### 💡 Suggestions

[Предложения по улучшению]

---

## ✅ Что сделано хорошо

[1-3 позитивных момента]

---

## 📚 Полезные ссылки

- [Ссылки на релевантную документацию из проекта]

---

**Категории для замечаний:**
- 🏗️ Architecture
- 🐛 Bug / Potential Bug
- ⚡ Performance
- 🎨 Code Style
- 🧪 Testing
- 📚 Documentation
- ♻️ Refactoring
- 💡 Best Practice
- 🔒 Security
- 🧵 Concurrency

**Тон:**
- Конструктивный и дружелюбный
- Используй "мы": "Давайте рассмотрим..."
- Формулируй как вопросы: "Стоит ли рассмотреть...?"
- Объясняй "почему", а не только "что"

**Приоритизация:**
- 🔴 Critical — баги, утечки памяти, нарушения архитектуры
- 🟡 Important — code smell, неоптимальные решения
- 💡 Suggestion — улучшения, рефакторинг

Начинай анализ!
"""
        return prompt

    def review_code(
        self,
        diff: str,
        file_contents: str,
        pr_info: str,
        project_context: str
    ) -> str:
        """Запускает AI review кода"""

        prompt = self.build_review_prompt(
            diff=diff,
            file_contents=file_contents,
            pr_info=pr_info,
            project_context=project_context
        )

        try:
            response = self.client.messages.create(
                model=self.model,
                max_tokens=self.max_tokens,
                temperature=0.3,  # Более детерминированный для code review
                messages=[
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]
            )

            return response.content[0].text

        except Exception as e:
            return f"""# ❌ Ошибка при анализе

Не удалось выполнить AI code review:

```
{str(e)}
```

Пожалуйста, проверьте:
1. Настройку `ANTHROPIC_API_KEY` в secrets репозитория
2. Квоты API
3. Логи GitHub Actions для деталей
"""


def main():
    parser = argparse.ArgumentParser(description='AI Code Review with Claude')
    parser.add_argument('--diff-file', required=True, help='Файл с diff изменений')
    parser.add_argument('--files-file', required=True, help='Файл с содержимым изменённых файлов')
    parser.add_argument('--pr-info-file', required=True, help='Файл с информацией о PR')
    parser.add_argument('--docs-dir', required=True, help='Директория с документацией проекта')
    parser.add_argument('--output-file', required=True, help='Файл для сохранения результата')
    parser.add_argument('--config', default='.github/ai-review-config.yml', help='Путь к файлу конфигурации')

    args = parser.parse_args()

    # Проверяем API ключ
    api_key = os.environ.get('ANTHROPIC_API_KEY')
    if not api_key:
        print("❌ ANTHROPIC_API_KEY не установлен")
        sys.exit(1)

    # Читаем входные данные
    try:
        with open(args.diff_file, 'r', encoding='utf-8') as f:
            diff = f.read()

        with open(args.files_file, 'r', encoding='utf-8') as f:
            file_contents = f.read()

        with open(args.pr_info_file, 'r', encoding='utf-8') as f:
            pr_info = f.read()
    except FileNotFoundError as e:
        print(f"❌ Файл не найден: {e}")
        sys.exit(1)

    # Создаём ассистента
    config_path = Path(args.config) if args.config else None
    assistant = CodeReviewAssistant(api_key=api_key, config_path=config_path)

    # Загружаем контекст проекта
    docs_dir = Path(args.docs_dir)
    project_context = assistant.load_project_context(docs_dir)

    print("🤖 Запуск AI code review...")
    print(f"📄 Размер diff: {len(diff)} символов")
    print(f"📄 Размер файлов: {len(file_contents)} символов")
    print(f"📚 Размер контекста: {len(project_context)} символов")
    print(f"🔧 Модель: {assistant.model}")

    # Выполняем review
    review_result = assistant.review_code(
        diff=diff,
        file_contents=file_contents,
        pr_info=pr_info,
        project_context=project_context
    )

    # Сохраняем результат
    output_path = Path(args.output_file)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(review_result)

    print(f"✅ Review сохранён в {output_path}")
    print("\n" + "="*50)
    print(review_result[:500] + "..." if len(review_result) > 500 else review_result)


if __name__ == '__main__':
    main()

