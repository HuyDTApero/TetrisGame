package com.example.tetrisgame.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.example.tetrisgame.R
import kotlinx.coroutines.CoroutineScope

/**
 * Real Music Manager using MediaPlayer for actual audio files
 * Compatible interface with MusicGenerator for easy replacement
 */
class RealMusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isMusicPlaying = false
    private var currentVolume = 0.5f
    private var isMusicEnabled = true

    /**
     * Start playing background music from raw resource
     */
    fun startMusic(scope: CoroutineScope, @RawRes resourceId: Int = R.raw.tetrismusic) {
        if (!isMusicEnabled) return


        try {
            // Stop any existing music
            stopMusic()

            // Create new MediaPlayer (already prepared)
            mediaPlayer = MediaPlayer.create(context, resourceId)?.apply {
                isLooping = true
                setVolume(currentVolume, currentVolume)

                setOnErrorListener { _, what, extra ->
                    isMusicPlaying = false
                    false
                }
                setOnCompletionListener {
                    isMusicPlaying = false
                }

                // Start playing immediately since MediaPlayer.create() returns prepared player
                start()
                isMusicPlaying = true
            }

            if (mediaPlayer == null) {
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stop background music
     */
    fun stopMusic() {
        isMusicPlaying = false
        mediaPlayer?.apply {
            try {
                if (isPlaying) {
                    stop()
                }
                release()
            } catch (e: Exception) {
            }
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
                isMusicPlaying = false
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
                isMusicPlaying = true
            }
        }
    }

    /**
     * Set music volume (0.0f to 1.0f)
     */
    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(currentVolume, currentVolume)
    }

    /**
     * Check if music is currently playing
     */
    fun isPlaying(): Boolean = isMusicPlaying && mediaPlayer?.isPlaying == true

    /**
     * Enable/disable music
     */
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            pauseMusic()
        }
    }

    /**
     * Release all resources
     */
    fun release() {
        stopMusic()
    }
}