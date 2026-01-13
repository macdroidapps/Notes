package ru.macdroid.subagentstest

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import ru.macdroid.subagentstest.core.di.initKoin
import ru.macdroid.subagentstest.core.di.platformModule

class NotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(platformModule) {
            androidLogger()
            androidContext(this@NotesApplication)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
