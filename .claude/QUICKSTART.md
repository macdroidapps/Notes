# SubAgentsTest - Quick Start Guide

Быстрая шпаргалка для работы с проектом.

## 🚀 Запуск

```bash
# Android
./gradlew :composeApp:installDebug

# Desktop
./gradlew :composeApp:runDebugExecutableDesktop

# Web
./gradlew :composeApp:jsBrowserDevelopmentRun

# iOS (требует Xcode)
./gradlew :composeApp:iosSimulatorArm64MainBinaries
```

---

## 📂 Структура Фичи

```
features/[feature-name]/
├── data/
│   ├── local/              # Data Source (SQLDelight)
│   └── repository/         # Repository Implementation
├── domain/
│   ├── model/             # Domain Models
│   ├── repository/        # Repository Interface
│   └── usecase/           # Use Cases
├── presentation/
│   ├── list/              # List Screen
│   └── viewmodel/         # ViewModel
└── di/                    # Koin Module
```

---

## 🔨 Добавление новой фичи

### 1. Domain Layer

**Модель:**
```kotlin
// domain/model/Item.kt
data class Item(
    val id: Long,
    val name: String,
    val createdAt: Instant
)
```

**Repository Interface:**
```kotlin
// domain/repository/ItemRepository.kt
interface ItemRepository {
    fun getItems(): Flow<List<Item>>
    suspend fun createItem(name: String): Result<Long>
}
```

**Use Case:**
```kotlin
// domain/usecase/GetItemsUseCase.kt
class GetItemsUseCase(private val repository: ItemRepository) {
    operator fun invoke(): Flow<List<Item>> = repository.getItems()
}
```

### 2. Data Layer

**SQL Schema:**
```sql
-- sqldelight/Item.sq
CREATE TABLE Item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    created_at INTEGER NOT NULL
);

selectAll:
SELECT * FROM Item;

insert:
INSERT INTO Item(name, created_at) VALUES (?, ?);
```

**Data Source:**
```kotlin
// data/local/ItemLocalDataSource.kt
class ItemLocalDataSource(private val database: NotesDatabase) {
    fun getItems(): Flow<List<ItemEntity>> {
        return database.itemQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
}
```

**Repository Implementation:**
```kotlin
// data/repository/ItemRepositoryImpl.kt
class ItemRepositoryImpl(
    private val localDataSource: ItemLocalDataSource
) : ItemRepository {
    override fun getItems(): Flow<List<Item>> {
        return localDataSource.getItems()
            .map { entities -> entities.map { it.toDomainModel() } }
    }
}
```

### 3. Presentation Layer

**UI State:**
```kotlin
// presentation/viewmodel/ItemsViewModel.kt
sealed interface ItemsUiState {
    data object Loading : ItemsUiState
    data object Empty : ItemsUiState
    data class Success(val items: List<Item>) : ItemsUiState
    data class Error(val message: String) : ItemsUiState
}
```

**ViewModel:**
```kotlin
class ItemsViewModel(
    private val getItemsUseCase: GetItemsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ItemsUiState>(ItemsUiState.Loading)
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()
    
    init {
        loadItems()
    }
    
    private fun loadItems() {
        viewModelScope.launch {
            getItemsUseCase()
                .catch { e -> _uiState.value = ItemsUiState.Error(e.message ?: "Error") }
                .collect { items ->
                    _uiState.value = if (items.isEmpty()) {
                        ItemsUiState.Empty
                    } else {
                        ItemsUiState.Success(items)
                    }
                }
        }
    }
}
```

**Screen:**
```kotlin
// presentation/list/ItemsScreen.kt
@Composable
fun ItemsScreen(
    viewModel: ItemsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is ItemsUiState.Loading -> LoadingIndicator()
        is ItemsUiState.Empty -> EmptyState()
        is ItemsUiState.Success -> ItemsList(state.items)
        is ItemsUiState.Error -> ErrorState(state.message)
    }
}
```

### 4. Dependency Injection

```kotlin
// di/ItemsModule.kt
val itemsModule = module {
    // Data Source
    single { ItemLocalDataSource(database = get()) }
    
    // Repository
    single<ItemRepository> { 
        ItemRepositoryImpl(localDataSource = get()) 
    }
    
    // Use Cases
    factory { GetItemsUseCase(repository = get()) }
    factory { CreateItemUseCase(repository = get()) }
    
    // ViewModel
    viewModel { ItemsViewModel(getItemsUseCase = get()) }
}
```

**Регистрация модуля:**
```kotlin
// core/di/KoinInitializer.kt
fun initKoin(platformModule: Module) {
    startKoin {
        modules(
            platformModule,
            coreModule,
            databaseModule,
            categoriesModule,
            notesModule,
            itemsModule  // ← Добавить сюда
        )
    }
}
```

---

## 🎨 UI Компоненты

### Базовые состояния

```kotlin
@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(message: String = "Нет данных") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = Color.Red)
        }
    }
}
```

### Список

```kotlin
@Composable
fun ItemsList(items: List<Item>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items) { item ->
            ItemCard(
                item = item,
                onClick = { /* handle click */ }
            )
        }
    }
}
```

### Карточка

```kotlin
@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                item.createdAt.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
```

---

## 🗄️ SQLDelight Queries

### Основные операции

