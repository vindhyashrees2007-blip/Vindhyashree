package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StoryViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf("home") }

                when (currentScreen) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToCharacter = { currentScreen = "character" },
                        onNavigateToReader = { currentScreen = "reader" },
                        onNavigateToChat = { currentScreen = "chat" },
                        onNavigateToSettings = { currentScreen = "settings" }
                    )
                    "character" -> CharacterCustomizationScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "reader" -> StoryReaderScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "chat" -> StoryChatScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                }
            }
        }
    }
}
