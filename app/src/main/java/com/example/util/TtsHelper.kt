package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) {
    private var tts: TextToSpeech? = null
    var isReady: Boolean = false
        private set

    var currentRate: Float = 1.0f
        private set

    var currentLocale: Locale = Locale.US
        private set

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        tts?.let {
                            val result = it.setLanguage(currentLocale)
                            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                                isReady = true
                                it.setSpeechRate(currentRate)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSpeedRate(rate: Float) {
        currentRate = rate.coerceIn(0.5f, 2.0f)
        try {
            tts?.setSpeechRate(currentRate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVoiceLocale(locale: Locale): Boolean {
        currentLocale = locale
        return try {
            val res = tts?.setLanguage(locale)
            res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
        } catch (e: Exception) {
            false
        }
    }

    fun speak(text: String, rate: Float = currentRate) {
        try {
            if (isReady && tts != null) {
                tts?.setSpeechRate(rate)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocab_utterance_${System.currentTimeMillis()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            tts = null
            isReady = false
        }
    }
}
