package ru.macdroid.subagentstest

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import ru.macdroid.subagentstest.core.di.initKoin
import ru.macdroid.subagentstest.core.di.platformModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(platformModule)
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
