package com.example.tetrisgame.data.models

import kotlinx.serialization.Serializable

/**
 * Data class for a high score entry
 */
@Serializable
data class HighScoreEntry(
    val score: Int,
    val lines: Int,
    val level: Int,
    val date: String
)
