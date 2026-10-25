package com.example

import com.example.data.model.FsrsRating
import com.example.data.model.VocabEntry
import com.example.util.FsrsScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FsrsSchedulerTest {

    private val sampleEntry = VocabEntry(
        entryNumber = 1,
        sourcePage = 1,
        headword = "accelerate",
        rawText = "1. accelerate (v.) to increase speed",
        definitions = listOf("to increase speed"),
        primaryMeaning = "to increase speed",
        grammaticalLabels = listOf("v.")
    )

    @Test
    fun testInitialRatingScheduling() {
        val againRes = FsrsScheduler.calculateNextReview(sampleEntry, FsrsRating.AGAIN)
        assertEquals(1, againRes.intervalDays) // 1 day interval for again
        assertTrue(againRes.newStability > 0f)
        assertTrue(againRes.newDifficulty in 1f..10f)

        val goodRes = FsrsScheduler.calculateNextReview(sampleEntry, FsrsRating.GOOD)
        assertTrue(goodRes.intervalDays >= 1)
        assertTrue(goodRes.newStability > againRes.newStability)
        assertTrue(goodRes.newDifficulty < againRes.newDifficulty)

        val easyRes = FsrsScheduler.calculateNextReview(sampleEntry, FsrsRating.EASY)
        assertTrue(easyRes.intervalDays >= goodRes.intervalDays)
        assertTrue(easyRes.newStability > goodRes.newStability)
    }

    @Test
    fun testRetrievabilityCalculation() {
        val stability = 5f // 5 days stability
        val r0 = FsrsScheduler.calculateRetrievability(stability, 0)
        assertEquals(1.0f, r0, 0.001f)

        val r5 = FsrsScheduler.calculateRetrievability(stability, 5)
        assertEquals(0.9f, r5, 0.05f)

        val r40 = FsrsScheduler.calculateRetrievability(stability, 40)
        assertTrue(r40 < 0.5f)
    }

    @Test
    fun testPreviewsForAllFourRatings() {
        val previews = FsrsScheduler.getRatingPreviews(sampleEntry)
        assertEquals(4, previews.size)
        assertNotNull(previews[FsrsRating.AGAIN])
        assertNotNull(previews[FsrsRating.HARD])
        assertNotNull(previews[FsrsRating.GOOD])
        assertNotNull(previews[FsrsRating.EASY])

        assertTrue(previews[FsrsRating.EASY]!!.intervalDays >= previews[FsrsRating.GOOD]!!.intervalDays)
    }

    @Test
    fun testReviewProgressionIncreasesStability() {
        val step1 = FsrsScheduler.calculateNextReview(sampleEntry, FsrsRating.GOOD)
        val entryAfterStep1 = sampleEntry.copy(
            fsrsStability = step1.newStability,
            fsrsDifficulty = step1.newDifficulty,
            fsrsState = 2,
            repetitionCount = 1,
            lastReviewedTimestamp = System.currentTimeMillis() - 86400000L
        )

        val step2 = FsrsScheduler.calculateNextReview(entryAfterStep1, FsrsRating.GOOD)
        assertTrue(step2.newStability > step1.newStability)
        assertTrue(step2.intervalDays > step1.intervalDays)
    }
}
