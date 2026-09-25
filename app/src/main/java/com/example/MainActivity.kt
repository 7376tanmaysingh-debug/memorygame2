package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ScreenState
import com.example.ui.GameViewModel
import com.example.ui.components.AppBackground
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.MemoryMatchTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MemoryMatchTheme(themeMode = state.userPreferences.themeMode) {
                AppBackground {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ) { innerPadding ->
                        GameContent(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    if (state.showSettingsDialog) {
                        SettingsDialog(
                            soundEnabled = state.userPreferences.soundEnabled,
                            hapticsEnabled = state.userPreferences.hapticsEnabled,
                            themeMode = state.userPreferences.themeMode,
                            cardTheme = state.selectedCardTheme,
                            onToggleSound = { viewModel.toggleSound() },
                            onToggleHaptics = { viewModel.toggleHaptics() },
                            onSelectTheme = { viewModel.setThemeMode(it) },
                            onSelectCardTheme = { viewModel.selectCardTheme(it) },
                            onResetScores = { viewModel.resetHighScores() },
                            onDismiss = { viewModel.showSettings(false) }
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseGame()
    }
}

@Composable
fun GameContent(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.screenState) {
        ScreenState.HOME -> {
            HomeScreen(
                selectedDifficulty = state.selectedDifficulty,
                selectedCardTheme = state.selectedCardTheme,
                userPreferences = state.userPreferences,
                onSelectDifficulty = { viewModel.selectDifficulty(it) },
                onSelectCardTheme = { viewModel.selectCardTheme(it) },
                onPlayClick = { viewModel.startGame() },
                onOpenSettings = { viewModel.showSettings(true) },
                onToggleSound = { viewModel.toggleSound() },
                modifier = modifier
            )
        }
        ScreenState.GAME -> {
            GameScreen(
                state = state,
                onCardClicked = { viewModel.onCardClicked(it) },
                onHomeClick = { viewModel.navigateToHome() },
                onPauseClick = { viewModel.pauseGame() },
                onResumeClick = { viewModel.resumeGame() },
                onRestartClick = { viewModel.restartGame() },
                onToggleSound = { viewModel.toggleSound() },
                modifier = modifier
            )
        }
    }
}
