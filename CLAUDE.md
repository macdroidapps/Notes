# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SubAgentsTest is a **Kotlin Multiplatform** notes management application with hierarchical categories, built with **Compose Multiplatform** and following **Clean Architecture** principles. It targets Android, iOS, Desktop (macOS), and Web platforms.

Key technologies: Kotlin 2.0.21, Compose Multiplatform 1.7.3, Koin 4.0.1, SQLDelight 2.0.2, Coroutines 1.10.1

## Build & Development Commands

### Building and Running

```bash
# Android
./gradlew :composeApp:installDebug

# iOS Simulator
./gradlew :composeApp:iosSimulatorArm64MainBinaries

# Desktop (macOS)
./gradlew :composeApp:runDebugExecutableDesktop

# Web Browser
./gradlew :composeApp:jsBrowserDevelopmentRun
# Opens at http://localhost:8080

# Build all targets
./gradlew build
```

### Testing

```bash
# Run all tests
./gradlew test

# Android tests
./gradlew :composeApp:testDebugUnitTest

# Desktop tests
./gradlew :composeApp:desktopTest
```

### Database Management

```bash
# Generate SQLDelight code after schema changes
./gradlew :composeApp:generateCommonMainNotesDatabase

# Verify SQLDelight schema
./gradlew :composeApp:verifySqlDelightMigration
```

### Cleaning

```bash
# Clean build artifacts
./gradlew clean

# Clean and rebuild
./gradlew clean build
```

## Architecture

The application follows **Clean Architecture** with three distinct layers per feature:

### Layer Flow
```
Presentation (UI/ViewModels) → Domain (Use Cases) → Data (Repositories/DataSources)
```

### Feature Structure Pattern

Each feature follows this exact structure:
```
features/[feature-name]/
├── data/
│   ├── local/[Feature]LocalDataSource.kt    # SQLDelight queries
│   └── repository/[Feature]RepositoryImpl.kt # Repository implementation
├── domain/
│   ├── model/[Feature].kt                    # Domain models
│   ├── repository/[Feature]Repository.kt     # Repository interface
│   └── usecase/                              # Individual use case classes
│       ├── Get[Feature]UseCase.kt
│       ├── Create[Feature]UseCase.kt
│       ├── Update[Feature]UseCase.kt
│       └── Delete[Feature]UseCase.kt
├── presentation/
│   ├── list/[Feature]ListScreen.kt          # Composable UI
│   └── viewmodel/[Feature]ViewModel.kt      # State management
└── di/[Feature]Module.kt                     # Koin DI configuration
```

### Key Architecture Patterns

**1. Domain Models**
- Pure Kotlin data classes with no platform dependencies
- Use `kotlinx.datetime.Instant` for timestamps
- No serialization annotations in domain layer

**2. Repository Pattern**
- Interfaces in `domain/repository/`
- Implementations in `data/repository/`
- Return `Result<T>` for error handling
- All operations wrapped in try-catch

**3. Use Cases**
- One class per operation (Single Responsibility)
- Simple operator `invoke()` function
- Validation logic lives here
- Example:
```kotlin
class CreateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, color: String?): Result<Long> {
        if (name.isBlank()) return Result.failure(Exception("Name required"))
        return repository.createCategory(name, color)
    }
}
```

**4. ViewModels**
- Use `StateFlow<UiState>` for UI state
- Use `SharedFlow<Event>` for one-time events (navigation, toasts)
- All async operations in `viewModelScope`
- Sealed interfaces for type-safe states:
```kotlin
sealed interface UiState {
    data object Loading : UiState
    data object Empty : UiState
    data class Success(val data: List<Item>) : UiState
    data class Error(val message: String) : UiState
}
```

**5. Data Sources**
- Use `withContext(Dispatchers.IO)` for suspend functions
- Return `Flow<List<T>>` for reactive data
- Map SQLDelight entities to domain models
- Convert epoch milliseconds ↔ `Instant`

## Dependency Injection with Koin

### Module Registration Pattern

All modules must be registered in `KoinInitializer.kt` in this order:
1. `platformModule` (platform-specific)
2. `coreModule`
3. `databaseModule`
4. Feature modules (categoriesModule, notesModule, etc.)

### Scope Usage

