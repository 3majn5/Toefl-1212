package com.example

import com.example.data.model.VocabEntry
import com.example.util.SentenceCoachAnalyzer
import com.example.util.SpellingChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpellingCheckerTest {

    @Test
    fun exactAnswerIsAccepted() {
        val result = SpellingChecker.check("  Accommodate ", "accommodate")
        assertEquals(SpellingChecker.Verdict.EXACT, result.verdict)
        assertTrue(result.isAccepted)
    }

    @Test
    fun singleSlipInLongWordIsForgiven() {
        val missing = SpellingChecker.check("acommodate", "accommodate")
        assertEquals(SpellingChecker.Verdict.MINOR_TYPO, missing.verdict)
        assertTrue(missing.isAccepted)
        assertTrue("the missing letter is highlighted", missing.highlightedAnswer.any { it.second })

        val swapped = SpellingChecker.check("recieve", "receive")
        assertTrue("adjacent swap counts as one slip", swapped.isAccepted)
    }

    @Test
    fun shortWordsMustBeExact() {
        val result = SpellingChecker.check("aid", "aim")
        assertFalse(result.isAccepted)
    }

    @Test
    fun differentWordIsRejected() {
        val result = SpellingChecker.check("adopt", "accommodate")
        assertEquals(SpellingChecker.Verdict.WRONG, result.verdict)
    }

    private val analyze = VocabEntry(
        entryNumber = 2029, sourcePage = 1, headword = "analyze", rawText = "", definitions = listOf("to examine"),
        primaryMeaning = "to examine something carefully", grammaticalLabels = listOf("v."),
        wordFamily = listOf("analysis (n.)", "analyst (n.)", "analytical (adj.)"),
        collocations = listOf("analyze data", "carefully analyze", "analyze the results")
    )

    @Test
    fun wordFamilyFormCountsAsUsingTheWord() {
        val result = SentenceCoachAnalyzer.evaluate("A careful analysis of the survey data revealed a clear trend.", analyze)
        assertTrue(result.isTargetWordPresent)
    }

    @Test
    fun verbAfterArticleAndInformalWordsAreFlagged() {
        val result = SentenceCoachAnalyzer.evaluate("The analyze of stuff was really good.", analyze)
        assertTrue(result.suggestions.any { it.contains("verb") })
        assertTrue(result.suggestions.any { it.contains("Informal") })
    }

    @Test
    fun copiedModelSentenceIsFlagged() {
        val entry = analyze.copy(exampleSentence = "Scientists analyze ice cores to learn about past climates.")
        val result = SentenceCoachAnalyzer.evaluate("Scientists analyze ice cores to learn about past climates.", entry)
        assertTrue(result.suggestions.any { it.contains("model sentence") })
    }
}
