package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.model.Difficulty
import com.example.model.GameState
import com.example.ui.components.GameStatsHeader
import com.example.ui.components.MemoryCardView

@Composable
fun GameScreen(
    state: GameState,
    onCardClicked: (Int) -> Unit,
    onHomeClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button to pause or prompt exit
    BackHandler {
        if (!state.isPaused && !state.isVictory) {
            onPauseClick()
        } else {
            onHomeClick()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with stats & controls
            GameStatsHeader(
                timeSeconds = state.timeSeconds,
                moves = state.moves,
                score = state.score,
                pairsMatched = state.pairsMatched,
                totalPairs = state.totalPairs,
                comboStreak = state.comboStreak,
                cardTheme = state.selectedCardTheme,
                isSoundEnabled = state.userPreferences.soundEnabled,
                onHomeClick = onHomeClick,
                onPauseClick = onPauseClick,
                onRestartClick = onRestartClick,
                onToggleSound = onToggleSound
            )

            // Responsive Card Grid Area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val cols = state.selectedDifficulty.cols
                val rows = state.selectedDifficulty.rows

                val spacing = when (state.selectedDifficulty) {
                    Difficulty.HARD -> 6.dp
                    Difficulty.MEDIUM -> 8.dp
                    Difficulty.EASY -> 10.dp
                }

                val totalHorizontalSpacing = spacing * (cols - 1)
                val totalVerticalSpacing = spacing * (rows - 1)

                val availW = maxWidth - totalHorizontalSpacing
                val availH = maxHeight - totalVerticalSpacing

                val cellW = availW / cols
                val cellH = availH / rows

                // Determine balanced card width & height
                val cardWidth: androidx.compose.ui.unit.Dp
                val cardHeight: androidx.compose.ui.unit.Dp

                if (cellW * 1.25f <= cellH) {
                    // Taller screen - maintain aspect ratio
                    cardWidth = cellW
                    cardHeight = min(cellH, cellW * 1.28f)
                } else if (cellH <= cellW) {
                    // Flatter / square screen
                    cardHeight = cellH
                    cardWidth = min(cellW, cellH * 0.88f)
                } else {
                    cardWidth = cellW
                    cardHeight = cellH
                }

                val fontSize = when (state.selectedDifficulty) {
                    Difficulty.HARD -> 22f
                    Difficulty.MEDIUM -> 28f
                    Difficulty.EASY -> 34f
                }

                val cornerRadius = when (state.selectedDifficulty) {
                    Difficulty.HARD -> 8.dp
                    Difficulty.MEDIUM -> 11.dp
                    Difficulty.EASY -> 14.dp
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (r in 0 until rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (c in 0 until cols) {
                                val cardIndex = r * cols + c
                                if (cardIndex < state.cards.size) {
                                    val card = state.cards[cardIndex]
                                    MemoryCardView(
                                        card = card,
                                        cardCornerRadius = cornerRadius,
                                        fontSize = fontSize,
                                        onClick = { onCardClicked(card.id) },
                                        modifier = Modifier.size(cardWidth, cardHeight)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(cardWidth, cardHeight))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Pause Overlay Dialog
        if (state.isPaused) {
            PauseDialog(
                onResume = onResumeClick,
                onRestart = onRestartClick,
                onHome = onHomeClick
            )
        }

        // Victory Overlay Dialog
        if (state.isVictory) {
            val record = state.userPreferences.records[state.selectedDifficulty]
            VictoryDialog(
                score = state.score,
                timeSeconds = state.timeSeconds,
                moves = state.moves,
                bestScore = record?.bestScore ?: state.score,
                isNewRecord = state.isNewHighScore,
                difficultyName = state.selectedDifficulty.displayName,
                onPlayAgain = onRestartClick,
                onHome = onHomeClick
            )
        }
    }
}
