# SubAgentsTest - Project Context for Claude

## 📋 Project Overview

**Type**: Kotlin Multiplatform (KMP) Note-taking application
**Architecture**: Clean Architecture with Feature Slicing
**Platforms**: Android, iOS, Desktop (macOS), Web (JS)

---

## 🏗️ Architecture

### Clean Architecture Layers

```
Presentation Layer (UI)
    ↓ Use Cases
Domain Layer (Business Logic)
    ↓ Repository Interface
Data Layer (SQLDelight + API)
```

### Feature Structure

```
features/[feature-name]/
├── data/
│   ├── local/              # SQLDelight DataSource
│   └── repository/         # Repository Implementation
├── domain/
│   ├── model/             # Domain Models
│   ├── repository/        # Repository Interface
│   └── usecase/           # Use Cases
└── presentation/
    ├── [screen]/          # Compose UI Screens
    └── viewmodel/         # ViewModels (StateFlow)
```

---

## 🎯 Current Features

### 1. Categories (✅ Implemented)
- CRUD operations
- Color coding
- Cascade delete

**Key Files:**
- `features/categories/domain/model/Category.kt`
- `features/categories/presentation/viewmodel/CategoriesViewModel.kt`
- `features/categories/data/repository/CategoryRepositoryImpl.kt`

### 2. Notes (✅ Implemented)
- CRUD operations
- Full-text search
- Category association

**Key Files:**
- `features/notes/domain/model/Note.kt`
- `features/notes/presentation/viewmodel/NotesViewModel.kt`
- `features/notes/data/repository/NoteRepositoryImpl.kt`

---

## 🗄️ Database Schema (SQLDelight)

### Category Table
```sql
CREATE TABLE Category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    color TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

### Note Table
```sql
CREATE TABLE Note (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE CASCADE
);
```

---

## 🔧 Technology Stack

### Core
- Kotlin 2.0.21
- Kotlin Multiplatform
- Compose Multiplatform 1.7.3

### DI & State
- Koin 4.0.0 (Dependency Injection)
- Kotlin Coroutines + Flow (Async)
- StateFlow/SharedFlow (Reactive UI)

### Database
- SQLDelight 2.0.2 (Type-safe SQL)

---

## 📐 Design Patterns

### State Management
```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String?) : UiState<Nothing>
}
```

### Event Pattern
```kotlin
sealed interface ScreenEvent {
    data class ShowError(val message: String) : ScreenEvent
    data class NavigateTo(val route: String) : ScreenEvent
}
```

### Unidirectional Data Flow
```
User Action → ViewModel → Use Case → Repository → DataSource
     ↑                                                 ↓
     ←─────────── UI State Update ←──────────────────┘
```

---

## 🎨 Code Style Guidelines

### Naming Conventions
- **ViewModels**: `[Feature]ViewModel` (e.g., `CategoriesViewModel`)
- **Use Cases**: `[Verb][Feature]UseCase` (e.g., `GetCategoriesUseCase`)
- **Repositories**: `[Feature]Repository` (interface) + `[Feature]RepositoryImpl`
- **Screens**: `[Feature][Type]Screen` (e.g., `CategoriesListScreen`)

### File Organization
```kotlin
// 1. Package declaration
package ru.macdroid.subagentstest.features.categories.domain.model

// 2. Imports (grouped: stdlib, kmp, project)
import kotlinx.datetime.Instant

// 3. Domain model
data class Category(
    val id: Long = 0,
    val name: String,
    val color: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

---

## 🔍 Common Tasks & Solutions

### Adding a New Use Case

1. **Create Interface** (domain/usecase/)
```kotlin
class GetItemsUseCase(
    private val repository: ItemRepository
) {
    operator fun invoke(): Flow<List<Item>> = repository.getItems()
}
```

2. **Register in Koin**
```kotlin
val itemsModule = module {
    factory { GetItemsUseCase(get()) }
}
```

3. **Use in ViewModel**
```kotlin
class ItemsViewModel(
    private val getItemsUseCase: GetItemsUseCase
) : ViewModel() {
    init {
        viewModelScope.launch {
            getItemsUseCase()
                .catch { /* handle error */ }
                .collect { items -> /* update state */ }
        }
    }
}
```

### Adding SQLDelight Query

1. **Edit .sq file** (database/[Feature].sq)
```sql
selectAll:
SELECT * FROM Item;

insert:
INSERT INTO Item(name, value) VALUES (?, ?);
```

2. **Use in DataSource**
```kotlin
class ItemLocalDataSource(
    private val database: AppDatabase
) {
    fun getItems(): Flow<List<ItemEntity>> {
        return database.itemQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
}
```

### Handling Platform-Specific Code

```kotlin
// commonMain - expect declaration
expect fun getPlatformName(): String

// androidMain - actual implementation
actual fun getPlatformName(): String = "Android"

// iosMain - actual implementation
actual fun getPlatformName(): String = "iOS"
```

---

## 🐛 Common Issues & Solutions

### Issue: ViewModel not injecting
**Solution**: Check Koin module is registered in `App.kt`

### Issue: SQLDelight query not found
**Solution**: Run `./gradlew generateSqlDelightInterface`

### Issue: Flow not updating UI
**Solution**: Ensure `collectAsState()` or `collectAsStateWithLifecycle()` is used

---

## 📚 Key Documentation Files

1. **README.md** - Project overview
2. **ARCHITECTURE.md** - Detailed architecture
3. **PROJECT_STATUS.md** - Current status
4. **QUICKSTART.md** - Quick reference
5. **AI_HELP_SYSTEM.md** - Help system

---

## 🎯 Current Git Context

**Main Branch**: main
**Active Development**: Categories + Notes features complete

---

## 💡 Development Tips

1. **Always use Use Cases** - Never call repository directly from ViewModel
2. **Immutable State** - Use `data class` with `val` for UI states
3. **Error Handling** - Always use `.catch {}` on Flows
4. **Testing** - Write tests for Use Cases first (they're pure functions)
5. **Platform Code** - Keep `expect/actual` minimal, prefer interfaces

---

## 🔗 Quick Links

- SQLDelight Docs: https://cashapp.github.io/sqldelight/
- Koin KMP: https://insert-koin.io/docs/reference/koin-mp/kmp
- Compose Multiplatform: https://www.jetbrains.com/lp/compose-multiplatform/

---

*Last Updated: January 12, 2026*

