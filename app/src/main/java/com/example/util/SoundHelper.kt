package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build

class SoundHelper(context: Context) {
    private val soundPool: SoundPool
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(VIBRATOR_SERVICE) as? Vibrator
    }

    private val soundMap = mutableMapOf<String, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Since we don't have raw audio files bundled, we can synthesize short beeps/tones or use tone generator,
        // or we can use Android's ToneGenerator / Synthesizer or MediaPlayer or fallback safely without crashing if sound ID isn't loaded.
    }

    fun playSound(soundType: String, context: Context) {
        // Vibrate slightly for tactile feedback
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }

        // We can also play a subtle system sound or tone generator for fun feedback
        try {
            val toneType = when (soundType) {
                "chime" -> android.media.ToneGenerator.TONE_DTMF_1
                "sparkle" -> android.media.ToneGenerator.TONE_DTMF_3
                "animal" -> android.media.ToneGenerator.TONE_DTMF_5
                "pop" -> android.media.ToneGenerator.TONE_DTMF_7
                "boing" -> android.media.ToneGenerator.TONE_DTMF_9
                else -> android.media.ToneGenerator.TONE_PROP_BEEP
            }
            val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 80)
            tg.startTone(toneType, 150)
        } catch (e: Exception) {
            // Ignore tone generator errors on devices without audio stream
        }
    }

    fun release() {
        try {
            soundPool.release()
        } catch (e: Exception) {}
    }
}
