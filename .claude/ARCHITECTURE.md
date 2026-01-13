# Архитектура SubAgentsTest

Подробное описание архитектурных решений и паттернов проектирования.

## 📐 Обзор Архитектуры

Проект построен на принципах **Clean Architecture** с **Feature Slicing** подходом. Это обеспечивает:

- ✅ Разделение ответственности
- ✅ Тестируемость кода
- ✅ Независимость от фреймворков
- ✅ Гибкость и масштабируемость
- ✅ Платформенную независимость

## 🔄 Поток Данных

```
┌──────────────────┐
│       UI         │ ← Compose Multiplatform
│  (Presentation)  │
└────────┬─────────┘
         │ State/Events
         ↓
┌────────────────────┐
│    ViewModel       │ ← State Management
└────────┬───────────┘
         │ Use Cases
         ↓
┌────────────────────┐
│   Use Cases        │ ← Business Logic
└────────┬───────────┘
         │ Repository Interface
         ↓
┌────────────────────┐
│  Repository Impl   │ ← Data Coordination
└────────┬───────────┘
         │ Data Source
         ↓
┌────────────────────┐
│ Local Data Source  │ ← SQLDelight
└────────────────────┘
```

---

## 🏛️ Слои Архитектуры

### 1. Presentation Layer (UI)

**Ответственность:**
- Отображение UI
- Обработка пользовательского ввода
- Управление состоянием UI
- Навигация между экранами

**Компоненты:**

#### ViewModels
Управляют состоянием и бизнес-логикой UI.

```kotlin
class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    // Состояние UI - StateFlow для реактивности
    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    // События - SharedFlow для одноразовых действий
    private val _events = MutableSharedFlow<CategoriesEvent>()
    val events: SharedFlow<CategoriesEvent> = _events.asSharedFlow()

    init {
        loadCategories()
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
}
```

**Паттерны:**
- **State Hoisting** — состояние поднимается вверх
- **Unidirectional Data Flow** — данные текут в одном направлении
- **Single Source of Truth** — одно место для состояния

#### UI States
Sealed interfaces для типобезопасного управления состояниями.

```kotlin
sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Empty : CategoriesUiState
    data class Success(val categories: List<Category>) : CategoriesUiState
    data class Error(val message: String) : CategoriesUiState
}
```

**Преимущества:**
- Exhaustive when (компилятор проверяет все случаи)
- Невозможно создать некорректное состояние
- Легко читается и тестируется

#### Compose UI
Декларативный UI с Compose Multiplatform.

```kotlin
@Composable
fun CategoriesListScreen(
    onNavigateToNotes: (categoryId: Long) -> Unit,
    viewModel: CategoriesViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { CategoriesTopBar() }
    ) { padding ->
        when (val state = uiState) {
            is CategoriesUiState.Loading -> LoadingState()
            is CategoriesUiState.Empty -> EmptyState()
            is CategoriesUiState.Success -> CategoriesList(state.categories)
            is CategoriesUiState.Error -> ErrorState(state.message)
        }
    }
}
```

---

### 2. Domain Layer (Бизнес-логика)

**Ответственность:**
- Бизнес-правила приложения
- Определение контрактов данных
- Изолированные операции (Use Cases)
- Независимость от фреймворков

**Компоненты:**

#### Domain Models
Чистые data классы без зависимостей.

```kotlin
data class Category(
    val id: Long,
    val name: String,
    val color: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Note(
    val id: Long,
    val categoryId: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

#### Repository Interfaces
Контракты для получения данных.

```kotlin
interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    fun getCategoryById(id: Long): Flow<Category?>
    suspend fun createCategory(name: String, color: String): Result<Long>
    suspend fun updateCategory(id: Long, name: String, color: String): Result<Unit>
    suspend fun deleteCategory(id: Long): Result<Unit>
}
```

**Принципы:**
- **Dependency Inversion** — зависимость от абстракций
- **Interface Segregation** — узкие специализированные интерфейсы

#### Use Cases
Одна операция = один Use Case.

```kotlin
class CreateCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(name: String, color: String): Result<Long> {
        // Валидация бизнес-правил
        if (name.isBlank()) {
            return Result.failure(Exception("Category name cannot be empty"))
        }
        
