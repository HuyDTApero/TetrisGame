package com.example.tetrisgame.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null

    private val soundIds = mutableMapOf<SoundType, Int>()
    private var isSoundEnabled = true
    private var isMusicEnabled = true

    private var soundVolume = 1.0f
    private var musicVolume = 0.5f

    enum class SoundType {
        MOVE,
        ROTATE,
        DROP,
        LINE_CLEAR,
        TETRIS,
        GAME_OVER,
        LEVEL_UP
    }

    init {
        initSoundPool()
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
    }

    /**
     * Load a sound effect from raw resources
     * Call this during initialization for all sounds you need
     */
    fun loadSound(soundType: SoundType, @RawRes resourceId: Int) {
        soundPool?.let { pool ->
            val soundId = pool.load(context, resourceId, 1)
            soundIds[soundType] = soundId
        }
    }

    /**
     * Play a sound effect
     */
    fun playSound(soundType: SoundType) {
        if (!isSoundEnabled) return

        soundIds[soundType]?.let { soundId ->
            soundPool?.play(soundId, soundVolume, soundVolume, 1, 0, 1.0f)
        }
    }

    /**
     * Start background music with looping
     */
    fun startMusic(@RawRes resourceId: Int) {
        if (!isMusicEnabled) return

        stopMusic() // Stop any existing music

        try {
            mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stop background music
     */
    fun stopMusic() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    /**
     * Pause background music
     */
    fun pauseMusic() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
            }
        }
    }

    /**
     * Resume background music
     */
    fun resumeMusic() {
        if (!isMusicEnabled) return

        mediaPlayer?.apply {
            if (!isPlaying) {
                start()
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
     * Toggle music on/off
     */
    fun toggleMusic(): Boolean {
        isMusicEnabled = !isMusicEnabled
        if (isMusicEnabled) {
            resumeMusic()
        } else {
            pauseMusic()
        }
        return isMusicEnabled
    }

    /**
     * Set sound effects volume (0.0f to 1.0f)
     */
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0.0f, 1.0f)
    }

    /**
     * Set music volume (0.0f to 1.0f)
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0.0f, 1.0f)
        mediaPlayer?.setVolume(musicVolume, musicVolume)
    }

    /**
     * Check if sound is enabled
     */
    fun isSoundEnabled(): Boolean = isSoundEnabled

    /**
     * Check if music is enabled
     */
    fun isMusicEnabled(): Boolean = isMusicEnabled

    /**
     * Release all resources
     * Call this when the game is destroyed
     */
    fun release() {
        soundPool?.release()
        soundPool = null

        stopMusic()

        soundIds.clear()
    }
}
