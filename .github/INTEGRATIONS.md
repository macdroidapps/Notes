# 🔗 Интеграции AI Code Review

Расширенные возможности интеграции с другими инструментами.

## 🔧 Интеграция с Detekt

Комбинирование AI review с статическим анализом Detekt.

### Настройка

1. **Создайте workflow с Detekt:**

```yaml
# .github/workflows/detekt.yml
name: Detekt Analysis

on:
  pull_request:
    paths:
      - '**/*.kt'
      - '**/*.kts'

jobs:
  detekt:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Detekt
        run: ./gradlew detekt
      
      - name: Upload SARIF
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: build/reports/detekt/detekt.sarif
```

2. **Объедините с AI Review:**

```yaml
# .github/workflows/combined-review.yml
name: Combined Code Review

on:
  pull_request:
    paths: ['**/*.kt', '**/*.kts']

jobs:
  static-analysis:
    name: Detekt
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Detekt
        run: ./gradlew detekt
      - uses: actions/upload-artifact@v4
        with:
          name: detekt-report
          path: build/reports/detekt/
  
  ai-review:
    name: AI Review
    needs: static-analysis  # Запускается после Detekt
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      
      - name: Download Detekt Report
        uses: actions/download-artifact@v4
        with:
          name: detekt-report
          path: detekt-report/
      
      - name: AI Review with Detekt context
        env:
          ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
        run: |
          # Передаём результаты Detekt в AI для контекста
          python .github/scripts/ai_code_review.py \
            --detekt-report detekt-report/detekt.xml \
            ...
```

## 🎨 Интеграция с ktlint

Автоматическое форматирование и style checking.

### Pre-commit hook

```bash
# .git/hooks/pre-commit
#!/bin/bash
./gradlew ktlintCheck
if [ $? -ne 0 ]; then
    echo "❌ ktlint нашёл проблемы форматирования"
    echo "Запустите: ./gradlew ktlintFormat"
    exit 1
fi
```

### GitHub Action

```yaml
name: Code Style Check

on: [pull_request]

jobs:
  ktlint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: ktlint
        run: ./gradlew ktlintCheck
```

## 🧪 Интеграция с тестами

AI Review + Coverage Report.

```yaml
name: Tests and Review

on: [pull_request]

jobs:
  tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Run Tests
        run: ./gradlew testDebugUnitTest
      
      - name: Generate Coverage
        run: ./gradlew jacocoTestReport
      
      - name: Upload Coverage
        uses: codecov/codecov-action@v4
        with:
          files: ./build/reports/jacoco/test/jacocoTestReport.xml
  
  ai-review:
    needs: tests
    runs-on: ubuntu-latest
    steps:
      - name: AI Review
        # AI может видеть coverage и комментировать
        # непокрытые участки кода
        run: |
          python .github/scripts/ai_code_review.py \
            --coverage-report coverage.xml \
            ...
```

## 📊 Dashboard интеграция

Создание сводного dashboard с метриками.

### GitHub Actions Summary

```python
# В ai_code_review.py добавьте:

def create_summary(review_data):
    """Создаёт красивый summary для GitHub Actions"""
    summary = f"""
## 📊 Code Review Summary

| Метрика | Значение |
|---------|----------|
| 🔴 Критичные проблемы | {review_data['critical']} |
| 🟡 Важные замечания | {review_data['important']} |
| 💡 Предложения | {review_data['suggestions']} |
| ✅ Файлов проанализировано | {review_data['files_count']} |
| 📝 Строк изменено | +{review_data['added']} -{review_data['removed']} |

### 🎯 Оценка качества: {review_data['quality_score']}/10

{review_data['key_findings']}
"""
    
    # Записываем в GitHub Actions Summary
    with open(os.environ.get('GITHUB_STEP_SUMMARY', 'summary.md'), 'a') as f:
        f.write(summary)
```

### Использование в workflow

```yaml
- name: AI Review
  id: review
  run: |
    python .github/scripts/ai_code_review.py ... > review.md
    
- name: Create Summary
  run: |
    cat review.md >> $GITHUB_STEP_SUMMARY
```

## 🔔 Интеграция с Slack

Уведомления в Slack о результатах review.

```yaml
# .github/workflows/code-review.yml

- name: Notify Slack
  if: steps.ai-review.outputs.critical_issues > 0
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
    payload: |
      {
        "text": "🔴 Critical issues found in PR #${{ github.event.pull_request.number }}",
        "blocks": [
          {
            "type": "section",
            "text": {
              "type": "mrkdwn",
              "text": "*PR:* <${{ github.event.pull_request.html_url }}|#${{ github.event.pull_request.number }}>\n*Critical Issues:* ${{ steps.ai-review.outputs.critical_issues }}"
            }
          }
        ]
      }
```

## 📈 Метрики и аналитика

Сбор статистики по code quality.

### Сохранение метрик

