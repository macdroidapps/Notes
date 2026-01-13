package ru.macdroid.subagentstest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.koin.compose.KoinContext
import ru.macdroid.subagentstest.features.categories.presentation.list.CategoriesListScreen
import ru.macdroid.subagentstest.features.notes.presentation.list.NotesListScreen
import ru.macdroid.subagentstest.ui.theme.SubAgentsTestTheme

sealed class Screen {
    data object Categories : Screen()
    data class Notes(val categoryId: Long) : Screen()
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Categories) }

    KoinContext {
        SubAgentsTestTheme {
            when (val screen = currentScreen) {
                is Screen.Categories -> {
                    CategoriesListScreen(
                        onNavigateToNotes = { categoryId ->
                            currentScreen = Screen.Notes(categoryId)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.Notes -> {
                    NotesListScreen(
                        categoryId = screen.categoryId,
                        onBackClick = {
                            currentScreen = Screen.Categories
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
