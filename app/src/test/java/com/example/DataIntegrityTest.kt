package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.StudyPlan
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import com.example.util.AcademicVocabularyEnricher
import com.example.util.BackupRestoreManager
import com.example.util.ConfusableWordsData
import com.example.util.Toefl2026PracticeData
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class DataIntegrityTest {

    @Test
    fun testExtractedJsonIntegrity() {
        val file = File("src/main/assets/toefl_1212_extracted.json")
        assertTrue("Asset file must exist", file.exists())

        val jsonString = file.readText()
        val root = JSONObject(jsonString)

        assertEquals("Total entries must be 1,212", 1212, root.getInt("totalEntries"))
        assertEquals("Total pages must be 46", 46, root.getInt("totalPages"))
        assertEquals("Total anomalies must be 27", 27, root.getInt("totalAnomalies"))

        val entriesArray = root.getJSONArray("entries")
        assertEquals(1212, entriesArray.length())

        val numbers = mutableSetOf<Int>()
        val pages = mutableSetOf<Int>()
        var anomalyCount = 0

        for (i in 0 until entriesArray.length()) {
            val item = entriesArray.getJSONObject(i)
            val num = item.getInt("entryNumber")
            val page = item.getInt("sourcePage")
            val headword = item.getString("headword")
            val hasAnomaly = item.optBoolean("hasAnomaly", false)

            assertTrue("Headword cannot be blank for entry #$num", headword.isNotBlank())
            assertTrue("Entry number must be within 1..1212", num in 1..1212)
            assertTrue("Source page must be within 1..46", page in 1..46)

            numbers.add(num)
            pages.add(page)
            if (hasAnomaly) anomalyCount++
        }

        assertEquals("All 1,212 entry numbers must be present without holes", 1212, numbers.size)
        assertEquals("All 46 source pages must be represented", 46, pages.size)
        assertEquals("Exactly 27 anomalies documented", 27, anomalyCount)
    }

    @Test
    fun testAll1212LabCoverageAndEnrichment() {
        val file = File("src/main/assets/toefl_1212_extracted.json")
        val root = JSONObject(file.readText())
        val entriesArray = root.getJSONArray("entries")

        val allEntries = (0 until entriesArray.length()).map { i ->
            val item = entriesArray.getJSONObject(i)
            val defs = mutableListOf<String>()
            val defsArr = item.optJSONArray("definitions")
            if (defsArr != null) {
                for (j in 0 until defsArr.length()) defs.add(defsArr.getString(j))
            }
            val labels = mutableListOf<String>()
            val labelsArr = item.optJSONArray("grammaticalLabels")
            if (labelsArr != null) {
                for (j in 0 until labelsArr.length()) labels.add(labelsArr.getString(j))
            }
            VocabEntry(
                entryNumber = item.getInt("entryNumber"),
                sourcePage = item.getInt("sourcePage"),
                headword = item.getString("headword"),
                rawText = item.optString("rawText", ""),
                definitions = defs,
                primaryMeaning = item.optString("primaryMeaning", ""),
                grammaticalLabels = labels,
                exampleSentence = item.optString("exampleSentence", ""),
                hasAnomaly = item.optBoolean("hasAnomaly", false),
                anomalyNotes = item.optString("anomalyNotes", "")
            )
        }

        assertEquals(1212, allEntries.size)

        // Verify all 1,212 entries produce complete enrichments and lab exercises
        for (entry in allEntries) {
            val enriched = AcademicVocabularyEnricher.enrich(entry)
            assertTrue("Collocations must not be empty for #${entry.entryNumber}", enriched.collocations.isNotEmpty())
            assertTrue("Word family must not be empty for #${entry.entryNumber}", enriched.wordFamily.isNotEmpty())
            assertTrue("Grammar patterns must not be empty for #${entry.entryNumber}", enriched.grammarPatterns.isNotEmpty())
            assertTrue("Common mistakes/tips must not be blank for #${entry.entryNumber}", enriched.commonMistakes.isNotBlank())

            val cloze = Toefl2026PracticeData.buildClozeForEntry(entry)
            assertEquals(entry.headword.trim(), cloze.targetWord)
            assertTrue(cloze.obscuredWord.contains("_"))
            assertTrue(cloze.textWithBlanks.isNotBlank())

            val reading = Toefl2026PracticeData.buildReadingQuestionForEntry(entry, allEntries)
            assertEquals(4, reading.options.size)
            assertTrue(reading.correctIndex in 0..3)
            assertEquals(entry.primaryMeaning, reading.options[reading.correctIndex])
        }

        val contrastPairs = ConfusableWordsData.build1212ContrastPairs(allEntries)
        assertEquals(1212, contrastPairs.size)
    }

    @Test
    fun testBackupRestoreRoundtrip() {
        val testEntries = (1..50).map { num ->
            VocabEntry(
                entryNumber = num,
                sourcePage = (num / 25) + 1,
                headword = "word_$num",
                rawText = "$num. word_$num definition",
                definitions = listOf("definition for $num"),
                primaryMeaning = "definition for $num",
                grammaticalLabels = listOf("adj."),
                userStatus = if (num % 5 == 0) UserVerificationStatus.VERIFIED else UserVerificationStatus.UNVERIFIED,
                userNote = if (num == 1) "Important note" else null,
                isBookmarked = num % 3 == 0,
                studyStatus = if (num < 10) StudyStatus.MASTERED else StudyStatus.NEW,
                fsrsStability = 4.2f,
                fsrsDifficulty = 3.5f,
                recognitionAttempts = 5,
                recognitionSuccesses = 4
            )
        }

        val plan = StudyPlan(
            targetExamTimestamp = System.currentTimeMillis() + 30L * 86400000L,
            dailyMinutes = 25,
            dailyNewWords = 15,
            desiredRetention = 0.92f,
            streakDays = 7
        )

        val exportedJson = BackupRestoreManager.exportToJson(testEntries, plan)
        assertTrue(exportedJson.isNotBlank())
        assertTrue(exportedJson.contains("TOEFL 1212"))

        val exportedCsv = BackupRestoreManager.exportToCsv(testEntries)
        assertTrue(exportedCsv.isNotBlank())
        assertTrue(exportedCsv.startsWith("Entry Number,Source Page,Headword"))

        val restoredResult = BackupRestoreManager.importFromJson(exportedJson, testEntries)
        assertTrue(restoredResult.isSuccess)

        val backupData = restoredResult.getOrThrow()
        assertEquals(50, backupData.totalEntriesCount)
        assertEquals(1, backupData.userNotesCount)
        assertNotNull(backupData.studyPlan)
        assertEquals(25, backupData.studyPlan!!.dailyMinutes)
        assertEquals(15, backupData.studyPlan!!.dailyNewWords)
        assertEquals(0.92f, backupData.studyPlan!!.desiredRetention, 0.001f)

        val firstRestored = backupData.entries.find { it.entryNumber == 1 }
        assertNotNull(firstRestored)
        assertEquals("Important note", firstRestored!!.userNote)
        assertEquals(4.2f, firstRestored.fsrsStability, 0.001f)
        assertEquals(4, firstRestored.recognitionSuccesses)
    }
}
