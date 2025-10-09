package com.example.tetrisgame.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generate simple beep sounds programmatically for different game events
 * This is a fallback when no audio files are available
 */
object SoundGenerator {

    private const val SAMPLE_RATE = 44100

    /**
     * Play a simple beep sound with specified frequency and duration
     */
    suspend fun playBeep(frequency: Double, durationMs: Int, volume: Float = 0.3f) {
        withContext(Dispatchers.IO) {
            try {
                val numSamples = (durationMs * SAMPLE_RATE / 1000)
                val samples = DoubleArray(numSamples)
                val buffer = ShortArray(numSamples)

                // Generate sine wave
                for (i in samples.indices) {
                    samples[i] = sin(2.0 * PI * i / (SAMPLE_RATE / frequency))
                    buffer[i] = (samples[i] * Short.MAX_VALUE * volume).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.setVolume(volume) // Apply volume to AudioTrack as well
                audioTrack.play()

                // Clean up after sound finishes
                Thread.sleep(durationMs.toLong())
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Play move sound (short, low beep)
     */
    suspend fun playMoveSound(volume: Float = 0.5f) = playBeep(400.0, 50, volume)

    /**
     * Play rotate sound (medium beep)
     */
    suspend fun playRotateSound(volume: Float = 0.5f) = playBeep(600.0, 80, volume)

    /**
     * Play drop sound (quick low to high)
     */
    suspend fun playDropSound(volume: Float = 0.6f) = playBeep(300.0, 100, volume)

    /**
     * Play line clear sound (rising tone)
     */
    suspend fun playLineClearSound(volume: Float = 0.7f) = playBeep(800.0, 200, volume)

    /**
     * Play Tetris sound (4 lines - triumphant)
     */
    suspend fun playTetrisSound(volume: Float = 0.8f) {
        playBeep(800.0, 100, volume)
        playBeep(1000.0, 100, volume)
        playBeep(1200.0, 150, volume)
    }

    /**
     * Play game over sound (descending)
     */
    suspend fun playGameOverSound(volume: Float = 0.6f) {
        playBeep(600.0, 150, volume)
        playBeep(400.0, 150, volume)
        playBeep(200.0, 300, volume)
    }

    /**
     * Play level up sound (ascending)
     */
    suspend fun playLevelUpSound(volume: Float = 0.7f) {
        playBeep(600.0, 100, volume)
        playBeep(800.0, 100, volume)
        playBeep(1000.0, 200, volume)
    }
}
