package com.example

import com.example.model.CardTheme
import com.example.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLogicTest {

    @Test
    fun verifyDifficultyGridSizes() {
        assertEquals(16, Difficulty.EASY.totalCards)
        assertEquals(8, Difficulty.EASY.pairCount)

        assertEquals(20, Difficulty.MEDIUM.totalCards)
        assertEquals(10, Difficulty.MEDIUM.pairCount)

        assertEquals(36, Difficulty.HARD.totalCards)
        assertEquals(18, Difficulty.HARD.pairCount)
    }

    @Test
    fun verifyScoreMultipliers() {
        assertTrue(Difficulty.EASY.scoreMultiplier < Difficulty.MEDIUM.scoreMultiplier)
        assertTrue(Difficulty.MEDIUM.scoreMultiplier < Difficulty.HARD.scoreMultiplier)
    }

    @Test
    fun verifyCardThemesContainSufficientEmojis() {
        val maxPairsNeeded = Difficulty.HARD.pairCount // 18 pairs
        for (theme in CardTheme.entries) {
            assertTrue("Theme ${theme.name} must have at least $maxPairsNeeded emojis", theme.emojis.size >= maxPairsNeeded)
            assertEquals("Emojis in theme ${theme.name} must be unique", theme.emojis.size, theme.emojis.toSet().size)
        }
    }
}
