package com.delta.harmonica.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

class TonePlayer {
    private val sampleRate = 44100
    private val freqs = doubleArrayOf(
        261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25,
        554.37, 587.33, 659.25, 698.46
    )

    fun play(index: Int, durationMs: Int = 220, volume: Float = 0.7f) {
        if (index !in 0 until freqs.size) return

        val freq = freqs[index]
        val frameCount = (sampleRate * durationMs / 1000.0).toInt()
        val buffer = ShortArray(frameCount)

        for (i in buffer.indices) {
            val phase = (2.0 * PI * freq * i / sampleRate)
            val envelope = (1.0 - i.toDouble() / buffer.size).coerceIn(0.0, 1.0)
            val sample = sin(phase) * 0.45 * envelope * 32767.0
            buffer[i] = sample.toInt().toShort()
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
            .build()

        track.play()
        track.write(buffer, 0, buffer.size)

        Thread {
            Thread.sleep(durationMs.toLong())
            track.stop()
            track.release()
        }.start()
    }
}
