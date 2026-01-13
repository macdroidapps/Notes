#!/usr/bin/env python3
"""
Тестовый скрипт для локального запуска AI Code Review
"""

import os
import sys
from pathlib import Path
import subprocess

def create_test_files():
    """Создаёт тестовые файлы для проверки"""

    test_dir = Path("test-review")
    test_dir.mkdir(exist_ok=True)

    # Пример diff
    diff_content = """diff --git a/composeApp/src/commonMain/kotlin/features/categories/presentation/viewmodel/CategoriesViewModel.kt b/composeApp/src/commonMain/kotlin/features/categories/presentation/viewmodel/CategoriesViewModel.kt
index 1234567..abcdefg 100644
--- a/composeApp/src/commonMain/kotlin/features/categories/presentation/viewmodel/CategoriesViewModel.kt
+++ b/composeApp/src/commonMain/kotlin/features/categories/presentation/viewmodel/CategoriesViewModel.kt
@@ -20,7 +20,7 @@ class CategoriesViewModel(

     init {
-        loadCategories()
+        viewModelScope.launch { loadCategories() }
     }

     private fun loadCategories() {
"""

    # Пример содержимого файла
    file_content = """=== FILE: composeApp/src/commonMain/kotlin/features/categories/presentation/viewmodel/CategoriesViewModel.kt ===
package features.categories.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import features.categories.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadCategories() }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .catch { e -> _uiState.value = CategoriesUiState.Error(e.message) }
                .collect { categories ->
                    _uiState.value = if (categories.isEmpty()) {
                        CategoriesUiState.Empty
                    } else {
                        CategoriesUiState.Success(categories)
                    }
                }
        }
    }

    fun createCategory(name: String, color: String) {
        viewModelScope.launch {
            createCategoryUseCase(name, color)
                .onSuccess { /* Handle success */ }
                .onFailure { /* Handle error */ }
        }
    }
}
"""

    # PR Info
    pr_info = """PR Title: Fix coroutine scope in CategoriesViewModel
PR Description: Fixed potential memory leak by properly managing coroutine scope in init block
Author: testuser
Base Branch: main
"""

    (test_dir / "changes.diff").write_text(diff_content)
    (test_dir / "file_contents.txt").write_text(file_content)
    (test_dir / "pr_info.txt").write_text(pr_info)

    print("✅ Тестовые файлы созданы в test-review/")
    return test_dir


def run_review(test_dir: Path):
    """Запускает AI review"""

    api_key = os.environ.get('ANTHROPIC_API_KEY')
    if not api_key:
        print("❌ Установите ANTHROPIC_API_KEY:")
        print("   export ANTHROPIC_API_KEY='your-key'")
        sys.exit(1)

    docs_dir = Path(".claude")
    if not docs_dir.exists():
        print("⚠️  Директория .claude не найдена")
        print("   AI review будет работать без контекста проекта")

    cmd = [
        "python3", ".github/scripts/ai_code_review.py",
        "--diff-file", str(test_dir / "changes.diff"),
        "--files-file", str(test_dir / "file_contents.txt"),
        "--pr-info-file", str(test_dir / "pr_info.txt"),
        "--docs-dir", str(docs_dir),
        "--output-file", str(test_dir / "review.md")
    ]

    print("🤖 Запуск AI Code Review...")
    print(f"📄 Команда: {' '.join(cmd)}\n")

    result = subprocess.run(cmd, capture_output=True, text=True)

    if result.returncode == 0:
        print("✅ Review завершён успешно!")
        print("\n" + "="*70)
        review_content = (test_dir / "review.md").read_text()
        print(review_content)
        print("="*70)
        print(f"\n📄 Полный review сохранён в: {test_dir / 'review.md'}")
    else:
        print("❌ Ошибка при выполнении review:")
        print(result.stderr)
        sys.exit(1)


def main():
    print("🧪 AI Code Review - Локальное тестирование\n")

    # Проверяем, что скрипт запущен из корня проекта
    if not Path(".github/scripts/ai_code_review.py").exists():
        print("❌ Запустите скрипт из корня проекта:")
        print("   python3 .github/scripts/test_review.py")
        sys.exit(1)

    # Создаём тестовые файлы
    test_dir = create_test_files()

    # Запускаем review
    run_review(test_dir)


if __name__ == '__main__':
    main()

