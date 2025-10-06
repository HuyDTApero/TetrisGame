package com.example.tetrisgame.data.models

import androidx.compose.ui.graphics.Color

enum class GameLevel {
    CLASSIC,
    SPEED,
    CHALLENGE
}

data class LevelConfig(
    val level: GameLevel,
    val boardWidth: Int,
    val boardHeight: Int,
    val initialSpeed: Long,
    val speedDecrement: Long,
    val minSpeed: Long,
    val linesPerLevel: Int,
    val hardDropMultiplier: Int,
    val hasObstacles: Boolean,
    val obstacleFrequency: Int, // Every N lines add obstacles
    val theme: LevelTheme
) {
    companion object {
        fun getConfig(level: GameLevel): LevelConfig {
            return when (level) {
                GameLevel.CLASSIC -> LevelConfig(
                    level = GameLevel.CLASSIC,
                    boardWidth = 10,
                    boardHeight = 20,
                    initialSpeed = 1000L,
                    speedDecrement = 100L,
                    minSpeed = 100L,
                    linesPerLevel = 10,
                    hardDropMultiplier = 1,
                    hasObstacles = false,
                    obstacleFrequency = 0,
                    theme = LevelTheme.DARK
                )
                GameLevel.SPEED -> LevelConfig(
                    level = GameLevel.SPEED,
                    boardWidth = 10,
                    boardHeight = 18,
                    initialSpeed = 500L,
                    speedDecrement = 50L,
                    minSpeed = 50L,
                    linesPerLevel = 10,
                    hardDropMultiplier = 2,
                    hasObstacles = false,
                    obstacleFrequency = 0,
                    theme = LevelTheme.GRADIENT
                )
                GameLevel.CHALLENGE -> LevelConfig(
                    level = GameLevel.CHALLENGE,
                    boardWidth = 12,
                    boardHeight = 22,
                    initialSpeed = 700L,
                    speedDecrement = 70L,
                    minSpeed = 150L,
                    linesPerLevel = 10,
                    hardDropMultiplier = 1,
                    hasObstacles = true,
                    obstacleFrequency = 5, // Add obstacles every 5 lines
                    theme = LevelTheme.MATRIX
                )
            }
        }
    }
}

data class LevelTheme(
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val backgroundColor: Color,
    val showParticles: Boolean,
    val showMatrixRain: Boolean,
    val showGradient: Boolean
) {
    companion object {
        val DARK = LevelTheme(
            name = "Dark Classic",
            primaryColor = Color(0xFF00FFFF), // Cyan
            secondaryColor = Color(0xFFFF006E), // Pink
            accentColor = Color(0xFF00FF41), // Green
            backgroundColor = Color(0xFF0A0A0A),
            showParticles = false,
            showMatrixRain = false,
            showGradient = false
        )

        val GRADIENT = LevelTheme(
            name = "Speed Gradient",
            primaryColor = Color(0xFFFF6B35), // Orange
            secondaryColor = Color(0xFFF7931E), // Yellow-Orange
            accentColor = Color(0xFFFFFF00), // Yellow
            backgroundColor = Color(0xFF1A0A2E),
            showParticles = true,
            showMatrixRain = false,
            showGradient = true
        )

        val MATRIX = LevelTheme(
            name = "Matrix Challenge",
            primaryColor = Color(0xFF00FF41), // Matrix Green
            secondaryColor = Color(0xFF00D4FF), // Cyan
            accentColor = Color(0xFFBF40BF), // Purple
            backgroundColor = Color(0xFF000000),
            showParticles = true,
            showMatrixRain = true,
            showGradient = false
        )
    }
}
