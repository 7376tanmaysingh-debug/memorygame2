package com.example.model

data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val emoji: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val isMismatched: Boolean = false
)
