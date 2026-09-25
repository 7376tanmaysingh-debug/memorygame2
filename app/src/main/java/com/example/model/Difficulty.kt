package com.example.model

enum class Difficulty(
    val id: String,
    val displayName: String,
    val rows: Int,
    val cols: Int,
    val pairCount: Int,
    val badge: String,
    val scoreMultiplier: Float
) {
    EASY(
        id = "easy",
        displayName = "Easy",
        rows = 4,
        cols = 4,
        pairCount = 8,
        badge = "4×4",
        scoreMultiplier = 1.0f
    ),
    MEDIUM(
        id = "medium",
        displayName = "Medium",
        rows = 5,
        cols = 4,
        pairCount = 10,
        badge = "5×4",
        scoreMultiplier = 1.4f
    ),
    HARD(
        id = "hard",
        displayName = "Hard",
        rows = 6,
        cols = 6,
        pairCount = 18,
        badge = "6×6",
        scoreMultiplier = 2.0f
    );

    val totalCards: Int get() = rows * cols
}
