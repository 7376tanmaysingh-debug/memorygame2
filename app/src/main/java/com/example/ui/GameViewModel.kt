package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.GamePreferencesRepository
import com.example.model.AppThemeMode
import com.example.model.CardTheme
import com.example.model.Difficulty
import com.example.model.GameState
import com.example.model.MemoryCard
import com.example.model.ScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application.applicationContext)
    private val soundManager = SoundManager()
    private val hapticManager = HapticManager(application.applicationContext)

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { prefs ->
                soundManager.isSoundEnabled = prefs.soundEnabled
                hapticManager.isHapticsEnabled = prefs.hapticsEnabled
                _uiState.update {
                    it.copy(
                        userPreferences = prefs,
                        selectedCardTheme = prefs.cardTheme
                    )
                }
            }
        }
    }

    fun playClickSound() {
        soundManager.playClick()
        hapticManager.vibrateTap()
    }

    fun selectDifficulty(difficulty: Difficulty) {
        playClickSound()
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun selectCardTheme(theme: CardTheme) {
        playClickSound()
        _uiState.update { it.copy(selectedCardTheme = theme) }
        viewModelScope.launch {
            repository.setCardTheme(theme)
        }
    }

    fun startGame(
        difficulty: Difficulty = _uiState.value.selectedDifficulty,
        theme: CardTheme = _uiState.value.selectedCardTheme
    ) {
        playClickSound()
        stopTimer()
        val cards = generateCards(difficulty, theme)
        _uiState.update {
            it.copy(
                screenState = ScreenState.GAME,
                selectedDifficulty = difficulty,
                selectedCardTheme = theme,
                cards = cards,
                firstSelectedCardId = null,
                secondSelectedCardId = null,
                isProcessing = false,
                moves = 0,
                score = 0,
                timeSeconds = 0,
                isTimerRunning = false,
                pairsMatched = 0,
                comboStreak = 0,
                isPaused = false,
                isVictory = false,
                isNewHighScore = false
            )
        }
    }

    private fun generateCards(difficulty: Difficulty, theme: CardTheme): List<MemoryCard> {
        val pool = theme.emojis.shuffled()
        val selectedEmojis = pool.take(difficulty.pairCount)
        val cardsList = mutableListOf<MemoryCard>()
        var idCounter = 0

        selectedEmojis.forEachIndexed { pairIndex, emoji ->
            cardsList.add(
                MemoryCard(
                    id = idCounter++,
                    pairId = pairIndex,
                    emoji = emoji,
                    isFaceUp = false,
                    isMatched = false
                )
            )
            cardsList.add(
                MemoryCard(
                    id = idCounter++,
                    pairId = pairIndex,
                    emoji = emoji,
                    isFaceUp = false,
                    isMatched = false
                )
            )
        }
        return cardsList.shuffled()
    }

    fun onCardClicked(cardId: Int) {
        val currentState = _uiState.value
        if (currentState.isProcessing || currentState.isPaused || currentState.isVictory) {
            return
        }

        val card = currentState.cards.find { it.id == cardId } ?: return
        if (card.isFaceUp || card.isMatched) {
            return
        }

        // Start timer on very first card interaction if not running
        if (!currentState.isTimerRunning) {
            startTimer()
        }

        // Tactile clicking sound and flip effect
        soundManager.playCardFlip()
        soundManager.playClick()
        hapticManager.vibrateTap()

        if (currentState.firstSelectedCardId == null) {
            // First card selected
            _uiState.update { state ->
                val updatedCards = state.cards.map {
                    if (it.id == cardId) it.copy(isFaceUp = true) else it
                }
                state.copy(
                    cards = updatedCards,
                    firstSelectedCardId = cardId
                )
            }
        } else {
            // Second card selected
            val firstCardId = currentState.firstSelectedCardId
            val firstCard = currentState.cards.find { it.id == firstCardId } ?: return

            val updatedCards = currentState.cards.map {
                if (it.id == cardId) it.copy(isFaceUp = true) else it
            }

            val newMoves = currentState.moves + 1

            _uiState.update {
                it.copy(
                    cards = updatedCards,
                    secondSelectedCardId = cardId,
                    moves = newMoves,
                    isProcessing = true
                )
            }

            viewModelScope.launch {
                if (firstCard.pairId == card.pairId) {
                    // Match found!
                    handleMatch(firstCardId, cardId)
                } else {
                    // Mismatch
                    handleMismatch(firstCardId, cardId)
                }
            }
        }
    }

    private suspend fun handleMatch(firstId: Int, secondId: Int) {
        val state = _uiState.value
        val mult = state.selectedDifficulty.scoreMultiplier
        val combo = state.comboStreak + 1
        val pointsEarned = ((100 * mult) + (combo * 30 * mult)).toInt()
        val newScore = state.score + pointsEarned
        val newPairsMatched = state.pairsMatched + 1

        soundManager.playMatch()
        hapticManager.vibrateMatch()

        _uiState.update { curr ->
            val matchedCards = curr.cards.map {
                if (it.id == firstId || it.id == secondId) {
                    it.copy(isMatched = true, isFaceUp = true)
                } else it
            }
            curr.copy(
                cards = matchedCards,
                score = newScore,
                pairsMatched = newPairsMatched,
                comboStreak = combo,
                firstSelectedCardId = null,
                secondSelectedCardId = null,
                isProcessing = false
            )
        }

        if (newPairsMatched == state.totalPairs) {
            handleVictory()
        }
    }

    private suspend fun handleMismatch(firstId: Int, secondId: Int) {
        soundManager.playMismatch()
        hapticManager.vibrateMismatch()

        _uiState.update { curr ->
            val cardsWithMismatch = curr.cards.map {
                if (it.id == firstId || it.id == secondId) {
                    it.copy(isMismatched = true)
                } else it
            }
            val decrementedScore = max(0, curr.score - 10)
            curr.copy(
                cards = cardsWithMismatch,
                score = decrementedScore,
                comboStreak = 0
            )
        }

        // Delay so player can memorize the cards
        delay(850)

        _uiState.update { curr ->
            val resetCards = curr.cards.map {
                if (it.id == firstId || it.id == secondId) {
                    it.copy(isFaceUp = false, isMismatched = false)
                } else it
            }
            curr.copy(
                cards = resetCards,
                firstSelectedCardId = null,
                secondSelectedCardId = null,
                isProcessing = false
            )
        }
    }

    private fun handleVictory() {
        stopTimer()
        val state = _uiState.value
        val mult = state.selectedDifficulty.scoreMultiplier
        val pairCount = state.totalPairs

        // Bonus for low moves and quick time
        val movePar = pairCount * 2
        val moveBonus = max(0, (movePar - state.moves)) * 40
        val timePar = pairCount * 12
        val timeBonus = max(0, (timePar - state.timeSeconds)) * 15
        val totalVictoryBonus = ((moveBonus + timeBonus) * mult).toInt()
        val finalScore = state.score + totalVictoryBonus

        soundManager.playVictory()
        hapticManager.vibrateVictory()

        viewModelScope.launch {
            val isNewBest = repository.updateBestRecord(
                difficulty = state.selectedDifficulty,
                score = finalScore,
                timeSeconds = state.timeSeconds
            )

            _uiState.update {
                it.copy(
                    score = finalScore,
                    isVictory = true,
                    isNewHighScore = isNewBest
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    if (state.isTimerRunning && !state.isPaused && !state.isVictory) {
                        state.copy(timeSeconds = state.timeSeconds + 1)
                    } else state
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun pauseGame() {
        playClickSound()
        if (_uiState.value.screenState == ScreenState.GAME && !_uiState.value.isVictory) {
            _uiState.update { it.copy(isPaused = true) }
        }
    }

    fun resumeGame() {
        playClickSound()
        _uiState.update { it.copy(isPaused = false) }
    }

    fun restartGame() {
        playClickSound()
        startGame(_uiState.value.selectedDifficulty, _uiState.value.selectedCardTheme)
    }

    fun navigateToHome() {
        playClickSound()
        stopTimer()
        _uiState.update {
            it.copy(
                screenState = ScreenState.HOME,
                isPaused = false,
                isVictory = false
            )
        }
    }

    fun toggleSound() {
        val next = !_uiState.value.userPreferences.soundEnabled
        soundManager.isSoundEnabled = next
        if (next) {
            playClickSound()
        }
        viewModelScope.launch {
            repository.setSoundEnabled(next)
        }
    }

    fun toggleHaptics() {
        playClickSound()
        val next = !_uiState.value.userPreferences.hapticsEnabled
        hapticManager.isHapticsEnabled = next
        viewModelScope.launch {
            repository.setHapticsEnabled(next)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        playClickSound()
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun resetHighScores() {
        playClickSound()
        viewModelScope.launch {
            repository.clearRecords()
        }
    }

    fun showSettings(show: Boolean) {
        playClickSound()
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
