package com.example.data.repository

import android.content.Context
import com.example.data.api.DictionaryApiService
import com.example.data.db.AppDatabase
import com.example.data.db.VocabEntity
import com.example.data.model.DictionaryResult
import com.example.data.model.LearningDirection
import com.example.data.model.PageAudit
import com.example.data.model.QuizQuestion
import com.example.data.model.StudyStats
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class VocabRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.vocabDao()

    private var cachedPagesAudit: List<PageAudit> = emptyList()

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count < 1212) {
            val (entries, pageAudits) = loadFromAssets()
            cachedPagesAudit = pageAudits
            val entityList = entries.map { VocabEntity.fromDomain(it) }
            for (batch in entityList.chunked(50)) {
                dao.insertAll(batch)
            }
        } else if (cachedPagesAudit.isEmpty()) {
            val (_, pageAudits) = loadFromAssets()
            cachedPagesAudit = pageAudits
        }
    }

    private fun loadFromAssets(): Pair<List<VocabEntry>, List<PageAudit>> {
        val jsonString = context.assets.open("toefl_1212_extracted.json").use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }
        val root = JSONObject(jsonString)

        val pagesArray = root.getJSONArray("pagesSummary")
        val pageAudits = mutableListOf<PageAudit>()
        for (i in 0 until pagesArray.length()) {
            val pObj = pagesArray.getJSONObject(i)
            pageAudits.add(
                PageAudit(
                    page = pObj.getInt("page"),
                    entryCount = pObj.getInt("entryCount"),
                    startNumber = pObj.getInt("startNumber"),
                    endNumber = pObj.getInt("endNumber"),
                    anomalyCount = pObj.getInt("anomalyCount"),
                    status = pObj.getString("status")
                )
            )
        }

        val entriesArray = root.getJSONArray("entries")
        val entries = mutableListOf<VocabEntry>()
        for (i in 0 until entriesArray.length()) {
            val eObj = entriesArray.getJSONObject(i)
            val defsArray = eObj.getJSONArray("definitions")
            val defs = mutableListOf<String>()
            for (j in 0 until defsArray.length()) {
                defs.add(defsArray.getString(j))
            }

            val grammArray = eObj.getJSONArray("grammaticalLabels")
            val gramms = mutableListOf<String>()
            for (j in 0 until grammArray.length()) {
                gramms.add(grammArray.getString(j))
            }

            val dupArray = eObj.getJSONArray("duplicateWithNumbers")
            val dups = mutableListOf<Int>()
            for (j in 0 until dupArray.length()) {
                dups.add(dupArray.getInt(j))
            }

            val exSentence = eObj.optString("exampleSentence", "")
            val dictExArray = eObj.optJSONArray("dictionaryExamples")
            val dictExamples = mutableListOf<String>()
            if (dictExArray != null) {
                for (j in 0 until dictExArray.length()) {
                    dictExamples.add(dictExArray.getString(j))
                }
            } else if (exSentence.isNotBlank()) {
                dictExamples.add(exSentence)
            }

            val dictDefArray = eObj.optJSONArray("dictionaryDefinitions")
            val dictDefs = mutableListOf<String>()
            if (dictDefArray != null) {
                for (j in 0 until dictDefArray.length()) {
                    dictDefs.add(dictDefArray.getString(j))
                }
            }

            val dictSynArray = eObj.optJSONArray("dictionarySynonyms")
            val dictSyns = mutableListOf<String>()
            if (dictSynArray != null) {
                for (j in 0 until dictSynArray.length()) {
                    dictSyns.add(dictSynArray.getString(j))
                }
            }

            val phoneticVal = if (eObj.has("phonetic") && !eObj.isNull("phonetic")) eObj.getString("phonetic") else null

            entries.add(
                VocabEntry(
                    entryNumber = eObj.getInt("entryNumber"),
                    sourcePage = eObj.getInt("sourcePage"),
                    headword = eObj.getString("headword"),
                    rawText = eObj.getString("rawText"),
                    definitions = defs,
                    primaryMeaning = eObj.optString("primaryMeaning", if (defs.isNotEmpty()) defs[0] else ""),
                    grammaticalLabels = gramms,
                    notes = eObj.optString("notes", ""),
                    hasDuplicateHeadword = eObj.optBoolean("hasDuplicateHeadword", false),
                    duplicateWithNumbers = dups,
                    hasAnomaly = eObj.optBoolean("hasAnomaly", false),
                    anomalyNotes = eObj.optString("anomalyNotes", ""),
                    exampleSentence = exSentence,
                    dictionaryExamples = dictExamples,
                    dictionaryDefinitions = dictDefs,
                    dictionarySynonyms = dictSyns,
                    phonetic = phoneticVal
                )
            )
        }
        return Pair(entries, pageAudits)
    }

    fun getAllEntries(): Flow<List<VocabEntry>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getAllEntriesDirect(): List<VocabEntry> = withContext(Dispatchers.IO) {
        dao.getAllDirect().map { it.toDomain() }
    }

    fun getEntryByNumber(num: Int): Flow<VocabEntry?> = dao.getByNumber(num).map { it?.toDomain() }

    suspend fun getEntryByNumberDirect(num: Int): VocabEntry? = dao.getByNumberDirect(num)?.toDomain()

    fun getEntriesByPage(page: Int): Flow<List<VocabEntry>> = dao.getByPage(page).map { list -> list.map { it.toDomain() } }

    fun search(query: String): Flow<List<VocabEntry>> = dao.search(query).map { list -> list.map { it.toDomain() } }

    fun getDifficultEntries(): Flow<List<VocabEntry>> = dao.getDifficultEntries().map { list -> list.map { it.toDomain() } }

    fun getBookmarkedEntries(): Flow<List<VocabEntry>> = dao.getBookmarkedEntries().map { list -> list.map { it.toDomain() } }

    fun getAnomalyEntries(): Flow<List<VocabEntry>> = dao.getAnomalyEntries().map { list -> list.map { it.toDomain() } }

    fun getFrequentlyForgotten(): Flow<List<VocabEntry>> = dao.getFrequentlyForgottenEntries().map { list -> list.map { it.toDomain() } }

    suspend fun getDueEntries(currentTime: Long): List<VocabEntry> = withContext(Dispatchers.IO) {
        dao.getDueEntriesDirect(currentTime).map { it.toDomain() }
    }

    suspend fun getNewEntries(limit: Int): List<VocabEntry> = withContext(Dispatchers.IO) {
        dao.getNewEntriesDirect(limit).map { it.toDomain() }
    }

    fun getPagesAudit(): List<PageAudit> {
        if (cachedPagesAudit.isEmpty()) {
            val (_, audits) = loadFromAssets()
            cachedPagesAudit = audits
        }
        return cachedPagesAudit
    }

    suspend fun toggleDifficult(entryNumber: Int, isDifficult: Boolean) = withContext(Dispatchers.IO) {
        dao.updateDifficult(entryNumber, isDifficult)
    }

    suspend fun toggleBookmark(entryNumber: Int, isBookmarked: Boolean) = withContext(Dispatchers.IO) {
        dao.updateBookmarked(entryNumber, isBookmarked)
    }

    suspend fun updateVerification(
        entryNumber: Int,
        status: UserVerificationStatus,
        correction: String?,
        note: String?
    ) = withContext(Dispatchers.IO) {
        dao.updateVerification(entryNumber, status.name, correction, note)
    }

    suspend fun recordStudyResult(entry: VocabEntry, isCorrect: Boolean, rating: Int = if (isCorrect) 2 else 0) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        var easeFactor = entry.easeFactor
        var repetitionCount = entry.repetitionCount
        var forgottenCount = entry.forgottenCount
        var intervalDays = entry.intervalDays
        val reviewCount = entry.reviewCount + 1

        if (isCorrect) {
            repetitionCount += 1
            intervalDays = when (repetitionCount) {
                1 -> 1
                2 -> 3
                3 -> 6
                else -> (intervalDays * easeFactor).toInt().coerceAtLeast(intervalDays + 1)
            }
            easeFactor += (0.1f - (3 - rating) * (0.08f + (3 - rating) * 0.02f))
            if (easeFactor < 1.3f) easeFactor = 1.3f
        } else {
            repetitionCount = 0
            forgottenCount += 1
            intervalDays = 1
            easeFactor = (easeFactor - 0.2f).coerceAtLeast(1.3f)
        }

        val studyStatus = when {
            repetitionCount >= 5 -> StudyStatus.MASTERED
            repetitionCount >= 2 -> StudyStatus.REVIEWING
            else -> StudyStatus.LEARNING
        }

        val nextReview = now + (intervalDays * oneDayMillis)

        dao.updateStudyProgress(
            entryNumber = entry.entryNumber,
            studyStatus = studyStatus.name,
            repetitionCount = repetitionCount,
            reviewCount = reviewCount,
            forgottenCount = forgottenCount,
            easeFactor = easeFactor,
            intervalDays = intervalDays,
            nextReview = nextReview,
            lastReviewed = now
        )
    }

    fun createQuestion(
        target: VocabEntry,
        allEntries: List<VocabEntry>,
        direction: LearningDirection
    ): QuizQuestion {
        val distractors = allEntries
            .filter { it.entryNumber != target.entryNumber && it.headword.lowercase() != target.headword.lowercase() }
            .shuffled()
            .take(3)

        return when (direction) {
            LearningDirection.RECOGNITION -> {
                val correct = target.primaryMeaning.ifBlank { target.definitions.firstOrNull() ?: target.headword }
                val options = (distractors.map { it.primaryMeaning.ifBlank { it.definitions.firstOrNull() ?: it.headword } } + correct).shuffled()
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    direction = direction,
                    prompt = "Select the correct meaning for:",
                    targetWord = target.headword,
                    options = options,
                    correctAnswer = correct,
                    explanation = "'${target.headword}' (${target.grammaticalLabels.joinToString()}) means: ${target.primaryMeaning}",
                    sourceDefinition = target.rawText,
                    grammaticalLabel = target.grammaticalLabels.firstOrNull()
                )
            }
            LearningDirection.REVERSE_RECALL -> {
                val correct = target.headword
                val options = (distractors.map { it.headword } + correct).shuffled()
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    direction = direction,
                    prompt = "Which vocabulary word matches this definition?",
                    targetWord = target.primaryMeaning,
                    options = options,
                    correctAnswer = correct,
                    explanation = "'${target.headword}' corresponds to: ${target.primaryMeaning} (PDF page ${target.sourcePage})",
                    sourceDefinition = target.rawText,
                    grammaticalLabel = target.grammaticalLabels.firstOrNull()
                )
            }
            LearningDirection.SPELLING -> {
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    direction = direction,
                    prompt = "Spell the word that means: \"${target.primaryMeaning}\"",
                    targetWord = target.headword,
                    options = emptyList(),
                    correctAnswer = target.headword.trim().lowercase(),
                    explanation = "Correct spelling: '${target.headword}'. Source entry #${target.entryNumber} on page ${target.sourcePage}.",
                    sourceDefinition = target.rawText,
                    grammaticalLabel = target.grammaticalLabels.firstOrNull()
                )
            }
            LearningDirection.CONTEXT -> {
                val sentence = generateContextSentence(target)
                val correct = target.headword
                val options = (distractors.map { it.headword } + correct).shuffled()
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    direction = direction,
                    prompt = "Fill in the blank in the academic context:",
                    targetWord = sentence,
                    options = options,
                    correctAnswer = correct,
                    explanation = "'${target.headword}' properly completes the context. Meaning: ${target.primaryMeaning}",
                    sourceDefinition = target.rawText,
                    grammaticalLabel = target.grammaticalLabels.firstOrNull()
                )
            }
            LearningDirection.SYNONYM -> {
                val dictSyn = target.dictionarySynonyms.firstOrNull()
                val targetSyn = dictSyn ?: target.definitions.firstOrNull() ?: target.primaryMeaning
                val otherMeanings = distractors.map { it.primaryMeaning }.shuffled().take(3)
                val options = (otherMeanings + targetSyn).shuffled()
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    direction = direction,
                    prompt = "Identify the closest synonym for '${target.headword}':",
                    targetWord = target.headword,
                    options = options,
                    correctAnswer = targetSyn,
                    explanation = "A recognized synonym for '${target.headword}' is '${targetSyn}'.",
                    sourceDefinition = target.rawText,
                    grammaticalLabel = target.grammaticalLabels.firstOrNull()
                )
            }
            LearningDirection.PRODUCTION -> {
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    direction = direction,
                    prompt = "Write an authentic sentence demonstrating '${target.headword}' (${target.primaryMeaning}):",
                    targetWord = target.headword,
                    options = emptyList(),
                    correctAnswer = target.headword,
                    explanation = "Good usage involves demonstrating the target term in an appropriate academic register. Reference: ${target.exampleSentence.ifBlank { target.rawText }}",
                    sourceDefinition = target.rawText,
                    grammaticalLabel = target.grammaticalLabels.firstOrNull()
                )
            }
            LearningDirection.LISTENING -> {
                val correct = target.headword
                val options = (distractors.map { it.headword } + correct).shuffled()
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    direction = direction,
                    prompt = "Tap audio to listen, then select the spoken vocabulary item:",
                    targetWord = target.headword,
                    options = options,
                    correctAnswer = correct,
                    explanation = "Spoken word: '${target.headword}'. Meaning: ${target.primaryMeaning} (Page ${target.sourcePage})",
                    sourceDefinition = target.rawText,
                    grammaticalLabel = target.grammaticalLabels.firstOrNull()
                )
            }
        }
    }

    private fun generateContextSentence(target: VocabEntry): String {
        if (target.exampleSentence.isNotBlank()) {
            val escapedWord = Regex.escape(target.headword.trim())
            val regex = Regex("(?i)\\b$escapedWord\\b")
            if (regex.containsMatchIn(target.exampleSentence)) {
                return target.exampleSentence.replace(regex, "[_____]")
            }
            val firstWord = target.headword.split(" ").firstOrNull() ?: target.headword
            val regexFirst = Regex("(?i)\\b" + Regex.escape(firstWord) + "\\b")
            if (regexFirst.containsMatchIn(target.exampleSentence)) {
                return target.exampleSentence.replace(regexFirst, "[_____]")
            }
        }
        return "The researchers observed that conditions were optimal to [_____] during the experimental phase."
    }

    suspend fun fetchOnlineDictionary(entry: VocabEntry): Result<DictionaryResult> = withContext(Dispatchers.IO) {
        val result = DictionaryApiService().fetchDefinition(entry.headword)
        if (result.isSuccess) {
            val dict = result.getOrNull()
            if (dict != null) {
                val combinedExamples = (dict.examples + entry.exampleSentence).filter { it.isNotBlank() }.distinct()
                val combinedSynonyms = (dict.synonyms + entry.dictionarySynonyms + entry.definitions).filter { it.isNotBlank() && it.lowercase() != entry.headword.lowercase() }.distinct()
                dao.updateDictionaryData(
                    entryNumber = entry.entryNumber,
                    examples = combinedExamples,
                    definitions = dict.definitions,
                    synonyms = combinedSynonyms,
                    phonetic = dict.phonetic
                )
            }
        }
        result
    }

    fun computeStats(entries: List<VocabEntry>): StudyStats {
        val total = entries.size
        var verified = 0
        var unverified = 0
        var questionable = 0
        var corrected = 0
        var newCount = 0
        var learningCount = 0
        var reviewingCount = 0
        var masteredCount = 0
        var dueCount = 0
        var forgottenCount = 0
        var difficultCount = 0
        var bookmarkedCount = 0

        val now = System.currentTimeMillis()

        for (e in entries) {
            when (e.userStatus) {
                UserVerificationStatus.VERIFIED -> verified++
                UserVerificationStatus.UNVERIFIED -> unverified++
                UserVerificationStatus.QUESTIONABLE -> questionable++
                UserVerificationStatus.CORRECTED -> corrected++
                UserVerificationStatus.NEEDS_EXPLANATION -> questionable++
            }
            when (e.studyStatus) {
                StudyStatus.NEW -> newCount++
                StudyStatus.LEARNING -> learningCount++
                StudyStatus.REVIEWING -> reviewingCount++
                StudyStatus.MASTERED -> masteredCount++
            }
            if (e.studyStatus != StudyStatus.NEW && e.nextReviewTimestamp <= now) {
                dueCount++
            }
            if (e.forgottenCount > 0) {
                forgottenCount++
            }
            if (e.isDifficult) {
                difficultCount++
            }
            if (e.isBookmarked) {
                bookmarkedCount++
            }
        }

        val learnedTotal = learningCount + reviewingCount + masteredCount
        val completion = if (total > 0) (learnedTotal.toFloat() / total) * 100f else 0f

        return StudyStats(
            totalEntries = total,
            verifiedEntries = verified,
            unverifiedEntries = unverified,
            questionableEntries = questionable,
            correctedEntries = corrected,
            newCount = newCount,
            learningCount = learningCount,
            reviewingCount = reviewingCount,
            masteredCount = masteredCount,
            dueCount = dueCount,
            forgottenCount = forgottenCount,
            difficultCount = difficultCount,
            bookmarkedCount = bookmarkedCount,
            overallCompletionPct = completion
        )
    }
}
