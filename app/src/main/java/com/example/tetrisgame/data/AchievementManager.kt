package com.example.tetrisgame.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.achievementDataStore: DataStore<Preferences> by preferencesDataStore(name = "achievements")

/**
 * Manages achievement progress and unlocking
 */
class AchievementManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val ACHIEVEMENTS_KEY = stringPreferencesKey("achievements_json")
        private val TOTAL_GAMES_KEY = intPreferencesKey("total_games")
        private val CONSECUTIVE_GAMES_KEY = intPreferencesKey("consecutive_games")
        private val LIFETIME_LINES_KEY = intPreferencesKey("lifetime_lines")
        private val HARD_DROPS_KEY = intPreferencesKey("hard_drops")
        private val ROTATIONS_KEY = intPreferencesKey("rotations")
    }

    /**
     * Flow of all achievements with current progress
     */
    val achievements: Flow<List<Achievement>> = context.achievementDataStore.data.map { preferences ->
        val achievementsJson = preferences[ACHIEVEMENTS_KEY]

        if (achievementsJson != null) {
            try {
                json.decodeFromString<List<Achievement>>(achievementsJson)
            } catch (e: Exception) {
                Achievements.getAllAchievements()
            }
        } else {
            Achievements.getAllAchievements()
        }
    }

    /**
     * Flow of unlocked achievements count
     */
    val unlockedCount: Flow<Int> = achievements.map { list ->
        list.count { it.isUnlocked }
    }

    /**
     * Flow of total achievements count
     */
    val totalCount: Flow<Int> = achievements.map { list ->
        list.size
    }

    /**
     * Flow of completion percentage
     */
    val completionPercentage: Flow<Int> = achievements.map { list ->
        val total = list.size
        val unlocked = list.count { it.isUnlocked }
        if (total > 0) (unlocked * 100) / total else 0
    }

    /**
     * Get achievements by category
     */
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>> {
        return achievements.map { list ->
            list.filter { it.category == category }
        }
    }

    /**
     * Check and update achievement progress
     * Returns newly unlocked achievements
     */
    suspend fun checkAchievements(
        score: Int = 0,
        level: Int = 0,
        linesInGame: Int = 0,
        linesClearedAtOnce: Int = 0,
        hardDropUsed: Boolean = false,
        rotationUsed: Boolean = false,
        gameStartTime: Long = 0,
        gameEndTime: Long = 0,
        nearDeathRecovered: Boolean = false,
        piecesPlaced: Int = 0
    ): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()

        context.achievementDataStore.edit { preferences ->
            val achievementsJson = preferences[ACHIEVEMENTS_KEY]
            val currentAchievements = if (achievementsJson != null) {
                try {
                    json.decodeFromString<List<Achievement>>(achievementsJson).toMutableList()
                } catch (e: Exception) {
                    Achievements.getAllAchievements().toMutableList()
                }
            } else {
                Achievements.getAllAchievements().toMutableList()
            }

            // Update stats
            if (hardDropUsed) {
                preferences[HARD_DROPS_KEY] = (preferences[HARD_DROPS_KEY] ?: 0) + 1
            }
            if (rotationUsed) {
                preferences[ROTATIONS_KEY] = (preferences[ROTATIONS_KEY] ?: 0) + 1
            }

            val lifetimeLines = (preferences[LIFETIME_LINES_KEY] ?: 0) + linesInGame
            preferences[LIFETIME_LINES_KEY] = lifetimeLines

            val hardDrops = preferences[HARD_DROPS_KEY] ?: 0
            val rotations = preferences[ROTATIONS_KEY] ?: 0
            val consecutiveGames = preferences[CONSECUTIVE_GAMES_KEY] ?: 0

            val gameDuration = if (gameEndTime > 0 && gameStartTime > 0) {
                (gameEndTime - gameStartTime) / 1000 // seconds
            } else 0

            // Check each achievement
            currentAchievements.forEachIndexed { index, achievement ->
                if (!achievement.isUnlocked) {
                    val shouldUnlock = when (achievement.id) {
                        // Score achievements
                        "bronze_scorer" -> score >= 1000
                        "silver_scorer" -> score >= 5000
                        "gold_scorer" -> score >= 10000
                        "diamond_scorer" -> score >= 50000
                        "legend" -> score >= 100000

                        // Level achievements
                        "beginner" -> level >= 3
                        "getting_hot" -> level >= 5
                        "on_fire" -> level >= 10
                        "master" -> level >= 15
                        "grandmaster" -> level >= 20

                        // Line achievements
                        "first_blood" -> linesInGame >= 1
                        "tetris_master" -> linesClearedAtOnce == 4
                        "combo_king" -> linesInGame >= 20
                        "century" -> lifetimeLines >= 100
                        "line_master" -> lifetimeLines >= 1000

                        // Skill achievements
                        "hard_dropper" -> hardDrops >= 50
                        "rotation_master" -> rotations >= 200
                        "efficient" -> level >= 5 && piecesPlaced < 100

                        // Survival achievements
                        "survivor" -> nearDeathRecovered
                        "phoenix" -> nearDeathRecovered && score >= 5000
                        "unstoppable" -> consecutiveGames >= 10

                        // Speed achievements
                        "quick_start" -> level >= 5 && gameDuration > 0 && gameDuration <= 120
                        "speedrunner" -> linesInGame >= 40 && gameDuration > 0 && gameDuration <= 180
                        "marathon_runner" -> gameDuration >= 600

                        else -> false
                    }

                    if (shouldUnlock) {
                        currentAchievements[index] = achievement.copy(
                            unlockedAt = System.currentTimeMillis(),
                            currentProgress = achievement.targetValue
                        )
                        newlyUnlocked.add(currentAchievements[index])
                    } else {
                        // Update progress
                        val newProgress = when (achievement.id) {
                            "bronze_scorer", "silver_scorer", "gold_scorer",
                            "diamond_scorer", "legend" -> score
                            "beginner", "getting_hot", "on_fire",
                            "master", "grandmaster" -> level
                            "combo_king" -> linesInGame
                            "century", "line_master" -> lifetimeLines
                            "hard_dropper" -> hardDrops
                            "rotation_master" -> rotations
                            "unstoppable" -> consecutiveGames
                            else -> achievement.currentProgress
                        }
                        currentAchievements[index] = achievement.copy(currentProgress = newProgress)
                    }
                }
            }

            // Save updated achievements
            preferences[ACHIEVEMENTS_KEY] = json.encodeToString(currentAchievements)
        }

        return newlyUnlocked
    }

    /**
     * Increment game count (for consecutive games tracking)
     */
    suspend fun incrementGameCount() {
        context.achievementDataStore.edit { preferences ->
            preferences[TOTAL_GAMES_KEY] = (preferences[TOTAL_GAMES_KEY] ?: 0) + 1
            preferences[CONSECUTIVE_GAMES_KEY] = (preferences[CONSECUTIVE_GAMES_KEY] ?: 0) + 1
        }
    }

    /**
     * Reset consecutive games (when user leaves to menu)
     */
    suspend fun resetConsecutiveGames() {
        context.achievementDataStore.edit { preferences ->
            preferences[CONSECUTIVE_GAMES_KEY] = 0
        }
    }

    /**
     * Get stats for achievements tracking
     */
    fun getStats(): Flow<AchievementStats> {
        return context.achievementDataStore.data.map { preferences ->
            AchievementStats(
                totalGames = preferences[TOTAL_GAMES_KEY] ?: 0,
                lifetimeLines = preferences[LIFETIME_LINES_KEY] ?: 0,
                hardDrops = preferences[HARD_DROPS_KEY] ?: 0,
                rotations = preferences[ROTATIONS_KEY] ?: 0
            )
        }
    }

    /**
     * Reset all achievements (for testing)
     */
    suspend fun resetAllAchievements() {
        context.achievementDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

/**
 * Stats for achievement tracking
 */
data class AchievementStats(
    val totalGames: Int = 0,
    val lifetimeLines: Int = 0,
    val hardDrops: Int = 0,
    val rotations: Int = 0
)
