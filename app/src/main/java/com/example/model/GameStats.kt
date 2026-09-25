package com.example.model

data class DifficultyBestRecord(
    val bestScore: Int = 0,
    val bestTimeSeconds: Int = 0
)

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ScreenState {
    HOME,
    GAME
}
