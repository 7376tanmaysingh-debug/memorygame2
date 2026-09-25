package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

class SoundManager {

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true

    // Pre-computed audio buffers for zero latency playback
    private val clickBuffer: ShortArray by lazy {
        val durationMs = 20
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 1300.0 - (progress * 600.0) // quick snappy click
            val envelope = exp(-progress * 7.0)
            val sample = sin(2.0 * Math.PI * freq * t) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
        buffer
    }

    private val cardFlipBuffer: ShortArray by lazy {
        val durationMs = 42
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 420.0 + (progress * 350.0)
            val envelope = sin(progress * Math.PI)
            val sample = sin(2.0 * Math.PI * freq * t) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
        buffer
    }

    private val mismatchBuffer: ShortArray by lazy {
        val durationMs = 85
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 270.0 - (progress * 90.0)
            val envelope = 1.0 - progress
            val sample = sin(2.0 * Math.PI * freq * t) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * 0.38).toInt().toShort()
        }
        buffer
    }

    private val matchBuffer: ShortArray by lazy {
        val notes = doubleArrayOf(523.25, 659.25, 783.99) // C5, E5, G5
        val noteDurationMs = 65
        val noteSamples = (sampleRate * noteDurationMs) / 1000
        val totalSamples = noteSamples * notes.size
        val buffer = ShortArray(totalSamples)

        for ((nIdx, freq) in notes.withIndex()) {
            val start = nIdx * noteSamples
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / noteSamples
                val envelope = sin(progress * Math.PI)
                val sample = sin(2.0 * Math.PI * freq * t) * envelope
                buffer[start + i] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
            }
        }
        buffer
    }

    private val victoryBuffer: ShortArray by lazy {
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val durations = intArrayOf(85, 85, 105, 260)
        val totalSamples = durations.sumOf { (sampleRate * it) / 1000 }
        val buffer = ShortArray(totalSamples)

        var offset = 0
        for (n in notes.indices) {
            val freq = notes[n]
            val durMs = durations[n]
            val samples = (sampleRate * durMs) / 1000
            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val envelope = if (n == notes.lastIndex) (1.0 - (progress * 0.5)) else sin(progress * Math.PI)
                val sample = (sin(2.0 * Math.PI * freq * t) * 0.7 + sin(4.0 * Math.PI * freq * t) * 0.3) * envelope
                buffer[offset + i] = (sample * Short.MAX_VALUE * 0.5).toInt().toShort()
            }
            offset += samples
        }
        buffer
    }

    fun playClick() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(clickBuffer)
        }
    }

    fun playCardFlip() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(cardFlipBuffer)
        }
    }

    fun playMatch() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(matchBuffer)
        }
    }

    fun playMismatch() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(mismatchBuffer)
        }
    }

    fun playVictory() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(victoryBuffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
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
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            scope.launch {
                val durationMs = ((buffer.size.toDouble() / sampleRate) * 1000).toLong() + 40
                kotlinx.coroutines.delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // AudioTrack creation fallback
        }
    }
}
