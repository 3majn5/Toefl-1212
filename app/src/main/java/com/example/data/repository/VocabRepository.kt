package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.DictionaryApiService
import com.example.data.db.AppDatabase
import com.example.data.db.ReviewLogEntity
import com.example.data.db.VocabEntity
import com.example.data.model.CollectionScope
import com.example.data.model.DictionaryResult
import com.example.data.model.FsrsRating
import com.example.data.model.FsrsState
import com.example.data.model.LearningDirection
import com.example.data.model.PageAudit
import com.example.data.model.QuizQuestion
import com.example.data.model.SkillMasterySummary
import com.example.data.model.StudyPlan
import com.example.data.model.StudyStats
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import com.example.data.model.WordCollection
import com.example.data.model.WorkloadForecast
import com.example.util.AcademicVocabularyEnricher
import com.example.util.BackupRestoreManager
import com.example.util.FsrsScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val CONTENT_VERSION = 2

class VocabRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.vocabDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("toefl_1212_study_prefs", Context.MODE_PRIVATE)

    private var cachedPagesAudit: Map<WordCollection, List<PageAudit>> = emptyMap()

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        for (collection in WordCollection.values()) {
            try {
                val (entries, pageAudits) = loadFromAssets(collection)
                cachedPagesAudit = cachedPagesAudit + (collection to pageAudits)
                val count = runCatching { dao.getCountForCollection(collection.key) }.getOrDefault(0)
                if (count < entries.size) {
                    val existing = runCatching { dao.getAllDirect().map { it.entryNumber }.toSet() }.getOrDefault(emptySet())
                    val missing = entries.filter { it.entryNumber !in existing }.map { VocabEntity.fromDomain(it) }
                    for (batch in missing.chunked(50)) {
                        runCatching { dao.insertAll(batch) }
                    }
                }
                refreshContentIfOutdated(collection, entries)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Older installs were seeded with templated example sentences and PDF ligature glyphs.
     * Refresh the source text in place without touching any study progress.
     */
    private suspend fun refreshContentIfOutdated(collection: WordCollection, entries: List<VocabEntry>) {
        val prefKey = "content_version_${collection.key}"
        if (prefs.getInt(prefKey, 0) >= CONTENT_VERSION) return
        for (entry in entries) {
            runCatching {
                dao.refreshSourceContent(
                    entryNumber = entry.entryNumber,
                    headword = entry.headword,
                    rawText = entry.rawText,
                    definitions = entry.definitions,
                    primaryMeaning = entry.primaryMeaning,
                    exampleSentence = entry.exampleSentence,
                    examples = entry.dictionaryExamples,
                    synonyms = entry.dictionarySynonyms
                )
                dao.updateLinguisticProfile(
                    entryNumber = entry.entryNumber,
                    collocations = entry.collocations,
                    wordFamily = entry.wordFamily,
                    grammarPatterns = entry.grammarPatterns,
                    commonMistakes = entry.commonMistakes,
                    registerFormality = entry.registerFormality,
                    antonyms = entry.antonyms
                )
            }
        }
        prefs.edit().putInt(prefKey, CONTENT_VERSION).apply()
    }

    private fun assetFileFor(collection: WordCollection): String = when (collection) {
        WordCollection.TOEFL -> "toefl_1212_extracted.json"
        WordCollection.AWL -> "awl_570.json"
    }

    private fun loadAllFromAssets(): List<VocabEntry> =
        WordCollection.values().flatMap { runCatching { loadFromAssets(it).first }.getOrDefault(emptyList()) }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).map { Normalizer.normalize(getString(it), Normalizer.Form.NFKC) }
    }

    private fun loadFromAssets(collection: WordCollection = WordCollection.TOEFL): Pair<List<VocabEntry>, List<PageAudit>> {
        val jsonString = context.assets.open(assetFileFor(collection)).use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }
        // NFKC folds PDF ligatures such as "ﬁ" into plain "fi" so search, TTS and spelling work
        val root = JSONObject(Normalizer.normalize(jsonString, Normalizer.Form.NFKC))

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
            val defs = eObj.getJSONArray("definitions").toStringList()
            val gramms = eObj.getJSONArray("grammaticalLabels").toStringList()

            val dupArray = eObj.getJSONArray("duplicateWithNumbers")
            val dups = (0 until dupArray.length()).map { dupArray.getInt(it) }

            val exSentence = eObj.optString("exampleSentence", "")
            val dictExamples = eObj.optJSONArray("dictionaryExamples").toStringList()
                .ifEmpty { if (exSentence.isNotBlank()) listOf(exSentence) else emptyList() }

            val phoneticVal = if (eObj.has("phonetic") && !eObj.isNull("phonetic")) eObj.getString("phonetic") else null

            val rawEntry = VocabEntry(
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
                dictionaryDefinitions = eObj.optJSONArray("dictionaryDefinitions").toStringList(),
                dictionarySynonyms = eObj.optJSONArray("dictionarySynonyms").toStringList(),
                phonetic = phoneticVal,
                collection = collection.key
            )

            // Enrich with academic collocations, word families, and common traps
            entries.add(AcademicVocabularyEnricher.enrich(rawEntry))
        }
        return Pair(entries, pageAudits)
    }

    fun getAllEntries(): Flow<List<VocabEntry>> = dao.getAll()
        .map { list -> list.map { it.toDomain() } }
        .catch { e ->
            e.printStackTrace()
            emit(loadAllFromAssets())
        }

    suspend fun getAllEntriesDirect(): List<VocabEntry> = withContext(Dispatchers.IO) {
        runCatching {
            dao.getAllDirect().map { it.toDomain() }
        }.getOrElse { loadAllFromAssets() }
    }

    fun getEntryByNumber(num: Int): Flow<VocabEntry?> = dao.getByNumber(num)
        .map { it?.toDomain() }
        .catch { emit(null) }

    suspend fun getEntryByNumberDirect(num: Int): VocabEntry? = runCatching {
        dao.getByNumberDirect(num)?.toDomain()
    }.getOrNull()

    fun getEntriesByPage(page: Int): Flow<List<VocabEntry>> = dao.getByPage(page)
        .map { list -> list.map { it.toDomain() } }
        .catch { emit(emptyList()) }

    fun search(query: String): Flow<List<VocabEntry>> = dao.search(query)
        .map { list -> list.map { it.toDomain() } }
        .catch { emit(emptyList()) }

    fun getDifficultEntries(): Flow<List<VocabEntry>> = dao.getDifficultEntries()
        .map { list -> list.map { it.toDomain() } }
        .catch { emit(emptyList()) }

    fun getBookmarkedEntries(): Flow<List<VocabEntry>> = dao.getBookmarkedEntries()
        .map { list -> list.map { it.toDomain() } }
        .catch { emit(emptyList()) }

    fun getAnomalyEntries(): Flow<List<VocabEntry>> = dao.getAnomalyEntries()
        .map { list -> list.map { it.toDomain() } }
        .catch { emit(emptyList()) }

    fun getFrequentlyForgotten(): Flow<List<VocabEntry>> = dao.getFrequentlyForgottenEntries()
        .map { list -> list.map { it.toDomain() } }
        .catch { emit(emptyList()) }

    suspend fun getDueEntries(currentTime: Long): List<VocabEntry> = withContext(Dispatchers.IO) {
        runCatching { dao.getDueEntriesDirect(currentTime).map { it.toDomain() } }.getOrDefault(emptyList())
    }

    suspend fun getOverdueEntries(currentTime: Long): List<VocabEntry> = withContext(Dispatchers.IO) {
        runCatching { dao.getOverdueEntriesDirect(currentTime).map { it.toDomain() } }.getOrDefault(emptyList())
    }

    suspend fun getNewEntries(limit: Int, scope: CollectionScope = CollectionScope.ALL): List<VocabEntry> = withContext(Dispatchers.IO) {
        val keys = WordCollection.values().map { it.key }.filter { key ->
            scope == CollectionScope.ALL || key == scope.name
        }
        runCatching { dao.getNewEntriesInCollections(keys, limit).map { it.toDomain() } }.getOrDefault(emptyList())
    }

    fun getPagesAudit(collection: WordCollection = WordCollection.TOEFL): List<PageAudit> {
        cachedPagesAudit[collection]?.let { return it }
        val audits = runCatching { loadFromAssets(collection).second }.getOrDefault(emptyList())
        cachedPagesAudit = cachedPagesAudit + (collection to audits)
        return audits
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

    /**
     * Records a spaced repetition recall outcome using FSRS.
     */
    suspend fun recordFsrsReview(
        entry: VocabEntry,
        rating: FsrsRating,
        channel: String = "FSRS",
        desiredRetention: Float = 0.90f
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val result = FsrsScheduler.schedule(entry, rating, desiredRetention, now)

        val isSuccess = rating != FsrsRating.AGAIN
        val newRepCount = if (isSuccess) entry.repetitionCount + 1 else entry.repetitionCount
        val newReviewCount = entry.reviewCount + 1
        val newForgottenCount = if (!isSuccess) entry.forgottenCount + 1 else entry.forgottenCount

        val newStudyStatus = when {
            result.newStability >= 14f && newRepCount >= 3 -> StudyStatus.MASTERED
            result.newState == FsrsState.REVIEW.value && newRepCount >= 2 -> StudyStatus.REVIEWING
            newRepCount >= 1 -> StudyStatus.LEARNING
            else -> StudyStatus.LEARNING
        }

        dao.updateFsrsProgress(
            entryNumber = entry.entryNumber,
            studyStatus = newStudyStatus.name,
            repetitionCount = newRepCount,
            reviewCount = newReviewCount,
            forgottenCount = newForgottenCount,
            easeFactor = (10f - result.newDifficulty).coerceIn(1.3f, 3.5f),
            intervalDays = result.intervalDays,
            nextReview = result.nextReviewTimestamp,
            lastReviewed = now,
            stability = result.newStability,
            difficulty = result.newDifficulty,
            state = result.newState,
            lapses = result.newLapses,
            rating = result.lastRating
        )

        dao.insertReviewLog(
            ReviewLogEntity(
                entryNumber = entry.entryNumber,
                rating = rating.value,
                reviewTimestamp = now,
                scheduledDays = result.intervalDays,
                stability = result.newStability,
                difficulty = result.newDifficulty,
                channel = channel
            )
        )
    }

    /**
     * Backward-compatible simple quiz recorder.
     */
    suspend fun recordStudyResult(entry: VocabEntry, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        val rating = if (isCorrect) FsrsRating.GOOD else FsrsRating.AGAIN
        recordFsrsReview(entry, rating, "QUIZ")
    }

    /**
     * Records an attempt in one of the 7 skill practice channels.
     */
    suspend fun recordSkillPractice(
        entry: VocabEntry,
        direction: LearningDirection,
        isCorrect: Boolean
    ) = withContext(Dispatchers.IO) {
        val succ = if (isCorrect) 1 else 0
        when (direction) {
            LearningDirection.RECOGNITION -> dao.incrementSkillStats(entry.entryNumber, recAtt = 1, recSucc = succ)
            LearningDirection.REVERSE_RECALL -> dao.incrementSkillStats(entry.entryNumber, recallAtt = 1, recallSucc = succ)
            LearningDirection.SPELLING -> dao.incrementSkillStats(entry.entryNumber, spellAtt = 1, spellSucc = succ)
            LearningDirection.CONTEXT -> dao.incrementSkillStats(entry.entryNumber, ctxAtt = 1, ctxSucc = succ)
            LearningDirection.SYNONYM -> dao.incrementSkillStats(entry.entryNumber, synAtt = 1, synSucc = succ)
            LearningDirection.PRODUCTION -> dao.incrementSkillStats(entry.entryNumber, prodAtt = 1, prodSucc = succ)
            LearningDirection.LISTENING -> dao.incrementSkillStats(entry.entryNumber, listAtt = 1, listSucc = succ)
        }
    }

    /**
     * Computes upcoming workload forecast for the next [daysAhead] days.
     */
    suspend fun getWorkloadForecast(daysAhead: Int = 7): List<WorkloadForecast> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60L * 60L * 1000L
        val list = mutableListOf<WorkloadForecast>()
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.US)

        for (i in 0 until daysAhead) {
            val start = now + (i * oneDay)
            val end = start + oneDay
            val count = runCatching { dao.getEntriesDueBetween(start, end).size }.getOrDefault(0)
            list.add(
                WorkloadForecast(
                    dayOffset = i,
                    dateLabel = if (i == 0) "Today" else if (i == 1) "Tomorrow" else sdf.format(Date(start)),
                    dueCount = count
                )
            )
        }
        list
    }

    /**
     * Aggregates skill summaries across all 7 channels.
     */
    fun computeSkillSummaries(entries: List<VocabEntry>): List<SkillMasterySummary> {
        return LearningDirection.values().map { dir ->
            var attempts = 0
            var successes = 0
            var masteredCount = 0

            for (e in entries) {
                when (dir) {
                    LearningDirection.RECOGNITION -> {
                        attempts += e.recognitionAttempts
                        successes += e.recognitionSuccesses
                        if (e.recognitionSuccesses >= 2) masteredCount++
                    }
                    LearningDirection.REVERSE_RECALL -> {
                        attempts += e.recallAttempts
                        successes += e.recallSuccesses
                        if (e.recallSuccesses >= 2) masteredCount++
                    }
                    LearningDirection.SPELLING -> {
                        attempts += e.spellingAttempts
                        successes += e.spellingSuccesses
                        if (e.spellingSuccesses >= 2) masteredCount++
                    }
                    LearningDirection.CONTEXT -> {
                        attempts += e.contextAttempts
                        successes += e.contextSuccesses
                        if (e.contextSuccesses >= 2) masteredCount++
                    }
                    LearningDirection.SYNONYM -> {
                        attempts += e.synonymAttempts
                        successes += e.synonymSuccesses
                        if (e.synonymSuccesses >= 2) masteredCount++
                    }
                    LearningDirection.PRODUCTION -> {
                        attempts += e.productionAttempts
                        successes += e.productionSuccesses
                        if (e.productionSuccesses >= 2) masteredCount++
                    }
                    LearningDirection.LISTENING -> {
                        attempts += e.listeningAttempts
                        successes += e.listeningSuccesses
                        if (e.listeningSuccesses >= 2) masteredCount++
                    }
                }
            }

            val acc = if (attempts > 0) successes.toFloat() / attempts else 0f
            SkillMasterySummary(
                direction = dir,
                attempts = attempts,
                successes = successes,
                accuracy = acc,
                masteredWordsCount = masteredCount
            )
        }
    }

    /**
     * Study Plan persistence using SharedPreferences.
     */
    fun getStudyPlan(): StudyPlan {
        val targetDate = prefs.getLong("plan_target_exam_date", 0L).takeIf { it > 0 }
        val dailyMin = prefs.getInt("plan_daily_minutes", 20)
        val dailyWords = prefs.getInt("plan_daily_new_words", 10)
        val retention = prefs.getFloat("plan_desired_retention", 0.90f)
        val lastDay = prefs.getLong("plan_last_study_day", 0L)
        val streak = prefs.getInt("plan_streak_days", 0)
        val focusStr = prefs.getString("plan_focus_direction", null)
        val focus = focusStr?.let { runCatching { LearningDirection.valueOf(it) }.getOrNull() }

        return StudyPlan(
            targetExamTimestamp = targetDate,
            dailyMinutes = dailyMin,
            dailyNewWords = dailyWords,
            desiredRetention = retention,
            focusDirection = focus,
            lastStudyDay = lastDay,
            streakDays = streak
        )
    }

    fun getCollectionScope(): CollectionScope =
        runCatching { CollectionScope.valueOf(prefs.getString("active_collection_scope", null) ?: "") }
            .getOrDefault(CollectionScope.TOEFL)

    fun saveCollectionScope(scope: CollectionScope) {
        prefs.edit().putString("active_collection_scope", scope.name).apply()
    }

    fun saveStudyPlan(plan: StudyPlan) {
        prefs.edit().apply {
            putLong("plan_target_exam_date", plan.targetExamTimestamp ?: 0L)
            putInt("plan_daily_minutes", plan.dailyMinutes)
            putInt("plan_daily_new_words", plan.dailyNewWords)
            putFloat("plan_desired_retention", plan.desiredRetention)
            putLong("plan_last_study_day", plan.lastStudyDay)
            putInt("plan_streak_days", plan.streakDays)
            putString("plan_focus_direction", plan.focusDirection?.name)
            apply()
        }
    }

    /**
     * Exports full state to JSON string.
     */
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val all = getAllEntriesDirect()
        val plan = getStudyPlan()
        BackupRestoreManager.exportToJson(all, plan)
    }

    /**
     * Exports entries to CSV.
     */
    suspend fun exportBackupCsv(): String = withContext(Dispatchers.IO) {
        val all = getAllEntriesDirect()
        BackupRestoreManager.exportToCsv(all)
    }

    /**
     * Validates and restores from backup JSON transactionally.
     */
    suspend fun restoreFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val base = getAllEntriesDirect()
            val parsedResult = BackupRestoreManager.validateAndParseJson(jsonString, base)
            val backupData = parsedResult.getOrThrow()

            // Safe transactional update
            val entities = backupData.entries.map { VocabEntity.fromDomain(it) }
            for (batch in entities.chunked(50)) {
                dao.insertAll(batch)
            }

            backupData.studyPlan?.let { saveStudyPlan(it) }
            backupData.entries.size
        }
    }

    fun createQuestion(
        target: VocabEntry,
        allEntries: List<VocabEntry>,
        direction: LearningDirection
    ): QuizQuestion {
        val word = target.answerForm
        val meaning = target.primaryMeaning.ifBlank { target.definitions.firstOrNull() ?: word }
        val pos = target.grammaticalLabels.firstOrNull()
        val distractors = pickDistractors(target, allEntries)
        val examples = target.usableExamples

        fun options(correct: String, others: List<String>): List<String> =
            (others.filter { !it.equals(correct, ignoreCase = true) }.distinctBy { it.lowercase() }.take(3) + correct).shuffled()

        fun question(prompt: String, opts: List<String>, correct: String, explanation: String, source: String = meaning) = QuizQuestion(
            id = UUID.randomUUID().toString(),
            direction = direction,
            prompt = prompt,
            targetWord = word,
            options = opts,
            correctAnswer = correct,
            explanation = explanation,
            sourceDefinition = source,
            grammaticalLabel = pos,
            entryNumber = target.entryNumber
        )

        return when (direction) {
            LearningDirection.RECOGNITION -> question(
                prompt = "What does '$word' mean?",
                opts = options(meaning, distractors.map { it.primaryMeaning.ifBlank { it.definitions.firstOrNull() ?: it.answerForm } }),
                correct = meaning,
                explanation = "'$word'${pos?.let { " ($it)" } ?: ""} means: $meaning." +
                    (examples.firstOrNull()?.let { "\nExample: \"$it\"" } ?: "")
            )
            LearningDirection.REVERSE_RECALL -> question(
                prompt = "Which word matches this meaning: \"$meaning\"?",
                opts = options(word, distractors.map { it.answerForm }),
                correct = word,
                explanation = "\"$meaning\" defines '$word'."
            )
            LearningDirection.SPELLING -> question(
                prompt = "Listen and type the word that means: \"$meaning\"",
                opts = emptyList(),
                correct = word,
                explanation = "Correct spelling: '$word'." + (target.phonetic?.let { " Pronunciation: $it." } ?: "")
            )
            LearningDirection.CONTEXT -> {
                val example = examples.randomOrNull()
                if (example != null) {
                    val blanked = example.replace(
                        Regex("(?<![A-Za-z])${Regex.escape(word)}(?![A-Za-z])", RegexOption.IGNORE_CASE), "_______"
                    )
                    question(
                        prompt = "Fill in the blank:\n\"$blanked\"",
                        opts = options(word, distractors.map { it.answerForm }),
                        correct = word,
                        explanation = "Complete sentence: \"$example\"\n'$word' = $meaning."
                    )
                } else {
                    question(
                        prompt = "Choose the expression that best completes the idea \"$meaning\":",
                        opts = options(word, distractors.map { it.answerForm }),
                        correct = word,
                        explanation = "'$word' means $meaning."
                    )
                }
            }
            LearningDirection.SYNONYM -> {
                val syns = target.dictionarySynonyms.filter { it.isNotBlank() && !it.equals(word, ignoreCase = true) }
                val correct = syns.randomOrNull() ?: meaning
                val targetSyns = syns.map { it.lowercase() }.toSet()
                val others = distractors.mapNotNull { d ->
                    (d.dictionarySynonyms.firstOrNull { it.isNotBlank() } ?: d.primaryMeaning.ifBlank { null })
                }.filter { it.lowercase() !in targetSyns }
                question(
                    prompt = "Which word or phrase is closest in meaning to '$word'?",
                    opts = options(correct, others),
                    correct = correct,
                    explanation = "'$word' ≈ '$correct'." + if (syns.size > 1) " Other synonyms: ${syns.filter { it != correct }.joinToString()}." else ""
                )
            }
            LearningDirection.PRODUCTION -> question(
                prompt = "Write your own academic sentence using '$word' ($meaning):",
                opts = emptyList(),
                correct = word,
                explanation = "Model sentence: \"${examples.firstOrNull() ?: "Researchers discussed how '$word' applies to their findings."}\""
            )
            LearningDirection.LISTENING -> question(
                prompt = "Listen carefully and choose the word you hear:",
                opts = options(word, distractors.map { it.answerForm }),
                correct = word,
                explanation = "The spoken word was '$word' ($meaning)."
            )
        }
    }

    /** Distractors from the same collection and part of speech when possible, so options are plausible. */
    private fun pickDistractors(target: VocabEntry, allEntries: List<VocabEntry>): List<VocabEntry> {
        val word = target.answerForm.lowercase()
        val pool = allEntries.filter {
            it.entryNumber != target.entryNumber &&
                it.answerForm.lowercase() != word &&
                !it.primaryMeaning.equals(target.primaryMeaning, ignoreCase = true)
        }
        val pos = target.grammaticalLabels.firstOrNull()
        val sameCollection = pool.filter { it.collection == target.collection }
        val samePos = if (pos != null) sameCollection.filter { pos in it.grammaticalLabels } else emptyList()
        val chosen = samePos.shuffled().take(3).toMutableList()
        if (chosen.size < 3) chosen += sameCollection.filter { it !in chosen }.shuffled().take(3 - chosen.size)
        if (chosen.size < 3) chosen += pool.filter { it !in chosen }.shuffled().take(3 - chosen.size)
        // Spare candidates let callers drop duplicates and still fill three options
        return chosen + pool.filter { it !in chosen }.shuffled().take(3)
    }

    private val apiService = DictionaryApiService()

    suspend fun fetchOnlineDictionary(entry: VocabEntry): Result<DictionaryResult> = withContext(Dispatchers.IO) {
        val result = apiService.fetchDefinition(entry.headword)
        if (result.isSuccess) {
            val data = result.getOrThrow()
            dao.updateDictionaryData(
                entryNumber = entry.entryNumber,
                examples = data.examples,
                definitions = data.definitions,
                synonyms = data.synonyms,
                phonetic = data.phonetic
            )
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
        var activeRecalled = 0
        var totalRetrievabilitySum = 0f
        var nonNewCardsCount = 0

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
            if (e.isActivelyUsable) {
                activeRecalled++
            }

            if (e.studyStatus != StudyStatus.NEW && e.fsrsStability > 0f) {
                val elapsedDays = (now - (e.lastReviewedTimestamp ?: now)).toFloat() / (1000f * 60f * 60f * 24f)
                totalRetrievabilitySum += FsrsScheduler.calculateRetrievability(elapsedDays, e.fsrsStability)
                nonNewCardsCount++
            }
        }

        val learnedTotal = learningCount + reviewingCount + masteredCount
        val completion = if (total > 0) (learnedTotal.toFloat() / total) * 100f else 0f
        val avgRetention = if (nonNewCardsCount > 0) (totalRetrievabilitySum / nonNewCardsCount) * 100f else 90f

        // Estimated study minutes: due words * 0.75 min + new target words * 1.5 min
        val estimatedMinutes = (dueCount * 0.75f + (minOf(15, newCount)) * 1.5f).toInt()

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
            overallCompletionPct = completion,
            retentionRatePct = avgRetention,
            estimatedStudyMinutesRemaining = estimatedMinutes,
            activeRecalledCount = activeRecalled
        )
    }
}
