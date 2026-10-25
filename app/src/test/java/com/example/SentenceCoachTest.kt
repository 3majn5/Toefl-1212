package com.example

import com.example.data.model.VocabEntry
import com.example.util.SentenceCoachAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SentenceCoachTest {

    private val accelerateEntry = VocabEntry(
        entryNumber = 1,
        sourcePage = 1,
        headword = "accelerate",
        rawText = "1. accelerate (v.) to increase speed",
        definitions = listOf("to increase speed"),
        primaryMeaning = "to increase speed",
        grammaticalLabels = listOf("v.")
    )

    private val consistEntry = VocabEntry(
        entryNumber = 15,
        sourcePage = 2,
        headword = "consist of",
        rawText = "15. consist of (v.) to be composed of",
        definitions = listOf("to be composed of"),
        primaryMeaning = "to be composed of",
        grammaticalLabels = listOf("v.")
    )

    @Test
    fun testValidAcademicSentence() {
        val sentence = "The university decided to accelerate its climate research programs across departments."
        val result = SentenceCoachAnalyzer.evaluate(sentence, accelerateEntry)

        assertTrue(result.isTargetWordPresent)
        assertTrue(result.estimatedAcademicScore >= 4)
        assertTrue(result.strengths.isNotEmpty())
        assertNotNull(result.modelSentence)
    }

    @Test
    fun testMissingTargetWordPenalty() {
        val sentence = "The committee met to discuss new departmental regulations."
        val result = SentenceCoachAnalyzer.evaluate(sentence, accelerateEntry)

        assertFalse(result.isTargetWordPresent)
        assertTrue(result.suggestions.any { it.contains("Ensure the target word") })
        assertTrue(result.estimatedAcademicScore < 4)
    }

    @Test
    fun testPassiveVoiceTrapDetection() {
        val flawedSentence = "The scientific committee is consisted of five senior faculty members."
        val result = SentenceCoachAnalyzer.evaluate(flawedSentence, consistEntry)

        assertTrue(result.suggestions.any { it.contains("Grammar Trap") || it.contains("consist of") })
    }
}
