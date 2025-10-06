package com.example.tetrisgame.input

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

/**
 * Manages haptic feedback for game events
 */
class HapticFeedbackManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var isHapticsEnabled = true

    enum class HapticType {
        LIGHT,      // Move, rotate
        MEDIUM,     // Lock piece
        HEAVY,      // Line clear
        PATTERN     // Tetris, level up
    }

    /**
     * Trigger haptic feedback
     */
    fun vibrate(type: HapticType) {
        if (!isHapticsEnabled || vibrator == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrateWithEffect(type)
        } else {
            vibrateLegacy(type)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrateWithEffect(type: HapticType) {
        val effect = when (type) {
            HapticType.LIGHT -> VibrationEffect.createOneShot(10, 50)  // 10ms, light
            HapticType.MEDIUM -> VibrationEffect.createOneShot(30, 100) // 30ms, medium
            HapticType.HEAVY -> VibrationEffect.createOneShot(50, 200)  // 50ms, strong
            HapticType.PATTERN -> {
                // Pattern: short-short-long
                val timings = longArrayOf(0, 50, 100, 50, 100, 150)
                val amplitudes = intArrayOf(0, 100, 0, 100, 0, 200)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
        }
        vibrator?.vibrate(effect)
    }

    @Suppress("DEPRECATION")
    private fun vibrateLegacy(type: HapticType) {
        val duration = when (type) {
            HapticType.LIGHT -> 10L
            HapticType.MEDIUM -> 30L
            HapticType.HEAVY -> 50L
            HapticType.PATTERN -> {
                // Pattern vibration
                val pattern = longArrayOf(0, 50, 100, 50, 100, 150)
                vibrator?.vibrate(pattern, -1)
                return
            }
        }
        vibrator?.vibrate(duration)
    }

    /**
     * Quick haptics for different game events
     */
    fun onMove() = vibrate(HapticType.LIGHT)
    fun onRotate() = vibrate(HapticType.LIGHT)
    fun onLock() = vibrate(HapticType.MEDIUM)
    fun onLineClear() = vibrate(HapticType.HEAVY)
    fun onTetris() = vibrate(HapticType.PATTERN)
    fun onLevelUp() = vibrate(HapticType.PATTERN)
    fun onGameOver() = vibrate(HapticType.HEAVY)

    /**
     * Toggle haptics on/off
     */
    fun toggleHaptics(): Boolean {
        isHapticsEnabled = !isHapticsEnabled
        return isHapticsEnabled
    }

    /**
     * Set haptics enabled state
     */
    fun setEnabled(enabled: Boolean) {
        isHapticsEnabled = enabled
    }

    /**
     * Check if haptics is enabled
     */
    fun isEnabled() = isHapticsEnabled

    /**
     * Check if device supports haptics
     */
    fun isSupported() = vibrator?.hasVibrator() == true
}
