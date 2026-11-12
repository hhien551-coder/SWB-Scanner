package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.model.ValidationStatus
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class FeedbackManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null
    private val audioExecutor = Executors.newSingleThreadExecutor()

    // Pre-synthesized "Ting tong" chime PCM buffer
    private val sampleRate = 44100
    private val tingTongBuffer: ShortArray

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 95)
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Init error", e)
        }

        // Generate high-fidelity melodic "Ting-tong" chime
        tingTongBuffer = generateTingTongPcm()
    }

    private fun generateTingTongPcm(): ShortArray {
        val durationTing = 0.14 // 140ms
        val durationTong = 0.26 // 260ms
        val totalDuration = durationTing + durationTong
        val totalSamples = (sampleRate * totalDuration).toInt()
        val buffer = ShortArray(totalSamples)

        val freqTing = 1250.0 // Bright high chime ("Ting")
        val freqTong = 932.0  // Warm resolving chime ("Tong")

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val sample = if (t < durationTing) {
                // "Ting" note with bell envelope
                val env = exp(-t * 16.0)
                sin(2.0 * PI * freqTing * t) * env
            } else {
                // "Tong" note with resonance decay
                val localT = t - durationTing
                val env = exp(-localT * 10.0)
                sin(2.0 * PI * freqTong * localT) * env
            }
            buffer[i] = (sample * 28000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(1.05f)
            tts?.setPitch(1.0f)
            isTtsReady = true
        }
    }

    fun onScanResult(status: ValidationStatus, customMessage: String? = null) {
        when (status) {
            ValidationStatus.VALID -> {
                // 1. Play authentic two-tone "Ting tong" chime immediately
                playTingTong()
                // 2. Gentle haptic bump
                vibrateSuccess()
                // No TTS voice on success as requested
            }
            ValidationStatus.ALREADY_CHECKED_IN -> {
                playWarningTone()
                vibrateError()
                speak("Vé đã sử dụng")
            }
            ValidationStatus.WRONG_TRIP -> {
                playWarningTone()
                vibrateError()
                speak("Vé sai chuyến")
            }
            ValidationStatus.WRONG_STATION -> {
                playWarningTone()
                vibrateError()
                speak("Sai bến khởi hành")
            }
            ValidationStatus.NOT_FOUND, ValidationStatus.INVALID_FORMAT -> {
                playErrorTone()
                vibrateError()
                speak("Mã vé không hợp lệ")
            }
        }
    }

    private fun playTingTong() {
        audioExecutor.execute {
            var track: AudioTrack? = null
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = tingTongBuffer.size.coerceAtLeast(minBufferSize)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(tingTongBuffer, 0, tingTongBuffer.size)
                track.play()
            } catch (e: Exception) {
                // Fallback to tone generator if AudioTrack fails
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                } catch (ignored: Exception) {}
            }
        }
    }

    private fun playWarningTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Tone error", e)
        }
    }

    private fun playErrorTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300)
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Tone error", e)
        }
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(75)
            }
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Vibrate error", e)
        }
    }

    private fun vibrateError() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 100, 80, 150)
                val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(250)
            }
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Vibrate error", e)
        }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SWB_TTS_${System.currentTimeMillis()}")
        }
    }

    fun release() {
        try {
            audioExecutor.shutdown()
            tts?.stop()
            tts?.shutdown()
            toneGenerator?.release()
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Release error", e)
        }
    }
}
