package com.example.util

import com.example.data.model.FsrsRating
import com.example.data.model.FsrsSchedulePreview
import com.example.data.model.FsrsState
import com.example.data.model.VocabEntry
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Modern Free Spaced Repetition Scheduler (FSRS) implementation.
 *
 * Implements adaptive spaced repetition based on memory stability (S),
 * item difficulty (D), and retrievability (R).
 *
 * Features:
 * - 4 distinct recall outcomes: Again, Hard, Good, Easy.
 * - Configurable desired retention rate (default 90%).
 * - Preview of scheduled intervals for each rating.
 * - Graceful overdue handling for missed days.
 */
object FsrsScheduler {

    // Default FSRS parameters
    private const val FACTOR_RETRIEVAL = 0.9f
    private val LN_RETRIEVAL = ln(FACTOR_RETRIEVAL.toDouble()).toFloat()

    // Initial stability values for new cards by rating: Again=0.4, Hard=1.0, Good=2.5, Easy=5.0 days
    private val INITIAL_STABILITY = mapOf(
        FsrsRating.AGAIN to 0.4f,
        FsrsRating.HARD to 1.2f,
        FsrsRating.GOOD to 3.0f,
        FsrsRating.EASY to 7.0f
    )

    /**
     * Calculates the memory retrievability (probability of recall) after elapsed days.
     */
    fun calculateRetrievability(elapsedDays: Float, stability: Float): Float {
        if (stability <= 0f) return 0f
        return exp(LN_RETRIEVAL * (elapsedDays / stability)).coerceIn(0f, 1f)
    }

    fun calculateRetrievability(stability: Float, elapsedDays: Int): Float =
        calculateRetrievability(elapsedDays.toFloat(), stability)

    /**
     * Calculates next interval in days given stability and desired retention (e.g. 0.90).
     */
    fun calculateInterval(stability: Float, desiredRetention: Float = 0.90f): Int {
        if (stability <= 0f) return 1
        val r = desiredRetention.coerceIn(0.80f, 0.95f)
        val intervalFloat = stability * (ln(r.toDouble()) / ln(FACTOR_RETRIEVAL.toDouble())).toFloat()
        return max(1, intervalFloat.roundToInt())
    }

    /**
     * Schedules the next review for a vocabulary entry after a given rating.
     */
    fun calculateNextReview(
        entry: VocabEntry,
        rating: FsrsRating,
        desiredRetention: Float = 0.90f,
        currentTimestamp: Long = System.currentTimeMillis()
    ): FsrsScheduleResult = schedule(entry, rating, desiredRetention, currentTimestamp)

