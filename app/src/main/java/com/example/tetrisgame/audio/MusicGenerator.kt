package com.example.tetrisgame.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generate simple background music using synthesized tones
 * Plays a simple Tetris-style melody loop
 */
class MusicGenerator {
    private var audioTrack: AudioTrack? = null
    private var musicJob: Job? = null
    private var isPlaying = false
    private var volume = 0.15f // Lower volume for background music

    private val sampleRate = 44100

    // Simple Tetris-like melody (note frequencies in Hz)
    private val melody = listOf(
        660.0 to 400,  // E5
        494.0 to 200,  // B4
        523.0 to 200,  // C5
        587.0 to 400,  // D5
        523.0 to 200,  // C5
        494.0 to 200,  // B4
        440.0 to 400,  // A4
        440.0 to 200,  // A4
        523.0 to 200,  // C5
        660.0 to 400,  // E5
        587.0 to 200,  // D5
        523.0 to 200,  // C5
        494.0 to 600,  // B4
        523.0 to 200,  // C5
        587.0 to 400,  // D5
        660.0 to 400,  // E5
        523.0 to 400,  // C5
        440.0 to 400,  // A4
        440.0 to 400   // A4
    )

    fun startMusic(scope: CoroutineScope) {
        if (isPlaying) return

        isPlaying = true
        musicJob = scope.launch(Dispatchers.IO) {
            while (isPlaying) {
                playMelody()
                delay(500) // Short pause before loop
            }
        }
    }

    private suspend fun playMelody() {
        for ((frequency, durationMs) in melody) {
            if (!isPlaying) break
            playNote(frequency, durationMs)
        }
    }

    private suspend fun playNote(frequency: Double, durationMs: Int) {
        withContext(Dispatchers.IO) {
            try {
                val numSamples = (durationMs * sampleRate / 1000)
                val samples = DoubleArray(numSamples)
                val buffer = ShortArray(numSamples)

                // Generate sine wave with fade out
                for (i in samples.indices) {
                    val fadeOut = if (i > numSamples * 0.8) {
                        (numSamples - i).toDouble() / (numSamples * 0.2)
                    } else {
                        1.0
                    }
                    samples[i] = sin(2.0 * PI * i / (sampleRate / frequency)) * fadeOut
                    buffer[i] = (samples[i] * Short.MAX_VALUE * volume).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                delay(durationMs.toLong())
                track.stop()
                track.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopMusic() {
        isPlaying = false
        musicJob?.cancel()
        musicJob = null
        audioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                stop()
            }
            release()
        }
        audioTrack = null
    }

    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0.0f, 0.3f)
    }

    fun isPlaying(): Boolean = isPlaying
}
