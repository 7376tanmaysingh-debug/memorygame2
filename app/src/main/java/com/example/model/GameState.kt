package com.example.model

import com.example.data.UserPreferences

data class GameState(
    val screenState: ScreenState = ScreenState.HOME,
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val selectedCardTheme: CardTheme = CardTheme.ANIMALS,
    val cards: List<MemoryCard> = emptyList(),
    val firstSelectedCardId: Int? = null,
    val secondSelectedCardId: Int? = null,
    val isProcessing: Boolean = false,
    val moves: Int = 0,
    val score: Int = 0,
    val timeSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val pairsMatched: Int = 0,
    val comboStreak: Int = 0,
    val isPaused: Boolean = false,
    val isVictory: Boolean = false,
    val isNewHighScore: Boolean = false,
    val userPreferences: UserPreferences = UserPreferences(),
    val showSettingsDialog: Boolean = false
) {
    val totalPairs: Int get() = selectedDifficulty.pairCount
    val isGameInProgress: Boolean get() = screenState == ScreenState.GAME && !isVictory
}
