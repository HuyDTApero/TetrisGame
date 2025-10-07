package com.example.tetrisgame.data.managers

import com.example.tetrisgame.data.models.GameTheme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages app settings using DataStore
 */
class SettingsManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tetris_settings")

        // Audio settings
        private val SFX_ENABLED_KEY = booleanPreferencesKey("sfx_enabled")
        private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
        private val SFX_VOLUME_KEY = floatPreferencesKey("sfx_volume")
        private val MUSIC_VOLUME_KEY = floatPreferencesKey("music_volume")

        // Haptic settings
        private val HAPTIC_ENABLED_KEY = booleanPreferencesKey("haptic_enabled")

        // Control settings
        private val GESTURE_SENSITIVITY_KEY = floatPreferencesKey("gesture_sensitivity")

        // Theme settings
        private val THEME_KEY = stringPreferencesKey("theme")

        // AI Assistant settings
        private val AI_ASSISTANT_ENABLED_KEY = booleanPreferencesKey("ai_assistant_enabled")
    }

    // Audio settings
    val isSfxEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SFX_ENABLED_KEY] ?: true
    }

    val isMusicEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MUSIC_ENABLED_KEY] ?: true
    }

    val sfxVolume: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SFX_VOLUME_KEY] ?: 0.7f
    }

    val musicVolume: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[MUSIC_VOLUME_KEY] ?: 0.5f
    }

    // Haptic settings
    val isHapticEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_ENABLED_KEY] ?: true
    }

    // Control settings
    val gestureSensitivity: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[GESTURE_SENSITIVITY_KEY] ?: 50f
    }

    // Theme settings
    val theme: Flow<GameTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: GameTheme.NEON.name
        try {
            GameTheme.valueOf(themeName)
        } catch (e: Exception) {
            GameTheme.NEON
        }
    }

    // AI Assistant settings
    val isAIAssistantEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AI_ASSISTANT_ENABLED_KEY] ?: false
    }


    // Update functions
    suspend fun setSfxEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SFX_ENABLED_KEY] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun setSfxVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[SFX_VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }

    suspend fun setMusicVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun setGestureSensitivity(sensitivity: Float) {
        context.dataStore.edit { preferences ->
            preferences[GESTURE_SENSITIVITY_KEY] = sensitivity.coerceIn(20f, 100f)
        }
    }

    suspend fun setTheme(theme: GameTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setAIAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AI_ASSISTANT_ENABLED_KEY] = enabled
        }
    }


    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
