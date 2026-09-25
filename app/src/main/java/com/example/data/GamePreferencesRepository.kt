package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppThemeMode
import com.example.model.CardTheme
import com.example.model.Difficulty
import com.example.model.DifficultyBestRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "memory_match_prefs")

data class UserPreferences(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val cardTheme: CardTheme = CardTheme.ANIMALS,
    val records: Map<Difficulty, DifficultyBestRecord> = emptyMap()
)

class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CARD_THEME = stringPreferencesKey("card_theme")

        fun bestScore(diff: Difficulty) = intPreferencesKey("best_score_${diff.id}")
        fun bestTime(diff: Difficulty) = intPreferencesKey("best_time_${diff.id}")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val sound = preferences[Keys.SOUND_ENABLED] ?: true
        val haptics = preferences[Keys.HAPTICS_ENABLED] ?: true
        val themeStr = preferences[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val theme = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)

        val cardThemeStr = preferences[Keys.CARD_THEME] ?: CardTheme.ANIMALS.id
        val cardTheme = CardTheme.fromId(cardThemeStr)

        val records = Difficulty.entries.associateWith { diff ->
            val score = preferences[Keys.bestScore(diff)] ?: 0
            val time = preferences[Keys.bestTime(diff)] ?: 0
            DifficultyBestRecord(bestScore = score, bestTimeSeconds = time)
        }

        UserPreferences(
            soundEnabled = sound,
            hapticsEnabled = haptics,
            themeMode = theme,
            cardTheme = cardTheme,
            records = records
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setCardTheme(theme: CardTheme) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CARD_THEME] = theme.id
        }
    }

    suspend fun updateBestRecord(difficulty: Difficulty, score: Int, timeSeconds: Int): Boolean {
        var isNewBest = false
        context.dataStore.edit { preferences ->
            val currentBestScore = preferences[Keys.bestScore(difficulty)] ?: 0
            val currentBestTime = preferences[Keys.bestTime(difficulty)] ?: 0

            if (score > currentBestScore) {
                preferences[Keys.bestScore(difficulty)] = score
                isNewBest = true
            }
            if (currentBestTime == 0 || timeSeconds < currentBestTime) {
                preferences[Keys.bestTime(difficulty)] = timeSeconds
            }
        }
        return isNewBest
    }

    suspend fun clearRecords() {
        context.dataStore.edit { preferences ->
            Difficulty.entries.forEach { diff ->
                preferences.remove(Keys.bestScore(diff))
                preferences.remove(Keys.bestTime(diff))
            }
        }
    }
}
