package com.example.tetrisgame.data.models

import kotlinx.serialization.Serializable

/**
 * Achievement category types
 */
enum class AchievementCategory {
    SCORE,
    LEVEL,
    LINES,
    SPEED,
    SKILL,
    SURVIVAL
}

/**
 * Achievement rarity levels
 */
enum class AchievementRarity(val stars: Int) {
    COMMON(1),
    RARE(3),
    EPIC(4),
    LEGENDARY(5)
}

/**
 * Achievement data model
 */
@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,  // Emoji
    val category: AchievementCategory,
    val rarity: AchievementRarity,
    val targetValue: Int,  // Target to unlock
    var currentProgress: Int = 0,  // Current progress
    var unlockedAt: Long? = null,  // Timestamp when unlocked
    val isSecret: Boolean = false  // Hidden until unlocked
) {
    val isUnlocked: Boolean
        get() = unlockedAt != null

    val progressPercentage: Int
        get() = if (targetValue > 0) {
            ((currentProgress.toFloat() / targetValue) * 100).toInt().coerceIn(0, 100)
        } else 100
}

/**
 * Predefined achievements
 */
object Achievements {

    // SCORE Achievements
    val BRONZE_SCORER = Achievement(
        id = "bronze_scorer",
        name = "Bronze Scorer",
        description = "Reach 1,000 points",
        icon = "🥉",
        category = AchievementCategory.SCORE,
        rarity = AchievementRarity.COMMON,
        targetValue = 1000
    )

    val SILVER_SCORER = Achievement(
        id = "silver_scorer",
        name = "Silver Scorer",
        description = "Reach 5,000 points",
        icon = "🥈",
        category = AchievementCategory.SCORE,
        rarity = AchievementRarity.COMMON,
        targetValue = 5000
    )

    val GOLD_SCORER = Achievement(
        id = "gold_scorer",
        name = "Gold Scorer",
        description = "Reach 10,000 points",
        icon = "🥇",
        category = AchievementCategory.SCORE,
        rarity = AchievementRarity.RARE,
        targetValue = 10000
    )

    val DIAMOND_SCORER = Achievement(
        id = "diamond_scorer",
        name = "Diamond Scorer",
        description = "Reach 50,000 points",
        icon = "💎",
        category = AchievementCategory.SCORE,
        rarity = AchievementRarity.EPIC,
        targetValue = 50000
    )

    val LEGEND = Achievement(
        id = "legend",
        name = "Legend",
        description = "Reach 100,000 points",
        icon = "👑",
        category = AchievementCategory.SCORE,
        rarity = AchievementRarity.LEGENDARY,
        targetValue = 100000
    )

    // LEVEL Achievements
    val BEGINNER = Achievement(
        id = "beginner",
        name = "Beginner",
        description = "Reach Level 3",
        icon = "🌱",
        category = AchievementCategory.LEVEL,
        rarity = AchievementRarity.COMMON,
        targetValue = 3
    )

    val GETTING_HOT = Achievement(
        id = "getting_hot",
        name = "Getting Hot",
        description = "Reach Level 5",
        icon = "🔥",
        category = AchievementCategory.LEVEL,
        rarity = AchievementRarity.COMMON,
        targetValue = 5
    )

    val ON_FIRE = Achievement(
        id = "on_fire",
        name = "On Fire",
        description = "Reach Level 10",
        icon = "⚡",
        category = AchievementCategory.LEVEL,
        rarity = AchievementRarity.RARE,
        targetValue = 10
    )

    val MASTER = Achievement(
        id = "master",
        name = "Master",
        description = "Reach Level 15",
        icon = "🚀",
        category = AchievementCategory.LEVEL,
        rarity = AchievementRarity.EPIC,
        targetValue = 15
    )

    val GRANDMASTER = Achievement(
        id = "grandmaster",
        name = "Grandmaster",
        description = "Reach Level 20",
        icon = "🌟",
        category = AchievementCategory.LEVEL,
        rarity = AchievementRarity.LEGENDARY,
        targetValue = 20
    )

    // LINE Clear Achievements
    val FIRST_BLOOD = Achievement(
        id = "first_blood",
        name = "First Blood",
        description = "Clear your first line",
        icon = "🎯",
        category = AchievementCategory.LINES,
        rarity = AchievementRarity.COMMON,
        targetValue = 1
    )

    val TETRIS_MASTER = Achievement(
        id = "tetris_master",
        name = "Tetris!",
        description = "Clear 4 lines at once",
        icon = "💥",
        category = AchievementCategory.LINES,
        rarity = AchievementRarity.RARE,
        targetValue = 1  // Special: tracks 4-line clears
    )

