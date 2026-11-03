package com.example

import com.example.data.model.VocabEntry
import com.example.util.MistakeNotebook
import com.example.util.SpeakingCoach
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SpeakingAndMistakesTest {

    private fun entry(n: Int, word: String, family: List<String> = emptyList()) = VocabEntry(
        entryNumber = n,
        sourcePage = 1,
        headword = word,
        rawText = word,
        definitions = listOf("meaning of $word"),
        primaryMeaning = "meaning of $word",
        grammaticalLabels = emptyList(),
        exampleSentence = "",
        hasAnomaly = false,
        anomalyNotes = "",
        wordFamily = family
    )

    private val strongAnswer = "I prefer studying in groups because it helps me stay motivated. First, my classmates " +
        "explain difficult ideas in different ways, and this means I understand them faster. For example, last term we " +
        "analyzed a complex article together and everyone contributed a different perspective. In addition, studying " +
        "with others is more enjoyable, so I study for longer. As a result, my grades improved significantly and I felt " +
        "more confident before exams. Overall, I think group study is the most effective approach for me."

    @Test
    fun strongAnswerScoresHighAndFindsTargetWords() {
        val targets = listOf(entry(1, "analyze", listOf("analysis (n.)")), entry(2, "contribute"), entry(3, "perspective"))
        val fb = SpeakingCoach.analyze(strongAnswer, targets, spokenSeconds = 45)
        assertTrue("word count ${fb.wordCount}", fb.wordCount >= 80)
        assertEquals(listOf("analyze", "contribute", "perspective"), fb.targetWordsUsed)
        assertTrue("linking ${fb.linkingWordsUsed}", fb.linkingWordsUsed.size >= 3)
        assertEquals(0, fb.fillerCount)
        assertTrue("score ${fb.score}", fb.score >= 3)
    }

    @Test
    fun shortAnswerWithFillersGetsTips() {
        val fb = SpeakingCoach.analyze("um I like um groups uh because you know it is fun", listOf(entry(1, "analyze")), 45)
        assertTrue(fb.fillerCount >= 4)
        assertTrue(fb.score <= 1)
        assertTrue(fb.tips.any { it.contains("fillers") || it.contains("Cut") })
        assertEquals(listOf("analyze"), fb.targetWordsMissed)
    }

    @Test
    fun emptyTranscriptIsZero() {
        val fb = SpeakingCoach.analyze("", emptyList(), 45)
        assertEquals(0, fb.wordCount)
        assertEquals(0, fb.score)
    }

    @Test
    fun pronunciationScoring() {
        assertEquals(2, SpeakingCoach.pronunciationScore("abandon", listOf("Abandon", "a band on")))
        assertEquals(1, SpeakingCoach.pronunciationScore("abandon", listOf("a band on", "abandon")))
        assertEquals(0, SpeakingCoach.pronunciationScore("abandon", listOf("a band on", "abundant")))
        assertEquals(0, SpeakingCoach.pronunciationScore("abandon", emptyList()))
    }

    @Test
    fun notebookRecordsAndClearsAfterTwoCorrect() {
        val book = MistakeNotebook(FakePrefs())
        assertFalse(book.record(7, isCorrect = true, channel = "SPELLING")) // correct answers alone add nothing
        assertTrue(book.all().isEmpty())

        book.record(7, isCorrect = false, channel = "SPELLING")
        book.record(7, isCorrect = false, channel = "LISTENING")
        val rec = book.all().single()
        assertEquals(2, rec.wrongCount)
        assertEquals(setOf("SPELLING", "LISTENING"), rec.channels)

        assertFalse(book.record(7, isCorrect = true, channel = "SPELLING"))
        assertEquals(1, book.all().single().correctStreak)
        // A miss resets the streak
        book.record(7, isCorrect = false, channel = "SPELLING")
        assertEquals(0, book.all().single().correctStreak)

        book.record(7, isCorrect = true, channel = "SPELLING")
        assertTrue(book.record(7, isCorrect = true, channel = "SPELLING"))
        assertTrue(book.all().isEmpty())
    }
}