```sql
-- Выборка всех записей
selectAll:
SELECT * FROM Item ORDER BY created_at DESC;

-- Выборка по ID
selectById:
SELECT * FROM Item WHERE id = :id;

-- Вставка
insert:
INSERT INTO Item(name, created_at) VALUES (?, ?);

-- Обновление
update:
UPDATE Item SET name = :name WHERE id = :id;

-- Удаление
deleteById:
DELETE FROM Item WHERE id = :id;

-- Подсчет
count:
SELECT COUNT(*) FROM Item;
```

### Сложные запросы

```sql
-- Поиск
search:
SELECT * FROM Item
WHERE name LIKE '%' || :query || '%'
ORDER BY created_at DESC;

-- Join
selectWithDetails:
SELECT Item.*, Category.name AS category_name
FROM Item
INNER JOIN Category ON Item.category_id = Category.id;

-- Группировка
countByCategory:
SELECT category_id, COUNT(*) as count
FROM Item
GROUP BY category_id;
```

---

## 🧪 Тестирование

### Use Case Test

```kotlin
class GetItemsUseCaseTest {
    private lateinit var repository: ItemRepository
    private lateinit var useCase: GetItemsUseCase
    
    @BeforeTest
    fun setup() {
        repository = mockk()
        useCase = GetItemsUseCase(repository)
    }
    
    @Test
    fun `should return items from repository`() = runTest {
        // Given
        val testItems = listOf(Item(1, "Test", Clock.System.now()))
        every { repository.getItems() } returns flowOf(testItems)
        
        // When
        val result = useCase().first()
        
        // Then
        assertEquals(testItems, result)
    }
}
```

### ViewModel Test

```kotlin
class ItemsViewModelTest {
    private lateinit var getItemsUseCase: GetItemsUseCase
    private lateinit var viewModel: ItemsViewModel
    
    @BeforeTest
    fun setup() {
        getItemsUseCase = mockk()
        viewModel = ItemsViewModel(getItemsUseCase)
    }
    
    @Test
    fun `loading state should be initial`() = runTest {
        assertTrue(viewModel.uiState.value is ItemsUiState.Loading)
    }
    
    @Test
    fun `should show success state with items`() = runTest {
        // Given
        val testItems = listOf(Item(1, "Test", Clock.System.now()))
        coEvery { getItemsUseCase() } returns flowOf(testItems)
        
        // When
        viewModel.loadItems()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is ItemsUiState.Success)
        assertEquals(testItems, (state as ItemsUiState.Success).items)
    }
}
```

---

## 🎯 Common Patterns

### Result Wrapper

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

// Использование
suspend fun createItem(name: String): Result<Long> {
    return try {
        val id = repository.create(name)
        Result.Success(id)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

### State Flow Pattern

```kotlin
class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()
}
```

### Mapper Pattern

```kotlin
// Entity → Domain
fun ItemEntity.toDomainModel(): Item {
    return Item(
        id = id,
        name = name,
        createdAt = Instant.fromEpochMilliseconds(created_at)
    )
}

// Domain → Entity
fun Item.toEntity(): ItemEntity {
    return ItemEntity(
        id = id,
        name = name,
        created_at = createdAt.toEpochMilliseconds()
    )
}
```

---

## 🔧 Полезные команды

```bash
# Очистка проекта
./gradlew clean

# Сборка всех таргетов
./gradlew build

# Запуск тестов
./gradlew test

# Генерация SQLDelight кода
./gradlew generateCommonMainNotesDatabase

# Проверка зависимостей
./gradlew dependencies

# Lint проверка
./gradlew lintDebug
```

---

## 📝 Checklist для новой фичи

- [ ] Создана структура папок (data/domain/presentation/di)
- [ ] Определены Domain Models
- [ ] Создан Repository Interface
- [ ] Реализованы Use Cases
- [ ] Создана SQL схема (если нужна БД)
- [ ] Реализован Local Data Source
- [ ] Реализован Repository Implementation
- [ ] Созданы Mappers (Entity ↔ Domain)
- [ ] Создан UI State (sealed interface)
- [ ] Создан ViewModel
- [ ] Созданы Compose экраны
- [ ] Настроен Koin модуль
- [ ] Зарегистрирован модуль в KoinInitializer
- [ ] Написаны Unit тесты для Use Cases
- [ ] Написаны тесты для ViewModel
- [ ] Протестирован UI

---

## 🐛 Troubleshooting

### SQLDelight не генерирует код
```bash
./gradlew generateCommonMainNotesDatabase
```

### Koin не находит зависимость
- Проверьте регистрацию модуля в `initKoin()`
- Убедитесь что используете правильный scope (`single`, `factory`, `viewModel`)

### Ошибка компиляции iOS
- Убедитесь что установлен Xcode
- Проверьте настройки в `build.gradle.kts`

### Web приложение не запускается
```bash
./gradlew clean
./gradlew :composeApp:jsBrowserDevelopmentRun
```

---

## 📚 Документация

- [README.md](../README.md) — Полное описание проекта
- [ARCHITECTURE.md](ARCHITECTURE.md) — Архитектура и паттерны
- [KMP Prompt](../app/src/main/java/ru/macdroid/subagentstest/kmp-prompt.md) — Детальный промпт для ассистента

---

**Happy Coding! 🚀**

