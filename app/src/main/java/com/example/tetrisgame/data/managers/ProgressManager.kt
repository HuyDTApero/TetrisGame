package com.example.tetrisgame.data.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tetrisgame.data.models.GameLevel
import com.example.tetrisgame.data.models.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "player_progress")

class ProgressManager(private val context: Context) {

    private object Keys {
        val CURRENT_LEVEL = stringPreferencesKey("current_level")
        val UNLOCKED_LEVELS = stringSetPreferencesKey("unlocked_levels")
        val TOTAL_PLAY_TIME = longPreferencesKey("total_play_time")
        val GAMES_PLAYED = intPreferencesKey("games_played")

        // Score keys per level
        fun levelScore(level: GameLevel) = intPreferencesKey("${level.name}_score")
        fun levelHighScore(level: GameLevel) = intPreferencesKey("${level.name}_high_score")
        fun levelLinesCleared(level: GameLevel) = intPreferencesKey("${level.name}_lines_cleared")
    }

    val playerProgress: Flow<PlayerProgress> = context.progressDataStore.data.map { prefs ->
        val currentLevel = prefs[Keys.CURRENT_LEVEL] ?: GameLevel.CLASSIC.name
        val unlockedLevels = prefs[Keys.UNLOCKED_LEVELS] ?: setOf(GameLevel.CLASSIC.name)
        val totalPlayTime = prefs[Keys.TOTAL_PLAY_TIME] ?: 0L
        val gamesPlayed = prefs[Keys.GAMES_PLAYED] ?: 0

        val levelScores = mutableMapOf<String, Int>()
        val levelHighScores = mutableMapOf<String, Int>()
        val levelLinesCleared = mutableMapOf<String, Int>()

        GameLevel.entries.forEach { level ->
            prefs[Keys.levelScore(level)]?.let {
                levelScores[level.name] = it
            }
            prefs[Keys.levelHighScore(level)]?.let {
                levelHighScores[level.name] = it
            }
            prefs[Keys.levelLinesCleared(level)]?.let {
                levelLinesCleared[level.name] = it
            }
        }

        PlayerProgress(
            currentLevel = currentLevel,
            levelScores = levelScores,
            levelHighScores = levelHighScores,
            levelLinesCleared = levelLinesCleared,
            unlockedLevels = unlockedLevels,
            totalPlayTime = totalPlayTime,
            gamesPlayed = gamesPlayed
        )
    }

    suspend fun saveProgress(progress: PlayerProgress) {
        context.progressDataStore.edit { prefs ->
            prefs[Keys.CURRENT_LEVEL] = progress.currentLevel
            prefs[Keys.UNLOCKED_LEVELS] = progress.unlockedLevels
            prefs[Keys.TOTAL_PLAY_TIME] = progress.totalPlayTime
            prefs[Keys.GAMES_PLAYED] = progress.gamesPlayed

            progress.levelScores.forEach { (levelName, score) ->
                val level = GameLevel.valueOf(levelName)
                prefs[Keys.levelScore(level)] = score
            }

            progress.levelHighScores.forEach { (levelName, score) ->
                val level = GameLevel.valueOf(levelName)
                prefs[Keys.levelHighScore(level)] = score
            }

            progress.levelLinesCleared.forEach { (levelName, lines) ->
                val level = GameLevel.valueOf(levelName)
                prefs[Keys.levelLinesCleared(level)] = lines
            }
        }
    }

    suspend fun setCurrentLevel(level: GameLevel) {
        context.progressDataStore.edit { prefs ->
            prefs[Keys.CURRENT_LEVEL] = level.name
        }
    }

    suspend fun unlockLevel(level: GameLevel) {
        context.progressDataStore.edit { prefs ->
            val currentUnlocked = prefs[Keys.UNLOCKED_LEVELS] ?: setOf(GameLevel.CLASSIC.name)
            prefs[Keys.UNLOCKED_LEVELS] = currentUnlocked + level.name
        }
    }

    suspend fun updateGameResult(level: GameLevel, score: Int, lines: Int, playTime: Long) {
        context.progressDataStore.edit { prefs ->
            // Update score
            prefs[Keys.levelScore(level)] = score

            // Update high score if needed
            val currentHighScore = prefs[Keys.levelHighScore(level)] ?: 0
            if (score > currentHighScore) {
                prefs[Keys.levelHighScore(level)] = score
            }

            // Update total lines cleared
            val currentLines = prefs[Keys.levelLinesCleared(level)] ?: 0
            prefs[Keys.levelLinesCleared(level)] = currentLines + lines

            // Update total play time
            val totalPlayTime = prefs[Keys.TOTAL_PLAY_TIME] ?: 0L
            prefs[Keys.TOTAL_PLAY_TIME] = totalPlayTime + playTime

            // Increment games played
            val gamesPlayed = prefs[Keys.GAMES_PLAYED] ?: 0
            prefs[Keys.GAMES_PLAYED] = gamesPlayed + 1
        }
    }

    suspend fun resetProgress() {
        context.progressDataStore.edit { prefs ->
            prefs.clear()
            // Re-unlock classic level
            prefs[Keys.CURRENT_LEVEL] = GameLevel.CLASSIC.name
            prefs[Keys.UNLOCKED_LEVELS] = setOf(GameLevel.CLASSIC.name)
        }
    }
}
