package com.example.tetrisgame.data.models

import kotlinx.serialization.Serializable

/**
 * Game mode types
 */
enum class GameMode {
    CLASSIC,        // Classic endless mode
    SPRINT_40,      // Clear 40 lines as fast as possible
    ULTRA_2MIN,     // Get highest score in 2 minutes
    ZEN,            // Relaxing mode, no game over
    CHALLENGE,      // Start at level 10 with obstacles
    COUNTDOWN,      // Start with 3 minutes, gain time by clearing lines
    INVISIBLE,      // Pieces disappear after placement
    CHEESE,         // Start with garbage board with holes
    // REVERSE,        // Pieces spawn from bottom, gravity upward - TODO: Complex, needs more work
    RISING_TIDE     // Garbage rises from bottom every 10 seconds
}

/**
 * Game mode configuration data
 */
data class GameModeConfig(
    val mode: GameMode,
    val name: String,
    val description: String,
    val icon: String,  // Emoji
    val objective: String,
    val difficulty: GameModeDifficulty,

    // Mode-specific settings
    val hasTimeLimit: Boolean = false,
    val timeLimitSeconds: Int = 0,
    val targetLines: Int = 0,
    val startLevel: Int = 1,
    val hasGameOver: Boolean = true,
    val speedIncreases: Boolean = true,
    val hasObstacles: Boolean = false
) {
    companion object {
        fun getConfig(mode: GameMode): GameModeConfig {
            return when (mode) {
                GameMode.CLASSIC -> GameModeConfig(
                    mode = GameMode.CLASSIC,
                    name = "Classic",
                    description = "Classic Tetris - Play until game over",
                    icon = "⭐",
                    objective = "Get the highest score",
                    difficulty = GameModeDifficulty.MEDIUM,
                    hasTimeLimit = false,
                    hasGameOver = true,
                    speedIncreases = true
                )

                GameMode.SPRINT_40 -> GameModeConfig(
                    mode = GameMode.SPRINT_40,
                    name = "Sprint 40L",
                    description = "Clear 40 lines as fast as possible",
                    icon = "🏃",
                    objective = "Clear 40 lines",
                    difficulty = GameModeDifficulty.EASY,
                    hasTimeLimit = false,
                    targetLines = 40,
                    hasGameOver = true,
                    speedIncreases = false
                )

                GameMode.ULTRA_2MIN -> GameModeConfig(
                    mode = GameMode.ULTRA_2MIN,
                    name = "Ultra 2 Min",
                    description = "Get highest score in 2 minutes",
                    icon = "⏱️",
                    objective = "Max score in 2 minutes",
                    difficulty = GameModeDifficulty.MEDIUM,
                    hasTimeLimit = true,
                    timeLimitSeconds = 120,
                    hasGameOver = false,
                    speedIncreases = false
                )

                GameMode.ZEN -> GameModeConfig(
                    mode = GameMode.ZEN,
                    name = "Zen Mode",
                    description = "Relaxing mode - No game over, no pressure",
                    icon = "🧘",
                    objective = "Relax and practice",
                    difficulty = GameModeDifficulty.EASY,
                    hasTimeLimit = false,
                    hasGameOver = false,
                    speedIncreases = false
                )

                GameMode.CHALLENGE -> GameModeConfig(
                    mode = GameMode.CHALLENGE,
                    name = "Challenge",
                    description = "Start at level 10 with random obstacles",
                    icon = "💪",
                    objective = "Survive the challenge",
                    difficulty = GameModeDifficulty.HARD,
                    hasTimeLimit = false,
                    startLevel = 10,
                    hasGameOver = true,
                    speedIncreases = true,
                    hasObstacles = true
                )

                GameMode.COUNTDOWN -> GameModeConfig(
                    mode = GameMode.COUNTDOWN,
                    name = "Countdown",
                    description = "Start with 3 min. Each line adds 5 seconds",
                    icon = "⏳",
                    objective = "Survive as long as possible",
                    difficulty = GameModeDifficulty.HARD,
                    hasTimeLimit = true,
                    timeLimitSeconds = 180,
                    hasGameOver = true,
                    speedIncreases = true
                )

                GameMode.INVISIBLE -> GameModeConfig(
                    mode = GameMode.INVISIBLE,
                    name = "Invisible",
                    description = "Placed pieces disappear - Memory challenge",
                    icon = "🌙",
                    objective = "Play by memory",
                    difficulty = GameModeDifficulty.HARD,
                    hasTimeLimit = false,
                    hasGameOver = true,
                    speedIncreases = true
                )

                GameMode.CHEESE -> GameModeConfig(
                    mode = GameMode.CHEESE,
                    name = "Cheese Mode",
                    description = "Clear 10 lines of garbage with random holes",
                    icon = "🧀",
                    objective = "Dig through the cheese",
                    difficulty = GameModeDifficulty.MEDIUM,
                    hasTimeLimit = false,
                    hasGameOver = true,
                    speedIncreases = false,
                    hasObstacles = true
                )

                GameMode.RISING_TIDE -> GameModeConfig(
                    mode = GameMode.RISING_TIDE,
                    name = "Rising Tide",
                    description = "Garbage rises every 10 seconds - Survive!",
                    icon = "🌊",
                    objective = "Clear faster than the tide",
                    difficulty = GameModeDifficulty.HARD,
                    hasTimeLimit = false,
                    hasGameOver = true,
                    speedIncreases = true
                )
            }
        }

        fun getAllModes(): List<GameModeConfig> {
            return GameMode.values().map { getConfig(it) }
        }
    }
}

/**
 * Game mode difficulty levels
 */
enum class GameModeDifficulty(val displayName: String, val color: Long) {
    EASY("Easy", 0xFF4CAF50),
    MEDIUM("Medium", 0xFFFF9800),
    HARD("Hard", 0xFFF44336)
}

/**
 * Game result for mode-specific tracking
 */
@Serializable
data class GameResult(
    val mode: GameMode,
    val score: Int,
    val lines: Int,
    val level: Int,
    val timeSeconds: Int,
    val isWon: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