    fun schedule(
        entry: VocabEntry,
        rating: FsrsRating,
        desiredRetention: Float = 0.90f,
        currentTimestamp: Long = System.currentTimeMillis()
    ): FsrsScheduleResult {
        val now = currentTimestamp
        val lastReviewed = entry.lastReviewedTimestamp ?: now
        val elapsedDays = max(0f, (now - lastReviewed).toFloat() / (1000f * 60f * 60f * 24f))

        val isNew = entry.fsrsStability <= 0f || entry.repetitionCount == 0
        var newStability: Float
        var newDifficulty: Float
        var newState: Int
        var newLapses = entry.fsrsLapses

        if (isNew) {
            // First time encountering this card
            newStability = INITIAL_STABILITY[rating] ?: 2.5f
            newDifficulty = when (rating) {
                FsrsRating.AGAIN -> 8.0f
                FsrsRating.HARD -> 6.5f
                FsrsRating.GOOD -> 5.0f
                FsrsRating.EASY -> 3.0f
            }
            newState = if (rating == FsrsRating.AGAIN) FsrsState.LEARNING.value else FsrsState.REVIEW.value
            if (rating == FsrsRating.AGAIN) newLapses += 1
        } else {
            val oldD = entry.fsrsDifficulty.coerceIn(1.0f, 10.0f)
            val oldS = max(0.1f, entry.fsrsStability)
            val retrievability = calculateRetrievability(elapsedDays, oldS)

            // Update difficulty
            val deltaD = - (rating.value - 3) * 0.7f
            newDifficulty = (oldD + deltaD).coerceIn(1.0f, 10.0f)

            when (rating) {
                FsrsRating.AGAIN -> {
                    // Forgotten: decrease stability, increment lapses
                    newStability = max(0.4f, oldS * 0.25f)
                    newState = FsrsState.RELEARNING.value
                    newLapses += 1
                }
                FsrsRating.HARD -> {
                    // Recalled with effort
                    val hardMultiplier = 1f + (0.15f * (11f - newDifficulty) * oldS.pow(-0.2f) * exp(0.6 * (1.0 - retrievability))).toFloat()
                    newStability = max(oldS * 1.1f, oldS * hardMultiplier)
                    newState = FsrsState.REVIEW.value
                }
                FsrsRating.GOOD -> {
                    // Recalled successfully
                    val goodMultiplier = 1f + (0.35f * (11f - newDifficulty) * oldS.pow(-0.2f) * exp(0.8 * (1.0 - retrievability))).toFloat()
                    newStability = max(oldS * 1.25f, oldS * goodMultiplier)
                    newState = FsrsState.REVIEW.value
                }
                FsrsRating.EASY -> {
                    // Recalled easily
                    val easyMultiplier = 1f + (0.65f * (11f - newDifficulty) * oldS.pow(-0.2f) * exp(1.0 * (1.0 - retrievability))).toFloat()
                    newStability = max(oldS * 1.5f, oldS * easyMultiplier * 1.3f)
                    newState = FsrsState.REVIEW.value
                }
            }
        }

        val intervalDays = if (rating == FsrsRating.AGAIN) {
            1
        } else {
            calculateInterval(newStability, desiredRetention)
        }

        val nextReviewTimestamp = now + (intervalDays * 24L * 60L * 60L * 1000L)

        return FsrsScheduleResult(
            newStability = newStability,
            newDifficulty = newDifficulty,
            newState = newState,
            newLapses = newLapses,
            intervalDays = intervalDays,
            nextReviewTimestamp = nextReviewTimestamp,
            lastRating = rating.value
        )
    }

    /**
     * Generates interval previews for each of the 4 rating buttons for UI display.
     */
    fun getRatingPreviews(
        entry: VocabEntry,
        desiredRetention: Float = 0.90f,
        currentTimestamp: Long = System.currentTimeMillis()
    ): Map<FsrsRating, FsrsSchedulePreview> =
        previewIntervals(entry, desiredRetention, currentTimestamp).associateBy { it.rating }

    fun previewIntervals(
        entry: VocabEntry,
        desiredRetention: Float = 0.90f,
        currentTimestamp: Long = System.currentTimeMillis()
    ): List<FsrsSchedulePreview> {
        return FsrsRating.values().map { rating ->
            val result = schedule(entry, rating, desiredRetention, currentTimestamp)
            val label = formatInterval(result.intervalDays, rating)
            FsrsSchedulePreview(
                rating = rating,
                intervalText = label,
                intervalDays = result.intervalDays,
                newStability = result.newStability,
                newDifficulty = result.newDifficulty
            )
        }
    }

    private fun formatInterval(days: Int, rating: FsrsRating): String {
        return when {
            rating == FsrsRating.AGAIN -> "10m"
            days <= 1 -> "1d"
            days < 30 -> "${days}d"
            days < 365 -> "${days / 30}mo"
            else -> "${days / 365}y"
        }
    }
}

data class FsrsScheduleResult(
    val newStability: Float,
    val newDifficulty: Float,
    val newState: Int,
    val newLapses: Int,
    val intervalDays: Int,
    val nextReviewTimestamp: Long,
    val lastRating: Int
)
