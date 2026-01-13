package ru.macdroid.subagentstest

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.macdroid.subagentstest.core.di.initKoin
import ru.macdroid.subagentstest.core.di.platformModule

fun main() {
    initKoin(platformModule)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Notes App"
        ) {
            App()
        }
    }
}
