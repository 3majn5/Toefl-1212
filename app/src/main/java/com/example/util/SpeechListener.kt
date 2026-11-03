package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Thin wrapper over Android's [SpeechRecognizer], exposed as Compose state.
 *
 * - [startContinuous] keeps listening across the recognizer's natural pauses (it stops after a
 *   second or two of silence) by restarting until [stop] is called, accumulating the transcript.
 * - [startSingle] listens for one utterance and returns the recognizer's ranked guesses,
 *   which the pronunciation check compares with the target word.
 */
class SpeechListener(private val context: Context) {
    var isListening by mutableStateOf(false)
        private set
    /** Finished segments plus the in-progress partial result. */
    var transcript by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    /** Microphone loudness 0..1 for the listening animation. */
    var level by mutableStateOf(0f)
        private set

    val isAvailable: Boolean get() = SpeechRecognizer.isRecognitionAvailable(context)

    private var recognizer: SpeechRecognizer? = null
    private var continuous = false
    /** True from stop() until the final continuous segment arrives, so it isn't lost. */
    private var finishing = false
    private val segments = mutableListOf<String>()
    private var onSingleResult: ((List<String>) -> Unit)? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    /** Safety net: if the recognizer never calls back, end the single listen anyway. */
    private val singleTimeout = Runnable {
        if (!continuous && onSingleResult != null) {
            runCatching { recognizer?.cancel() }
            isListening = false
            level = 0f
            error = "Didn't catch that — try again a little louder."
            onSingleResult?.invoke(emptyList())
            onSingleResult = null
        }
    }

    /** Offline first (private, no data); switches to online once if no offline English pack exists. */
    private var preferOffline = true

    private fun intent(maxResults: Int) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { error = null }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) { level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) }
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { level = 0f }
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            if (!continuous && !finishing) return
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            transcript = (segments + partial).filter { it.isNotBlank() }.joinToString(" ")
        }

        override fun onResults(results: Bundle?) {
            val guesses = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
            if (continuous || finishing) {
                guesses.firstOrNull()?.takeIf { it.isNotBlank() }?.let { segments += it }
                transcript = segments.joinToString(" ")
                finishing = false
                restart()
            } else {
                handler.removeCallbacks(singleTimeout)
                isListening = false
                onSingleResult?.invoke(guesses)
                onSingleResult = null
            }
        }

        override fun onError(code: Int) {
            level = 0f
            // 12 = language not supported, 13 = language pack unavailable: retry online once
            if ((code == 12 || code == 13) && preferOffline) {
                preferOffline = false
                runCatching { recognizer?.startListening(intent(if (continuous) 1 else 5)) }
                return
            }
            val silence = code == SpeechRecognizer.ERROR_NO_MATCH || code == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            if (finishing) {
                finishing = false
                return
            }
            if (continuous && silence) {
                restart()
                return
            }
            handler.removeCallbacks(singleTimeout)
            isListening = false
            continuous = false
            error = when (code) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed."
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                    "Speech recognition needs a connection (or an offline English speech pack)."
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Didn't catch that — try again a little louder."
                12, 13 -> "English speech recognition isn't installed. Add it in Settings → System → Languages → On-device speech, or type instead."
                SpeechRecognizer.ERROR_AUDIO -> "The microphone couldn't be opened."
                else -> "Speech recognition stopped (code $code). Try again."
            }
            onSingleResult?.invoke(emptyList())
            onSingleResult = null
        }
    }

    private fun ensureRecognizer(): SpeechRecognizer? {
        if (recognizer == null && isAvailable) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply { setRecognitionListener(listener) }
        }
        return recognizer
    }

    private fun restart() {
        if (!continuous) return
        runCatching { recognizer?.startListening(intent(1)) }
    }

    fun startContinuous() {
        val r = ensureRecognizer() ?: run { error = "Speech recognition isn't available on this device."; return }
        finishing = false
        segments.clear()
        transcript = ""
        continuous = true
        isListening = true
        error = null
        r.startListening(intent(1))
    }

    fun startSingle(onResult: (List<String>) -> Unit) {
        val r = ensureRecognizer() ?: run { error = "Speech recognition isn't available on this device."; onResult(emptyList()); return }
        continuous = false
        onSingleResult = onResult
        isListening = true
        error = null
        r.startListening(intent(5))
        handler.removeCallbacks(singleTimeout)
        handler.postDelayed(singleTimeout, 12_000)
    }

    /** Stops listening; the final segment arrives via onResults shortly after. */
    fun stop() {
        finishing = continuous
        continuous = false
        isListening = false
        level = 0f
        runCatching { recognizer?.stopListening() }
    }

    fun reset() {
        segments.clear()
        transcript = ""
        error = null
    }

    fun destroy() {
        handler.removeCallbacks(singleTimeout)
        continuous = false
        runCatching { recognizer?.destroy() }
        recognizer = null
    }
}

@Composable
fun rememberSpeechListener(): SpeechListener {
    val context = LocalContext.current
    val listener = remember { SpeechListener(context.applicationContext) }
    DisposableEffect(Unit) { onDispose { listener.destroy() } }
    return listener
}