```kotlin
val featureModule = module {
    // Singleton: Database, DataSources, Repositories
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    singleOf(::CategoryLocalDataSource)

    // Factory: Use Cases (stateless)
    factory { GetCategoriesUseCase(get()) }
    factoryOf(::CreateCategoryUseCase)

    // ViewModel: Lifecycle-aware
    viewModelOf(::CategoriesViewModel)

    // Parameterized ViewModel
    factory { (id: Long) -> NotesViewModel(id, get(), get()) }
}
```

## SQLDelight Database

### Schema Location
`composeApp/src/commonMain/sqldelight/ru/macdroid/subagentstest/database/`

### Schema Rules

**Tables:**
- Use `INTEGER PRIMARY KEY AUTOINCREMENT` for IDs
- Store timestamps as `INTEGER NOT NULL` (epoch milliseconds)
- Use `FOREIGN KEY ... ON DELETE CASCADE` for relationships
- Create indexes on foreign keys: `CREATE INDEX [table]_[column]_idx ON [table]([column]);`

**Queries:**
- Query names use camelCase: `selectById`, `selectByCategory`
- Use parameterized queries with `:paramName` syntax
- Sort by date: `ORDER BY updated_at DESC`

### Migration Pattern

After schema changes:
1. Modify `.sq` file
2. Run `./gradlew :composeApp:generateCommonMainNotesDatabase`
3. Create migration if needed in `sqldelight/migrations/`
4. Update LocalDataSource mapping

## Platform-Specific Code

### expect/actual Pattern

For platform-specific implementations:

```kotlin
// commonMain
expect class DriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain - requires Context
actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(NotesDatabase.Schema, context, "notes.db")
    }
}

// iosMain - no parameters
actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(NotesDatabase.Schema, "notes.db")
    }
}

// desktopMain - file-based
actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".notes/notes.db")
        databasePath.parentFile?.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        NotesDatabase.Schema.create(driver)
        return driver
    }
}

// jsMain - web worker
actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker(js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)"""))
        ).also { NotesDatabase.Schema.create(it).await() }
    }
}
```

### Platform Modules

Define in `core/di/PlatformModule.[platform].kt`:
- **Android**: `single { DriverFactory(androidContext()) }`
- **Other platforms**: `single { DriverFactory() }`

## Adding a New Feature

Follow this step-by-step process:

### 1. Define Domain Layer

```kotlin
// domain/model/MyFeature.kt
data class MyFeature(
    val id: Long = 0,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

// domain/repository/MyFeatureRepository.kt
interface MyFeatureRepository {
    fun getAll(): Flow<Result<List<MyFeature>>>
    suspend fun create(name: String): Result<Long>
}

// domain/usecase/GetMyFeaturesUseCase.kt
class GetMyFeaturesUseCase(private val repository: MyFeatureRepository) {
    operator fun invoke(): Flow<Result<List<MyFeature>>> = repository.getAll()
}
```

### 2. Create Database Schema

```sql
-- sqldelight/.../MyFeature.sq
CREATE TABLE MyFeature (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

selectAll:
SELECT * FROM MyFeature ORDER BY created_at DESC;

insert:
INSERT INTO MyFeature(name, created_at, updated_at)
VALUES (?, ?, ?);
```

Run: `./gradlew :composeApp:generateCommonMainNotesDatabase`

### 3. Implement Data Layer

```kotlin
// data/local/MyFeatureLocalDataSource.kt
class MyFeatureLocalDataSource(private val database: NotesDatabase) {
    fun getAll(): Flow<List<MyFeature>> {
        return database.myFeatureQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    MyFeature(
                        id = entity.id,
                        name = entity.name,
                        createdAt = Instant.fromEpochMilliseconds(entity.created_at),
                        updatedAt = Instant.fromEpochMilliseconds(entity.updated_at)
                    )
                }
            }
    }

    suspend fun insert(name: String): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        database.myFeatureQueries.insert(name, now, now)
        database.myFeatureQueries.lastInsertRowId().executeAsOne()
    }
}

// data/repository/MyFeatureRepositoryImpl.kt
class MyFeatureRepositoryImpl(
    private val localDataSource: MyFeatureLocalDataSource
) : MyFeatureRepository {
    override fun getAll(): Flow<Result<List<MyFeature>>> {
        return localDataSource.getAll()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }

    override suspend fun create(name: String): Result<Long> {
        return try {
            val id = localDataSource.insert(name)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 4. Create ViewModel

```kotlin
// presentation/viewmodel/MyFeatureViewModel.kt
class MyFeatureViewModel(
    private val getUseCase: GetMyFeaturesUseCase,
    private val createUseCase: CreateMyFeatureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyFeatureUiState>(MyFeatureUiState.Loading)
    val uiState: StateFlow<MyFeatureUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MyFeatureEvent>()
    val events: SharedFlow<MyFeatureEvent> = _events.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getUseCase().collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { data ->
                        if (data.isEmpty()) MyFeatureUiState.Empty
                        else MyFeatureUiState.Success(data)
                    },
                    onFailure = { MyFeatureUiState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }
}

