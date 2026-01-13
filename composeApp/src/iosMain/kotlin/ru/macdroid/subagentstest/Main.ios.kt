package ru.macdroid.subagentstest

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ru.macdroid.subagentstest.core.di.initKoin
import ru.macdroid.subagentstest.core.di.platformModule

fun MainViewController(): UIViewController {
    initKoin(platformModule)
    return ComposeUIViewController { App() }
}
