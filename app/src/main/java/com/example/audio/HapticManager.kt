package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(private val context: Context) {

    var isHapticsEnabled: Boolean = true

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrateTap() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 25, amplitude = 70)
    }

    fun vibrateMatch() {
        if (!isHapticsEnabled) return
        // Double pulse for match
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasVibrator() == true) {
            try {
                val timings = longArrayOf(0, 35, 60, 45)
                val amplitudes = intArrayOf(0, 120, 0, 180)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } catch (_: Exception) {}
        } else {
            vibrate(durationMs = 80, amplitude = 150)
        }
    }

    fun vibrateMismatch() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 60, amplitude = 90)
    }

    fun vibrateVictory() {
        if (!isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasVibrator() == true) {
            try {
                val timings = longArrayOf(0, 50, 70, 60, 70, 120)
                val amplitudes = intArrayOf(0, 100, 0, 150, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } catch (_: Exception) {}
        } else {
            vibrate(durationMs = 150, amplitude = 200)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate(durationMs: Long, amplitude: Int) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                vib.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