sealed interface MyFeatureUiState {
    data object Loading : MyFeatureUiState
    data object Empty : MyFeatureUiState
    data class Success(val items: List<MyFeature>) : MyFeatureUiState
    data class Error(val message: String) : MyFeatureUiState
}
```

### 5. Setup Koin Module

```kotlin
// di/MyFeatureModule.kt
val myFeatureModule = module {
    // Data layer
    singleOf(::MyFeatureLocalDataSource)
    single<MyFeatureRepository> { MyFeatureRepositoryImpl(get()) }

    // Domain layer
    factoryOf(::GetMyFeaturesUseCase)
    factoryOf(::CreateMyFeatureUseCase)

    // Presentation layer
    viewModelOf(::MyFeatureViewModel)
}

// Register in KoinInitializer.kt
fun initKoin() = startKoin {
    modules(
        platformModule,
        coreModule,
        databaseModule,
        categoriesModule,
        notesModule,
        myFeatureModule  // ← Add here
    )
}
```

## Code Style Guidelines

### Naming Conventions
- **ViewModels**: `[Feature]ViewModel`
- **Use Cases**: `[Verb][Feature]UseCase` (e.g., `CreateCategoryUseCase`)
- **Repositories**: `[Feature]Repository` (interface) and `[Feature]RepositoryImpl` (implementation)
- **DataSources**: `[Feature]LocalDataSource`
- **Koin Modules**: `[feature]Module`

### State Management
- **StateFlow** for continuous state (UI state)
- **SharedFlow** for one-time events (navigation, toasts, errors)
- Sealed interfaces for type-safe state definitions
- Always provide Loading, Empty, Success, Error states

### Coroutines
- Use `viewModelScope` in ViewModels
- Use `Dispatchers.IO` for database operations
- Use `Flow` for reactive streams
- Handle cancellation with `catch` operator

## AI Integration

This project includes a comprehensive AI documentation system. Use it for context:

```bash
# Search documentation
python3 .claude/search_docs.py "your query"

# Get full context for Claude
python3 .claude/claude_helper.py "your question"

# Available /help commands
/help architecture  - Clean Architecture explanation
/help feature      - How to add new features
/help koin         - Dependency Injection patterns
/help sqldelight   - Database patterns
/help compose      - UI patterns
```

The `.claude/` directory contains:
- `indexed_docs.json` - 485 documentation fragments from 9 MD files
- `project_context.md` - Quick project overview
- `search_docs.py` - Search tool
- `claude_helper.py` - RAG system with Git context

## Common Patterns

### Search with Debounce
```kotlin
private val searchQuery = MutableStateFlow("")

init {
    searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                getNotesByCategoryUseCase(categoryId)
            } else {
                searchNotesUseCase(categoryId, query)
            }
        }
        .collect { /* handle results */ }
}
```

### Result Handling
```kotlin
suspend fun operation(): Result<T> {
    return try {
        val result = doOperation()
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Navigation Pattern
```kotlin
// In App.kt
var currentScreen by remember { mutableStateOf<Screen>(Screen.Categories) }

when (val screen = currentScreen) {
    is Screen.Categories -> CategoriesListScreen(
        onNavigateToNotes = { categoryId ->
            currentScreen = Screen.Notes(categoryId)
        }
    )
    is Screen.Notes -> NotesListScreen(
        categoryId = screen.categoryId,
        onNavigateBack = { currentScreen = Screen.Categories }
    )
}
```

## Important Notes

- **Never** use platform-specific APIs in `commonMain`
- **Always** map SQLDelight entities to domain models (don't expose DB entities to domain layer)
- **Always** validate input in Use Cases, not in ViewModels
- **Always** use `Result<T>` for operations that can fail
- **Always** handle loading/empty/error states in UI
- **Never** access repositories directly from ViewModels - use Use Cases
- **Never** put business logic in repositories - they only orchestrate data access
