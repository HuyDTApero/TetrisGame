package com.example.tetrisgame.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a single high score entry
 */
@Serializable
data class HighScoreEntry(
    val score: Int,
    val lines: Int,
    val level: Int,
    val date: String
)

/**
 * Manages high score persistence using DataStore
 */
class HighScoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tetris_high_scores")
        private val TOP_SCORES_KEY = stringPreferencesKey("top_scores")
        private val TOTAL_GAMES_KEY = intPreferencesKey("total_games")
        private val TOTAL_LINES_KEY = intPreferencesKey("total_lines")
        private val MAX_LEVEL_KEY = intPreferencesKey("max_level")
        private const val MAX_TOP_SCORES = 10
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get top 10 scores as Flow
     */
    val topScores: Flow<List<HighScoreEntry>> = context.dataStore.data.map { preferences ->
        val scoresJson = preferences[TOP_SCORES_KEY] ?: "[]"
        try {
            json.decodeFromString<List<HighScoreEntry>>(scoresJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get high score (top score) as Flow
     */
    val highScore: Flow<Int> = topScores.map { scores ->
        scores.firstOrNull()?.score ?: 0
    }

    /**
     * Get total games played
     */
    val totalGames: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[TOTAL_GAMES_KEY] ?: 0
    }

    /**
     * Get total lines cleared
     */
    val totalLines: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[TOTAL_LINES_KEY] ?: 0
    }

    /**
     * Get max level reached
     */
    val maxLevel: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MAX_LEVEL_KEY] ?: 1
    }

    /**
     * Save game results and update top scores
     */
    suspend fun saveGameResult(score: Int, lines: Int, level: Int) {
        context.dataStore.edit { preferences ->
            // Get current top scores
            val scoresJson = preferences[TOP_SCORES_KEY] ?: "[]"
            val currentScores = try {
                json.decodeFromString<List<HighScoreEntry>>(scoresJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }

            // Add new score
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val newEntry = HighScoreEntry(
                score = score,
                lines = lines,
                level = level,
                date = dateFormat.format(Date())
            )
            currentScores.add(newEntry)

            // Sort by score (descending) and keep top 10
            val topScores = currentScores
                .sortedByDescending { it.score }
                .take(MAX_TOP_SCORES)

            // Save back to preferences
            preferences[TOP_SCORES_KEY] = json.encodeToString(topScores)

            // Increment total games
            val currentTotalGames = preferences[TOTAL_GAMES_KEY] ?: 0
            preferences[TOTAL_GAMES_KEY] = currentTotalGames + 1

            // Add to total lines
            val currentTotalLines = preferences[TOTAL_LINES_KEY] ?: 0
            preferences[TOTAL_LINES_KEY] = currentTotalLines + lines

            // Update max level if this level is higher
            val currentMaxLevel = preferences[MAX_LEVEL_KEY] ?: 1
            if (level > currentMaxLevel) {
                preferences[MAX_LEVEL_KEY] = level
            }
        }
    }

    /**
     * Reset all statistics
     */
    suspend fun resetStatistics() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Check if score makes it to top 10
     */
    suspend fun isTopScore(score: Int): Boolean {
        val currentTopScores = topScores.first()
        return currentTopScores.size < MAX_TOP_SCORES ||
               score > (currentTopScores.lastOrNull()?.score ?: 0)
    }
}