        // Делегирование репозиторию
        return repository.createCategory(name, color)
    }
}
```

**Паттерн оператора invoke:**
```kotlin
// Вместо: useCase.execute(params)
// Можно:   useCase(params)
val result = createCategoryUseCase("Work", "#FF5722")
```

**Примеры Use Cases:**

**Категории:**
- `GetCategoriesUseCase` — получение списка
- `CreateCategoryUseCase` — создание с валидацией
- `UpdateCategoryUseCase` — обновление
- `DeleteCategoryUseCase` — удаление с проверками

**Заметки:**
- `GetNotesByCategoryUseCase` — получение по категории
- `SearchNotesUseCase` — поиск с фильтрацией
- `CreateNoteUseCase` — создание
- `UpdateNoteUseCase` — обновление
- `DeleteNoteUseCase` — удаление

---

### 3. Data Layer (Источники данных)

**Ответственность:**
- Получение и сохранение данных
- Преобразование форматов (Entity ↔ Domain)
- Кеширование (если нужно)
- Работа с БД, сетью, файлами

**Компоненты:**

#### Repository Implementation
Реализация интерфейсов из Domain.

```kotlin
class CategoryRepositoryImpl(
    private val localDataSource: CategoryLocalDataSource
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return localDataSource.getCategories()
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun createCategory(name: String, color: String): Result<Long> {
        return try {
            val id = localDataSource.insertCategory(name, color)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### Local Data Source
Прямая работа с SQLDelight.

```kotlin
class CategoryLocalDataSource(
    private val database: NotesDatabase
) {
    fun getCategories(): Flow<List<CategoryEntity>> {
        return database.categoryQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun insertCategory(name: String, color: String): Long {
        return withContext(Dispatchers.IO) {
            database.categoryQueries.insert(
                name = name,
                color = color,
                created_at = Clock.System.now().toEpochMilliseconds(),
                updated_at = Clock.System.now().toEpochMilliseconds()
            )
            database.categoryQueries.lastInsertRowId().executeAsOne()
        }
    }
}
```

#### Mappers
Преобразование Entity ↔ Domain Model.

```kotlin
// Entity (из SQLDelight) → Domain Model
fun ru.macdroid.subagentstest.database.Category.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        color = color ?: "#000000",
        createdAt = Instant.fromEpochMilliseconds(created_at),
        updatedAt = Instant.fromEpochMilliseconds(updated_at)
    )
}
```

---

## 🔌 Dependency Injection (Koin)

### Модульная организация

Каждая фича имеет свой Koin модуль:

```kotlin
val categoriesModule = module {
    // Repository - singleton
    single<CategoryRepository> { 
        CategoryRepositoryImpl(localDataSource = get()) 
    }
    
    // Use Cases - factory (новый экземпляр каждый раз)
    factory { GetCategoriesUseCase(repository = get()) }
    factory { CreateCategoryUseCase(repository = get()) }
    factory { UpdateCategoryUseCase(repository = get()) }
    factory { DeleteCategoryUseCase(repository = get()) }
    
    // ViewModel
    viewModel { 
        CategoriesViewModel(
            getCategoriesUseCase = get(),
            createCategoryUseCase = get(),
            deleteCategoryUseCase = get()
        ) 
    }
}
```

### Инициализация

```kotlin
fun initKoin(platformModule: Module) {
    startKoin {
        modules(
            platformModule,      // Платформенно-специфичные зависимости
            coreModule,          // Основные утилиты
            databaseModule,      // SQLDelight
            categoriesModule,    // Фича категорий
            notesModule          // Фича заметок
        )
    }
}
```

### Платформенно-специфичные модули

**Android:**
```kotlin
val androidModule = module {
    single<Context> { androidContext() }
    single { DriverFactory(context = get()) }
}
```

**iOS:**
```kotlin
val iosModule = module {
    single { DriverFactory() }
}
```

**Desktop:**
```kotlin
val desktopModule = module {
    single { DriverFactory() }
}
```

**Web:**
```kotlin
val jsModule = module {
    single { DriverFactory() }
}
```

---

## 🗃️ База Данных (SQLDelight)

### Expect/Actual для Driver

**Common (expect):**
```kotlin
expect class DriverFactory {
    fun createDriver(): SqlDriver
}
```

**Android (actual):**
```kotlin
actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            NotesDatabase.Schema,
            context,
            "notes.db"
        )
    }
}
```

**iOS (actual):**
```kotlin
actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            NotesDatabase.Schema,
            "notes.db"
        )
    }
}
```

### SQL Queries

**Типобезопасные запросы:**
```sql
-- Category.sq
selectAll:
SELECT * FROM Category
ORDER BY created_at DESC;

insert:
INSERT INTO Category(name, color, created_at, updated_at)
VALUES (?, ?, ?, ?);
```

**Использование:**
```kotlin
// Генерируется SQLDelight
database.categoryQueries.selectAll().asFlow()
database.categoryQueries.insert(name, color, createdAt, updatedAt)
```

---

## 🔀 Навигация

### Простая навигация через State

```kotlin
sealed class Screen {
    data object Categories : Screen()
    data class Notes(val categoryId: Long) : Screen()
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Categories) }
    
    when (val screen = currentScreen) {
        is Screen.Categories -> {
            CategoriesListScreen(
                onNavigateToNotes = { categoryId ->
                    currentScreen = Screen.Notes(categoryId)
                }
            )
        }
        is Screen.Notes -> {
            NotesListScreen(
                categoryId = screen.categoryId,
                onBackClick = { currentScreen = Screen.Categories }
            )
        }
    }
}
```

**Преимущества:**
- Простота реализации
- Типобезопасность
- Легко тестировать
- Подходит для небольших приложений

---

## 🧪 Тестирование

### Unit Tests для Use Cases

```kotlin
class CreateCategoryUseCaseTest {
    
    private lateinit var repository: CategoryRepository
    private lateinit var useCase: CreateCategoryUseCase
    
    @BeforeTest
    fun setup() {
        repository = mockk()
        useCase = CreateCategoryUseCase(repository)
    }
    
    @Test
    fun `create category with valid name returns success`() = runTest {
        // Given
        val name = "Work"
        val color = "#FF5722"
        coEvery { repository.createCategory(name, color) } returns Result.success(1L)
        
        // When
        val result = useCase(name, color)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }
    
    @Test
    fun `create category with empty name returns failure`() = runTest {
        // Given
        val name = ""
        val color = "#FF5722"
        
        // When
        val result = useCase(name, color)
        
        // Then
        assertTrue(result.isFailure)
    }
}
```

### UI Tests (Compose)

```kotlin
@Test
fun categoriesListDisplaysCategories() = runComposeUiTest {
    val testCategories = listOf(
        Category(1, "Work", "#FF5722", Clock.System.now(), Clock.System.now()),
        Category(2, "Personal", "#2196F3", Clock.System.now(), Clock.System.now())
    )
    
    setContent {
        CategoriesList(
            categories = testCategories,
            onCategoryClick = {},
            onDeleteClick = {}
        )
    }
    
    onNodeWithText("Work").assertIsDisplayed()
    onNodeWithText("Personal").assertIsDisplayed()
}
```

---

## 📋 Best Practices

### 1. Разделение ответственности

✅ **Правильно:**
```kotlin
// ViewModel - управление состоянием
class CategoriesViewModel(private val useCase: GetCategoriesUseCase) {
    fun loadCategories() {
        viewModelScope.launch {
            useCase().collect { _uiState.value = Success(it) }
        }
    }
}

// UI - только отображение
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel) {
    val state by viewModel.uiState.collectAsState()
    // Render UI
}
```

❌ **Неправильно:**
```kotlin
@Composable
fun CategoriesScreen() {
    val categories = remember { mutableStateOf<List<Category>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        // Бизнес-логика в UI - плохо!
        database.categoryQueries.selectAll().collect {
            categories.value = it
        }
    }
}
```

### 2. Immutable Data

✅ **Правильно:**
```kotlin
data class Category(
    val id: Long,
    val name: String
)

// Изменение через copy
val updated = category.copy(name = "New Name")
```

❌ **Неправильно:**
```kotlin
data class Category(
    val id: Long,
    var name: String  // var - изменяемое состояние
)

category.name = "New Name"  // Мутация
```

### 3. Error Handling

✅ **Правильно:**
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

suspend fun createCategory(name: String): Result<Long> {
    return try {
        val id = repository.create(name)
        Result.Success(id)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

### 4. Coroutines

✅ **Правильно:**
```kotlin
viewModelScope.launch {
    getCategoriesUseCase()
        .catch { e -> handleError(e) }
        .collect { categories -> updateState(categories) }
}
```

❌ **Неправильно:**
```kotlin
GlobalScope.launch {  // Утечка памяти!
    val categories = repository.getAll()
}
```

---

## 🎯 Принципы SOLID

### Single Responsibility Principle
Каждый класс имеет одну ответственность:
- `UseCase` — одна бизнес-операция
- `Repository` — работа с данными
- `ViewModel` — управление состоянием UI

### Open/Closed Principle
Расширение через новые Use Cases без изменения существующих.

### Liskov Substitution Principle
Любая реализация `Repository` может заменить интерфейс.

### Interface Segregation Principle
Узкие интерфейсы: `CategoryRepository`, `NoteRepository` вместо одного большого.

### Dependency Inversion Principle
Зависимость от абстракций (`Repository interface`), а не от реализаций.

---

## 📚 Ресурсы

- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Koin DI](https://insert-koin.io/)

---

**Архитектура создана для масштабируемости, тестируемости и поддерживаемости кода.**