    val COMBO_KING = Achievement(
        id = "combo_king",
        name = "Combo King",
        description = "Clear 20 lines in single game",
        icon = "🔥",
        category = AchievementCategory.LINES,
        rarity = AchievementRarity.RARE,
        targetValue = 20
    )

    val CENTURY = Achievement(
        id = "century",
        name = "Century",
        description = "Clear 100 total lines (lifetime)",
        icon = "🏆",
        category = AchievementCategory.LINES,
        rarity = AchievementRarity.EPIC,
        targetValue = 100
    )

    val LINE_MASTER = Achievement(
        id = "line_master",
        name = "Line Master",
        description = "Clear 1,000 total lines (lifetime)",
        icon = "💯",
        category = AchievementCategory.LINES,
        rarity = AchievementRarity.LEGENDARY,
        targetValue = 1000
    )

    // SKILL Achievements
    val HARD_DROPPER = Achievement(
        id = "hard_dropper",
        name = "Hard Dropper",
        description = "Use hard drop 50 times",
        icon = "⬇️",
        category = AchievementCategory.SKILL,
        rarity = AchievementRarity.COMMON,
        targetValue = 50
    )

    val ROTATION_MASTER = Achievement(
        id = "rotation_master",
        name = "Rotation Master",
        description = "Rotate pieces 200 times",
        icon = "🔄",
        category = AchievementCategory.SKILL,
        rarity = AchievementRarity.RARE,
        targetValue = 200
    )

    val EFFICIENT = Achievement(
        id = "efficient",
        name = "Efficient",
        description = "Reach level 5 with less than 100 pieces",
        icon = "🎯",
        category = AchievementCategory.SKILL,
        rarity = AchievementRarity.EPIC,
        targetValue = 1,  // Special: one-time achievement
        isSecret = true
    )

    // SURVIVAL Achievements
    val SURVIVOR = Achievement(
        id = "survivor",
        name = "Survivor",
        description = "Recover after filling 18+ rows",
        icon = "🛡️",
        category = AchievementCategory.SURVIVAL,
        rarity = AchievementRarity.RARE,
        targetValue = 1
    )

    val PHOENIX = Achievement(
        id = "phoenix",
        name = "Phoenix",
        description = "Score 5000+ after near-death",
        icon = "🔄",
        category = AchievementCategory.SURVIVAL,
        rarity = AchievementRarity.EPIC,
        targetValue = 1,
        isSecret = true
    )

    val UNSTOPPABLE = Achievement(
        id = "unstoppable",
        name = "Unstoppable",
        description = "Play 10 games in a row",
        icon = "💪",
        category = AchievementCategory.SURVIVAL,
        rarity = AchievementRarity.LEGENDARY,
        targetValue = 10
    )

    // SPEED Achievements
    val QUICK_START = Achievement(
        id = "quick_start",
        name = "Quick Start",
        description = "Reach Level 5 in under 2 minutes",
        icon = "⚡",
        category = AchievementCategory.SPEED,
        rarity = AchievementRarity.RARE,
        targetValue = 1,
        isSecret = true
    )

    val SPEEDRUNNER = Achievement(
        id = "speedrunner",
        name = "Speedrunner",
        description = "Clear 40 lines in under 3 minutes",
        icon = "🏃",
        category = AchievementCategory.SPEED,
        rarity = AchievementRarity.EPIC,
        targetValue = 1,
        isSecret = true
    )

    val MARATHON_RUNNER = Achievement(
        id = "marathon_runner",
        name = "Marathon Runner",
        description = "Play for 10 minutes without dying",
        icon = "⏰",
        category = AchievementCategory.SPEED,
        rarity = AchievementRarity.LEGENDARY,
        targetValue = 1
    )

    /**
     * Get all achievements as a list
     */
    fun getAllAchievements(): List<Achievement> {
        return listOf(
            // Score
            BRONZE_SCORER, SILVER_SCORER, GOLD_SCORER, DIAMOND_SCORER, LEGEND,
            // Level
            BEGINNER, GETTING_HOT, ON_FIRE, MASTER, GRANDMASTER,
            // Lines
            FIRST_BLOOD, TETRIS_MASTER, COMBO_KING, CENTURY, LINE_MASTER,
            // Skill
            HARD_DROPPER, ROTATION_MASTER, EFFICIENT,
            // Survival
            SURVIVOR, PHOENIX, UNSTOPPABLE,
            // Speed
            QUICK_START, SPEEDRUNNER, MARATHON_RUNNER
        )
    }
}
