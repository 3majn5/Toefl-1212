package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Instant sensory feedback: short synthesized chimes and haptic taps.
 *
 * Sounds are generated in code (no audio assets). The "correct" chime climbs a pentatonic
 * scale as the combo grows, so a streak of right answers literally sounds like it's building.
 */
object FeedbackFx {
    private const val SAMPLE_RATE = 44_100
    private const val PREFS = "toefl_1212_study_prefs"
    private const val KEY_SOUND = "fx_sound_enabled"
    private const val KEY_HAPTICS = "fx_haptics_enabled"

    private val audioThread = Executors.newSingleThreadExecutor()
    private var appContext: Context? = null

    var soundEnabled: Boolean = true
        private set
    var hapticsEnabled: Boolean = true
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        soundEnabled = prefs.getBoolean(KEY_SOUND, true)
        hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putBoolean(KEY_SOUND, enabled)?.apply()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        hapticsEnabled = enabled
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putBoolean(KEY_HAPTICS, enabled)?.apply()
        if (enabled) tap()
    }

    // ── Public cues ──────────────────────────────────────────────

    /** Bright two-note chime; pitch rises with [combo]. Lucky answers add a sparkle on top. */
    fun correct(combo: Int = 1, lucky: Boolean = false) {
        val step = PENTATONIC[min(combo - 1, PENTATONIC.lastIndex).coerceAtLeast(0)]
        val base = 659.25 * 2.0.pow(step / 12.0) // starts at E5
        val notes = mutableListOf(Note(base, 0, 110), Note(base * 1.5, 70, 220))
        if (lucky) notes += listOf(Note(base * 2, 160, 160), Note(base * 3, 230, 260, 0.18))
        play(notes)
        vibrate(if (combo >= 5) Haptic.DOUBLE else Haptic.CLICK)
    }

    /** Soft, low "hmm" — clearly different from success but not harsh. */
    fun wrong() {
        play(listOf(Note(311.13, 0, 140, 0.22), Note(233.08, 110, 220, 0.22)))
        vibrate(Haptic.REJECT)
    }

    /** Light tick for flashcard ratings and toggles. */
    fun tap() {
        play(listOf(Note(1046.5, 0, 45, 0.12)))
        vibrate(Haptic.TICK)
    }

    /** Rising major arpeggio for level-ups and chests. */
    fun fanfare() {
        val root = 523.25
        play(listOf(0, 4, 7, 12, 16).mapIndexed { i, semi ->
            Note(root * 2.0.pow(semi / 12.0), i * 85, if (i == 4) 520 else 200)
        })
        vibrate(Haptic.HEAVY)
    }

    /** Short sparkle for badges and quest completions. */
    fun sparkle() {
        play(listOf(Note(1318.5, 0, 90, 0.16), Note(1760.0, 60, 120, 0.16), Note(2093.0, 120, 240, 0.14)))
        vibrate(Haptic.DOUBLE)
    }

    // ── Synthesis ───────────────────────────────────────────────

    private val PENTATONIC = intArrayOf(0, 2, 4, 7, 9, 12, 14, 16, 19, 21, 24)

    private data class Note(val freq: Double, val startMs: Int, val durationMs: Int, val gain: Double = 0.28)

    private fun play(notes: List<Note>) {
        if (!soundEnabled) return
        audioThread.execute {
            runCatching {
                val totalMs = notes.maxOf { it.startMs + it.durationMs } + 20
                val samples = ShortArray(SAMPLE_RATE * totalMs / 1000)
                val mix = DoubleArray(samples.size)
                for (note in notes) {
                    val start = SAMPLE_RATE * note.startMs / 1000
                    val length = SAMPLE_RATE * note.durationMs / 1000
                    for (i in 0 until length) {
                        val idx = start + i
                        if (idx >= mix.size) break
                        val t = i.toDouble() / SAMPLE_RATE
                        // Fast attack, exponential decay: a soft bell rather than a beep
                        val attack = min(1.0, i / (SAMPLE_RATE * 0.004))
                        val envelope = attack * exp(-t * 9.0 / (note.durationMs / 1000.0 * 2.2))
                        val tone = sin(2 * PI * note.freq * t) +
                            0.35 * sin(2 * PI * note.freq * 2 * t) +
                            0.12 * sin(2 * PI * note.freq * 3 * t)
                        mix[idx] += tone * envelope * note.gain
                    }
                }
                for (i in mix.indices) {
                    samples[i] = (mix[i].coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.8).toInt().toShort()
                }
                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep(totalMs.toLong() + 50)
                track.release()
            }
        }
    }

    // ── Haptics ─────────────────────────────────────────────────

    private enum class Haptic { TICK, CLICK, DOUBLE, HEAVY, REJECT }

    private fun vibrator(): Vibrator? {
        val ctx = appContext ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun vibrate(type: Haptic) {
        if (!hapticsEnabled) return
        val v = vibrator()?.takeIf { it.hasVibrator() } ?: return
        runCatching {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val effect = when (type) {
                        Haptic.TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        Haptic.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        Haptic.DOUBLE -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                        Haptic.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        Haptic.REJECT -> VibrationEffect.createWaveform(longArrayOf(0, 35, 60, 35), intArrayOf(0, 140, 0, 90), -1)
                    }
                    v.vibrate(effect)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    val ms = when (type) { Haptic.TICK -> 10L; Haptic.CLICK -> 20L; Haptic.DOUBLE -> 30L; Haptic.HEAVY -> 50L; Haptic.REJECT -> 40L }
                    v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                else -> {
                    @Suppress("DEPRECATION")
                    v.vibrate(if (type == Haptic.HEAVY) 50L else 20L)
                }
            }
        }
    }
}
