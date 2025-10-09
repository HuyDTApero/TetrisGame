package com.example.tetrisgame.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Enhanced Sound Manager with real audio files support
 * Falls back to synthesized sounds if files are missing
 */
class EnhancedSoundManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundType, Int>()
    private val soundLoaded = mutableMapOf<SoundType, Boolean>()

    private var isSoundEnabled = true
    private var soundVolume = 0.7f

    enum class SoundType {
        MOVE,           // Move/rotate piece
        LOCK,           // Piece locks on board
        LINE_CLEAR,     // Clear 1-3 lines
        TETRIS,         // Clear 4 lines (special!)
        LEVEL_UP,       // Level increase
        GAME_OVER       // Game over
    }

    init {
        initSoundPool()
        loadAllSounds()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Listen for sound loading completion
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) { // Success
                soundLoaded.values.forEach { }
            }
        }
    }

    private fun loadAllSounds() {
        // Try to load real sound files, mark as loaded if successful
        loadSound(SoundType.MOVE, "sound_move")
        loadSound(SoundType.LOCK, "sound_lock")
        loadSound(SoundType.LINE_CLEAR, "sound_line_clear")
        loadSound(SoundType.TETRIS, "sound_tetris")
        loadSound(SoundType.LEVEL_UP, "sound_level_up")
        loadSound(SoundType.GAME_OVER, "sound_game_over")
    }

    private fun loadSound(type: SoundType, fileName: String) {
        try {
            val resId = context.resources.getIdentifier(
                fileName,
                "raw",
                context.packageName
            )

            if (resId != 0) {
                soundPool?.let { pool ->
                    val soundId = pool.load(context, resId, 1)
                    soundIds[type] = soundId
                    soundLoaded[type] = true
                }
            } else {
                // File not found, will use synthesized fallback
                soundLoaded[type] = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            soundLoaded[type] = false
        }
    }

    /**
     * Play a sound effect
     * Uses real audio if loaded, falls back to synthesized sound
     */
    fun playSound(soundType: SoundType) {
        if (!isSoundEnabled) return

        val isLoaded = soundLoaded[soundType] ?: false

        if (isLoaded) {
            // Play real audio file
            playRealSound(soundType)
        } else {
            // Fallback to synthesized sound
            playSynthesizedSound(soundType)
        }
    }

    private fun playRealSound(soundType: SoundType) {
        soundIds[soundType]?.let { soundId ->
            // Apply the current volume setting when playing
            soundPool?.play(soundId, soundVolume, soundVolume, 1, 0, 1.0f)
        }
    }

    private fun playSynthesizedSound(soundType: SoundType) {
        // Fallback to generated sounds with current volume
        scope.launch {
            when (soundType) {
                SoundType.MOVE -> SoundGenerator.playMoveSound(soundVolume)
                SoundType.LOCK -> SoundGenerator.playDropSound(soundVolume)
                SoundType.LINE_CLEAR -> SoundGenerator.playLineClearSound(soundVolume)
                SoundType.TETRIS -> SoundGenerator.playTetrisSound(soundVolume)
                SoundType.LEVEL_UP -> SoundGenerator.playLevelUpSound(soundVolume)
                SoundType.GAME_OVER -> SoundGenerator.playGameOverSound(soundVolume)
            }
        }
    }

    /**
     * Toggle sound effects on/off
     */
    fun toggleSound(): Boolean {
        isSoundEnabled = !isSoundEnabled
        return isSoundEnabled
    }

    /**
     * Set sound volume (0.0f to 1.0f)
     */
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0.0f, 1.0f)
    }

    /**
     * Check if sound is enabled
     */
    fun isSoundEnabled(): Boolean = isSoundEnabled

    /**
     * Get sound load status for debugging
     */
    fun getSoundStatus(): Map<SoundType, Boolean> {
        return soundLoaded.toMap()
    }

    /**
     * Release all resources
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        soundLoaded.clear()
    }
}
