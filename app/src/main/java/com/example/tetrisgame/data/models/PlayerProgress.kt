package com.example.tetrisgame.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProgress(
    val currentLevel: String = GameLevel.CLASSIC.name, // Store as String for serialization
    val levelScores: Map<String, Int> = emptyMap(),
    val levelHighScores: Map<String, Int> = emptyMap(),
    val levelLinesCleared: Map<String, Int> = emptyMap(),
    val unlockedLevels: Set<String> = setOf(GameLevel.CLASSIC.name), // Classic unlocked by default
    val totalPlayTime: Long = 0L,
    val gamesPlayed: Int = 0
) {
    fun getCurrentLevelEnum(): GameLevel {
        return try {
            GameLevel.valueOf(currentLevel)
        } catch (e: Exception) {
            GameLevel.CLASSIC
        }
    }

    fun isLevelUnlocked(level: GameLevel): Boolean {
        return unlockedLevels.contains(level.name)
    }

    fun getHighScore(level: GameLevel): Int {
        return levelHighScores[level.name] ?: 0
    }

    fun getLinesCleared(level: GameLevel): Int {
        return levelLinesCleared[level.name] ?: 0
    }

    fun unlockNextLevel(): PlayerProgress {
        val nextLevel = when (getCurrentLevelEnum()) {
            GameLevel.CLASSIC -> GameLevel.SPEED
            GameLevel.SPEED -> GameLevel.CHALLENGE
            GameLevel.CHALLENGE -> return this // Already at max level
        }

        return copy(
            unlockedLevels = unlockedLevels + nextLevel.name
        )
    }

    fun updateScore(level: GameLevel, score: Int, lines: Int): PlayerProgress {
        val currentHighScore = levelHighScores[level.name] ?: 0
        val currentLines = levelLinesCleared[level.name] ?: 0

        return copy(
            levelScores = levelScores + (level.name to score),
            levelHighScores = if (score > currentHighScore) {
                levelHighScores + (level.name to score)
            } else {
                levelHighScores
            },
            levelLinesCleared = levelLinesCleared + (level.name to (currentLines + lines)),
            gamesPlayed = gamesPlayed + 1
        )
    }

    fun setCurrentLevel(level: GameLevel): PlayerProgress {
        return copy(currentLevel = level.name)
    }
}