```python
# metrics_collector.py
import json
from datetime import datetime

def save_metrics(pr_number, review_data):
    """Сохраняет метрики review в JSON"""
    metrics = {
        'timestamp': datetime.now().isoformat(),
        'pr_number': pr_number,
        'critical_issues': review_data['critical'],
        'important_issues': review_data['important'],
        'suggestions': review_data['suggestions'],
        'files_changed': review_data['files_count'],
        'lines_changed': review_data['lines_changed']
    }
    
    # Добавляем в историю
    with open('metrics-history.json', 'a') as f:
        json.dump(metrics, f)
        f.write('\n')
```

### Визуализация

```python
# generate_report.py
import pandas as pd
import matplotlib.pyplot as plt

# Читаем историю метрик
df = pd.read_json('metrics-history.json', lines=True)

# График по времени
plt.figure(figsize=(12, 6))
plt.plot(df['timestamp'], df['critical_issues'], label='Critical')
plt.plot(df['timestamp'], df['important_issues'], label='Important')
plt.xlabel('Date')
plt.ylabel('Issues Count')
plt.title('Code Quality Trends')
plt.legend()
plt.savefig('quality-trends.png')
```

## 🔐 Интеграция с Security Scanning

Комбинация с GitHub Advanced Security.

```yaml
name: Security and AI Review

on: [pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Initialize CodeQL
        uses: github/codeql-action/init@v3
        with:
          languages: java
      
      - name: Build
        run: ./gradlew build
      
      - name: Perform CodeQL Analysis
        uses: github/codeql-action/analyze@v3
      
      - name: Save Security Report
        run: |
          # Экспортируем результаты CodeQL
          gh api /repos/${{ github.repository }}/code-scanning/alerts \
            > security-alerts.json
  
  ai-review:
    needs: security
    runs-on: ubuntu-latest
    steps:
      - name: AI Review with Security Context
        env:
          ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
        run: |
          # AI учитывает security alerts
          python .github/scripts/ai_code_review.py \
            --security-alerts security-alerts.json \
            ...
```

## 🚀 CI/CD Pipeline integration

### GitLab CI

```yaml
# .gitlab-ci.yml
stages:
  - test
  - review
  - deploy

ai-code-review:
  stage: review
  image: python:3.11
  script:
    - pip install -r .github/scripts/requirements.txt
    - python .github/scripts/ai_code_review.py
        --diff-file $CI_MERGE_REQUEST_DIFF_BASE_SHA..$CI_COMMIT_SHA
        --output-file review.md
  only:
    - merge_requests
  artifacts:
    paths:
      - review.md
```

### Jenkins

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    stages {
        stage('AI Code Review') {
            when {
                changeRequest()
            }
            steps {
                script {
                    sh '''
                        pip install -r .github/scripts/requirements.txt
                        python .github/scripts/ai_code_review.py \
                            --diff-file ${CHANGE_TARGET}...${GIT_COMMIT} \
                            --output-file review.md
                    '''
                    
                    def review = readFile('review.md')
                    
                    // Публикуем как комментарий
                    pullRequest.comment(review)
                }
            }
        }
    }
}
```

### CircleCI

```yaml
# .circleci/config.yml
version: 2.1

jobs:
  ai-review:
    docker:
      - image: cimg/python:3.11
    steps:
      - checkout
      - run:
          name: Install dependencies
          command: pip install -r .github/scripts/requirements.txt
      - run:
          name: Run AI Review
          command: |
            python .github/scripts/ai_code_review.py \
              --diff-file <(git diff origin/main...HEAD) \
              --output-file review.md
      - store_artifacts:
          path: review.md

workflows:
  pr-review:
    jobs:
      - ai-review:
          filters:
            branches:
              ignore: main
```

## 📱 Mobile App Notifications

Push-уведомления о результатах review.

### Telegram Bot

```python
# telegram_notifier.py
import requests

def notify_telegram(chat_id, token, review_summary):
    """Отправляет уведомление в Telegram"""
    message = f"""
🤖 *AI Code Review Complete*

🔴 Critical: {review_summary['critical']}
🟡 Important: {review_summary['important']}
💡 Suggestions: {review_summary['suggestions']}

[View PR]({review_summary['pr_url']})
"""
    
    url = f"https://api.telegram.org/bot{token}/sendMessage"
    data = {
        'chat_id': chat_id,
        'text': message,
        'parse_mode': 'Markdown'
    }
    requests.post(url, data=data)
```

## 🎯 Лучшие практики интеграции

1. **Не блокируйте деплой** — AI review как advisory, не blocking
2. **Кэшируйте результаты** — не повторяйте анализ при каждом push
3. **Параллелизуйте** — запускайте AI review параллельно с тестами
4. **Ограничивайте scope** — не анализируйте сгенерированный код
5. **Собирайте метрики** — отслеживайте эффективность review

---

**Документация обновлена:** 13 января 2026

