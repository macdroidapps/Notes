# 🚀 AI Code Review - Quick Start

## Быстрая настройка (2 минуты)

### 1. Получите API ключ Claude

```
1. Откройте https://console.anthropic.com/
2. Войдите или создайте аккаунт
3. API Keys → Create Key
4. Скопируйте ключ
```

### 2. Добавьте в GitHub Secrets

```
1. GitHub → Settings → Secrets and variables → Actions
2. New repository secret
3. Name: ANTHROPIC_API_KEY
4. Secret: [вставьте ваш ключ]
5. Add secret
```

### 3. Настройте Permissions

```
1. GitHub → Settings → Actions → General
2. Workflow permissions
3. Выберите: "Read and write permissions"
4. Save
```

### 4. Создайте тестовый PR

```bash
# Создайте новую ветку
git checkout -b test-ai-review

# Внесите изменения в любой .kt файл
echo "// Test change" >> composeApp/src/commonMain/kotlin/App.kt

# Commit и push
git add .
git commit -m "Test: AI Code Review"
git push origin test-ai-review

# Создайте PR через GitHub UI
```

### 5. Дождитесь результата

- Откройте созданный PR
- Через 2-5 минут появится комментарий с review
- Проверьте GitHub Actions для логов

## Локальное тестирование

```bash
# 1. Установите зависимости
pip3 install anthropic requests

# 2. Установите API ключ
export ANTHROPIC_API_KEY='your-key'

# 3. Запустите тест
python3 .github/scripts/test_review.py
```

## Что дальше?

- 📖 Полная документация: `.github/AI_CODE_REVIEW_README.md`
- ⚙️ Настройки: `.github/ai-review-config.yml`
- 🐛 Проблемы: см. раздел Troubleshooting в README

## Примеры замечаний

### Критичное 🔴
```kotlin
// ❌ Утечка памяти
class MyViewModel : ViewModel() {
    init {
        GlobalScope.launch {  // 🔴 Используйте viewModelScope!
            fetchData()
        }
    }
}
```

### Важное 🟡
```kotlin
// ⚠️ Нарушение архитектуры
class MyViewModel(
    private val database: Database  // 🟡 Используйте Repository!
) : ViewModel()
```

### Предложение 💡
```kotlin
// 💡 Можно улучшить
var count = 0  // 💡 Рассмотрите StateFlow для реактивности
```

## Быстрая диагностика

### Review не появился?
```bash
# Проверьте Actions
GitHub → Actions → Последний workflow run → Посмотрите логи
```

### Ошибка API?
```bash
# Проверьте ключ локально
python3 << EOF
from anthropic import Anthropic
client = Anthropic(api_key='your-key')
print("✅ API работает")
EOF
```

### Нужна помощь?
- 📖 Полная документация в `.github/AI_CODE_REVIEW_README.md`
- 🐛 Создайте Issue с логами из GitHub Actions
- 💬 Проверьте секцию Troubleshooting

---

**Готово! 🎉** Теперь AI будет помогать с code review в каждом PR.

