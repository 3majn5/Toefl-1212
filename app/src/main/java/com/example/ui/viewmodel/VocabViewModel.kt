package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CollectionScope
import com.example.data.model.FsrsRating
import com.example.data.model.FsrsSchedulePreview
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
import com.example.data.model.WritingEvaluationResult
import com.example.data.repository.VocabRepository
import com.example.data.model.ThemeMode
import com.example.data.model.TranslationLanguage
import com.example.data.model.WeeklyProgress
import com.example.util.FsrsScheduler
import com.example.util.Activity
import com.example.util.ChestReward
import com.example.util.FeedbackFx
import com.example.util.MistakeNotebook
import com.example.util.MistakeRecord
import com.example.util.SpeakingFeedback
import com.example.widget.WordWidgetProvider
import com.example.util.GamificationManager
import com.example.util.GamificationState
import com.example.util.ReminderScheduler
import com.example.util.ReminderSettings
import com.example.util.RewardEvent
import com.example.util.SentenceCoachAnalyzer
import com.example.util.TtsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class VocabFilter {
    ALL,
    DUE_REVIEW,
    NEW_WORDS,
    MASTERED,
    DIFFICULT,
    BOOKMARKED,
    ANOMALIES,
    BY_PAGE,
    BY_SUBLIST
}

enum class VocabSort {
    BY_NUMBER,
    ALPHABETICAL,
    BY_MASTERY,
    BY_FORGOTTEN,
    BY_SUBLIST
}

sealed class SessionPhase {
    object Idle : SessionPhase()
    data class IntroNew(val currentIndex: Int, val entries: List<VocabEntry>) : SessionPhase()
    data class FsrsFlashcards(val currentIndex: Int, val entries: List<VocabEntry>, val isFlipped: Boolean = false) : SessionPhase()
    data class Quiz(val currentIndex: Int, val questions: List<QuizQuestion>, val userAnswers: MutableMap<Int, Boolean>) : SessionPhase()
    data class MistakeReview(val currentIndex: Int, val questions: List<QuizQuestion>) : SessionPhase()
    data class Completed(val totalLearned: Int, val correctAnswers: Int, val totalQuestions: Int, val srsReviewed: Int = 0) : SessionPhase()
}

class VocabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VocabRepository(application)
    private val ttsHelper = TtsHelper(application)

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(VocabFilter.ALL)
    val filter: StateFlow<VocabFilter> = _filter.asStateFlow()

    private val _selectedPage = MutableStateFlow(1)
    val selectedPage: StateFlow<Int> = _selectedPage.asStateFlow()

    private val _selectedSublist = MutableStateFlow(1)
    val selectedSublist: StateFlow<Int> = _selectedSublist.asStateFlow()

    private val _sort = MutableStateFlow(VocabSort.BY_NUMBER)
    val sort: StateFlow<VocabSort> = _sort.asStateFlow()

    private val _selectedEntry = MutableStateFlow<VocabEntry?>(null)
    val selectedEntry: StateFlow<VocabEntry?> = _selectedEntry.asStateFlow()

    private val _dailyGoal = MutableStateFlow(10)
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _studyPlan = MutableStateFlow(repository.getStudyPlan())
    val studyPlan: StateFlow<StudyPlan> = _studyPlan.asStateFlow()

    private val _isDictionaryLoading = MutableStateFlow(false)
    val isDictionaryLoading: StateFlow<Boolean> = _isDictionaryLoading.asStateFlow()

    private val _dictionaryMessage = MutableStateFlow<String?>(null)
    val dictionaryMessage: StateFlow<String?> = _dictionaryMessage.asStateFlow()

    private val _sessionPhase = MutableStateFlow<SessionPhase>(SessionPhase.Idle)
    val sessionPhase: StateFlow<SessionPhase> = _sessionPhase.asStateFlow()

    private val _ttsSpeed = MutableStateFlow(1.0f)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    private val _workloadForecast = MutableStateFlow<List<WorkloadForecast>>(emptyList())
    val workloadForecast: StateFlow<List<WorkloadForecast>> = _workloadForecast.asStateFlow()

    private val _collectionScope = MutableStateFlow(repository.getCollectionScope())
    /** Which word list(s) the whole app is currently studying. */
    val collectionScope: StateFlow<CollectionScope> = _collectionScope.asStateFlow()

    /** Every entry from every collection, regardless of the active scope. */
    val everyEntry: StateFlow<List<VocabEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Entries in the active collection scope — what every study screen works with. */
    val allEntries: StateFlow<List<VocabEntry>> = combine(everyEntry, _collectionScope) { entries, scope ->
        entries.filter { scope.includes(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val collectionCounts: StateFlow<Map<CollectionScope, Int>> = everyEntry.map { entries ->
        CollectionScope.values().associateWith { scope -> entries.count { scope.includes(it) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val pageAndSublist = combine(_selectedPage, _selectedSublist) { page, sublist -> page to sublist }

    val displayEntries: StateFlow<List<VocabEntry>> = combine(
        allEntries,
        searchQuery,
        filter,
        pageAndSublist,
        sort
    ) { entries, query, currentFilter, (page, sublist), currentSort ->
        val now = System.currentTimeMillis()
        var filtered = entries

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.headword.lowercase().contains(q) ||
                it.primaryMeaning.lowercase().contains(q) ||
                it.definitions.any { d -> d.lowercase().contains(q) } ||
                it.entryNumber.toString().contains(q) ||
                it.rawText.lowercase().contains(q)
            }
        }

        filtered = when (currentFilter) {
            VocabFilter.ALL -> filtered
            VocabFilter.DUE_REVIEW -> filtered.filter { it.studyStatus != StudyStatus.NEW && it.nextReviewTimestamp <= now }
            VocabFilter.NEW_WORDS -> filtered.filter { it.studyStatus == StudyStatus.NEW }
            VocabFilter.MASTERED -> filtered.filter { it.studyStatus == StudyStatus.MASTERED }
            VocabFilter.DIFFICULT -> filtered.filter { it.isDifficult }
            VocabFilter.BOOKMARKED -> filtered.filter { it.isBookmarked }
            VocabFilter.ANOMALIES -> filtered.filter { it.hasAnomaly }
            VocabFilter.BY_PAGE -> filtered.filter { it.sourcePage == page }
            VocabFilter.BY_SUBLIST -> filtered.filter { it.awlSublist == sublist }
        }

        when (currentSort) {
            VocabSort.BY_NUMBER -> filtered.sortedBy { it.entryNumber }
            VocabSort.ALPHABETICAL -> filtered.sortedBy { it.headword.lowercase() }
            VocabSort.BY_MASTERY -> filtered.sortedByDescending { it.repetitionCount }
            VocabSort.BY_FORGOTTEN -> filtered.sortedByDescending { it.forgottenCount }
            VocabSort.BY_SUBLIST -> filtered.sortedWith(compareBy({ if (it.awlSublist == 0) 99 else it.awlSublist }, { it.headword.lowercase() }))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyStats: StateFlow<StudyStats> = allEntries.combine(_isInitializing) { entries, _ ->
        repository.computeStats(entries)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StudyStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0f)
    )

    val skillSummaries: StateFlow<List<SkillMasterySummary>> = allEntries.map {
        repository.computeSkillSummaries(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Settings ────────────────────────────────────────────────
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _translationLanguage = MutableStateFlow(repository.getTranslationLanguage())
    val translationLanguage: StateFlow<TranslationLanguage> = _translationLanguage.asStateFlow()

    private val _reminderSettings = MutableStateFlow(ReminderScheduler.load(application))
    val reminderSettings: StateFlow<ReminderSettings> = _reminderSettings.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        repository.saveThemeMode(mode)
    }

    fun setTranslationLanguage(language: TranslationLanguage) {
        _translationLanguage.value = language
        repository.saveTranslationLanguage(language)
    }

    fun translationFor(entry: VocabEntry, language: TranslationLanguage = _translationLanguage.value): String? =
        repository.translationFor(entry, language)

    fun setReminderSettings(settings: ReminderSettings) {
        _reminderSettings.value = settings
        ReminderScheduler.save(getApplication(), settings)
    }

    // ── Streaks, XP & badges ────────────────────────────────────
    private val gamification = GamificationManager(
        application.getSharedPreferences("toefl_1212_study_prefs", android.content.Context.MODE_PRIVATE)
    )
    private val _gameState = MutableStateFlow(gamification.state())
    val gameState: StateFlow<GamificationState> = _gameState.asStateFlow()

    private val _rewardEvent = MutableStateFlow<RewardEvent?>(null)
    /** Latest reward for the floating "+XP" / badge animation; cleared by [consumeReward]. */
    val rewardEvent: StateFlow<RewardEvent?> = _rewardEvent.asStateFlow()

    fun consumeReward() {
        _rewardEvent.value = null
    }

    private fun award(xp: Int, activity: Activity = Activity(), multiplier: Float = 1f, lucky: Boolean = false) {
        val skillsPracticed = skillSummaries.value.count { it.attempts > 0 }
        val event = gamification.award(xp, studyStats.value, skillsPracticed, activity, multiplier, lucky)
        _gameState.value = gamification.state()
        _rewardEvent.value = event
        _sessionStats.value = _sessionStats.value.let {
            it.copy(xp = it.xp + event.xp, luckyHits = it.luckyHits + if (lucky) 1 else 0)
        }
    }

    // ── Combos & instant feedback ───────────────────────────────
    private val _combo = MutableStateFlow(0)
    /** Consecutive objectively-correct answers; raises the XP multiplier. */
    val combo: StateFlow<Int> = _combo.asStateFlow()

    private val _sessionStats = MutableStateFlow(SessionStats())
    /** XP, best combo and lucky hits since the current session started (for the summary screen). */
    val sessionStats: StateFlow<SessionStats> = _sessionStats.asStateFlow()

    private val _soundEnabled = MutableStateFlow(FeedbackFx.soundEnabled)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()
    private val _hapticsEnabled = MutableStateFlow(FeedbackFx.hapticsEnabled)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        FeedbackFx.setSoundEnabled(enabled)
        _soundEnabled.value = enabled
        if (enabled) FeedbackFx.correct(3)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        FeedbackFx.setHapticsEnabled(enabled)
        _hapticsEnabled.value = enabled
    }

    init {
        // A fresh session starts with a clean combo and summary
        viewModelScope.launch {
            var wasActive = false
            _sessionPhase.collect { phase ->
                val active = phase !is SessionPhase.Idle && phase !is SessionPhase.Completed
                if (active && !wasActive) {
                    _combo.value = 0
                    _sessionStats.value = SessionStats()
                }
                wasActive = active
            }
        }
    }

    /**
     * Rewards an objectively graded answer the moment it is checked: sound + haptic, combo
     * multiplier, and a small chance of a surprise double-XP "lucky" bonus. Self-rated
     * flashcards never feed the combo or lucky rolls, so there's no incentive to rate dishonestly.
     */
    private fun rewardAnswer(isCorrect: Boolean, direction: LearningDirection, strongSentence: Boolean = false) =
        rewardAnswer(isCorrect, direction.name, strongSentence)

    private fun rewardAnswer(isCorrect: Boolean, skill: String, strongSentence: Boolean = false) {
        val combo = if (isCorrect) _combo.value + 1 else 0
        _combo.value = combo
        if (combo > _sessionStats.value.bestCombo) _sessionStats.value = _sessionStats.value.copy(bestCombo = combo)
        val lucky = isCorrect && kotlin.random.Random.nextFloat() < GamificationManager.LUCKY_CHANCE
        if (isCorrect) FeedbackFx.correct(combo, lucky) else FeedbackFx.wrong()
        award(
            xp = if (isCorrect) GamificationManager.XP_CORRECT else GamificationManager.XP_WRONG,
            activity = Activity(correctAnswer = isCorrect, combo = combo, skill = skill, strongSentence = strongSentence),
            multiplier = GamificationManager.comboMultiplier(combo),
            lucky = lucky
        )
    }

    /** Called by the quiz as soon as an answer is checked (before "Continue"). */
    fun onQuizAnswerGraded(isCorrect: Boolean, question: QuizQuestion) {
        if (question.entryNumber != 0) recordMistake(question.entryNumber, isCorrect, question.direction.name)
        rewardAnswer(isCorrect, question.direction, strongSentence = isCorrect && question.direction == LearningDirection.PRODUCTION)
    }

    /** Opens today's mystery chest; returns what was inside, or null if it isn't available. */
    fun openChest(): ChestReward? {
        val (reward, event) = gamification.openChest(studyStats.value) ?: return null
        FeedbackFx.fanfare()
        _gameState.value = gamification.state()
        _rewardEvent.value = event
        return reward
    }

    fun setDailyXpGoal(xp: Int) {
        gamification.setDailyGoal(xp)
        _gameState.value = gamification.state()
    }

    fun refreshGameState() {
        _gameState.value = gamification.state()
    }


    // ── Mistake notebook ────────────────────────────────────────
    private val mistakeNotebook = MistakeNotebook(
        application.getSharedPreferences("toefl_1212_study_prefs", android.content.Context.MODE_PRIVATE)
    )
    private val _mistakes = MutableStateFlow(mistakeNotebook.all())
    val mistakes: StateFlow<List<MistakeRecord>> = _mistakes.asStateFlow()

    private fun recordMistake(entryNumber: Int, isCorrect: Boolean, channel: String) {
        mistakeNotebook.record(entryNumber, isCorrect, channel)
        _mistakes.value = mistakeNotebook.all()
    }

    fun removeMistake(entryNumber: Int) {
        mistakeNotebook.remove(entryNumber)
        _mistakes.value = mistakeNotebook.all()
    }

    /** Quiz on up to 10 notebook words, most recent misses first, mixing three question types. */
    fun startMistakeDrill(): Boolean {
        val byNumber = everyEntry.value.associateBy { it.entryNumber }
        val targets = _mistakes.value.sortedByDescending { it.lastWrongAt }.mapNotNull { byNumber[it.entryNumber] }.take(10)
        if (targets.isEmpty()) return false
        val pool = everyEntry.value
        val directions = listOf(LearningDirection.RECOGNITION, LearningDirection.REVERSE_RECALL, LearningDirection.SPELLING)
        val questions = targets.mapIndexed { i, entry ->
            repository.createQuestion(entry, pool, directions[i % directions.size])
        }.shuffled()
        _sessionPhase.value = SessionPhase.Quiz(0, questions, mutableMapOf())
        return true
    }

    // ── 60-second challenge ─────────────────────────────────────
    private val challengePrefs = application.getSharedPreferences("toefl_1212_study_prefs", android.content.Context.MODE_PRIVATE)
    private val _bestChallenge = MutableStateFlow(challengePrefs.getInt("challenge_best", 0))
    val bestChallenge: StateFlow<Int> = _bestChallenge.asStateFlow()

    /** A new challenge starts with a fresh combo, independent of earlier practice. */
    fun startChallenge() {
        _combo.value = 0
    }

    fun nextChallengeQuestion(): QuizQuestion? {
        val pool = allEntries.value
        if (pool.size < 4) return null
        return repository.createQuestion(pool.random(), pool, LearningDirection.RECOGNITION)
    }

    fun answerChallenge(question: QuizQuestion, isCorrect: Boolean) {
        val entry = everyEntry.value.find { it.entryNumber == question.entryNumber }
        if (entry != null) recordSkillPractice(entry, LearningDirection.RECOGNITION, isCorrect)
        else rewardAnswer(isCorrect, LearningDirection.RECOGNITION)
    }

    /** Saves the score; returns true for a new personal best. */
    fun finishChallenge(score: Int): Boolean {
        _combo.value = 0
        if (score <= _bestChallenge.value) return false
        challengePrefs.edit().putInt("challenge_best", score).apply()
        val hadRecord = _bestChallenge.value > 0
        _bestChallenge.value = score
        if (hadRecord) FeedbackFx.fanfare()
        return hadRecord
    }

    // ── Speaking & pronunciation ────────────────────────────────
    /** XP for a speaking answer: 10 for trying plus 10 per rubric point. */
    fun rewardSpeaking(feedback: SpeakingFeedback) {
        if (feedback.wordCount == 0) return
        if (feedback.score >= 3) FeedbackFx.correct(3) else FeedbackFx.tap()
        award(10 + feedback.score * 10, Activity(skill = "SPEAKING"))
    }

    fun rewardPronunciation(entry: VocabEntry, score: Int) {
        recordMistake(entry.entryNumber, score > 0, "PRONUNCIATION")
        rewardAnswer(score > 0, "PRONUNCIATION")
    }

    // ── Home-screen widget ──────────────────────────────────────
    init {
        viewModelScope.launch {
            combine(studyStats, _gameState, allEntries) { stats, game, entries -> Triple(stats, game, entries) }
                .collect { (stats, game, entries) ->
                    if (entries.isEmpty()) return@collect
                    val day = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
                    val rotation = entries.filter { it.studyStatus != StudyStatus.MASTERED && it.primaryMeaning.isNotBlank() }
                        .ifEmpty { entries }
                        .shuffled(kotlin.random.Random(day))
                        .take(60)
                        .map { it.headword to it.primaryMeaning }
                    WordWidgetProvider.saveSnapshot(getApplication(), game.streakDays, game.studiedToday, stats.dueCount, rotation)
                }
        }
    }

    /**
     * Words studied in any mode: reviewed with flashcards or answered in a quiz, lab or challenge.
     * Matches what the "words learned per week" chart counts.
     */
    val wordsStudied: StateFlow<Int> = everyEntry
        .map { list -> list.count { it.studyStatus != StudyStatus.NEW || it.totalAttempts > 0 } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    // ── Progress charts ─────────────────────────────────────────
    private val _weeklyProgress = MutableStateFlow<WeeklyProgress?>(null)
    val weeklyProgress: StateFlow<WeeklyProgress?> = _weeklyProgress.asStateFlow()

    fun refreshProgress() {
        viewModelScope.launch { _weeklyProgress.value = repository.getWeeklyProgress(8) }
    }

    val pagesAudit: List<PageAudit>
        get() = repository.getPagesAudit(
            if (_collectionScope.value == CollectionScope.AWL) WordCollection.AWL else WordCollection.TOEFL
        )

    init {
        viewModelScope.launch {
            try {
                repository.initializeDatabaseIfNeeded()
                refreshForecast()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isInitializing.value = false
            }
        }
    }

    private suspend fun refreshForecast() {
        val forecast = repository.getWorkloadForecast(7)
        _workloadForecast.value = forecast
    }

    fun setCollectionScope(scope: CollectionScope) {
        if (_collectionScope.value == scope) return
        _collectionScope.value = scope
        _selectedPage.value = 1
        repository.saveCollectionScope(scope)
    }

    private suspend fun dueInScope(now: Long): List<VocabEntry> {
        val scope = _collectionScope.value
        return repository.getDueEntries(now).filter { scope.includes(it) }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: VocabFilter) {
        _filter.value = filter
    }

    fun setSelectedSublist(sublist: Int) {
        _selectedSublist.value = sublist.coerceIn(1, 10)
    }

    fun setSelectedPage(page: Int) {
        _selectedPage.value = page
    }

    fun setSort(sort: VocabSort) {
        _sort.value = sort
    }

    fun selectEntry(entry: VocabEntry?) {
        _selectedEntry.value = entry
    }

    fun setDailyGoal(count: Int) {
        val c = count.coerceIn(5, 50)
        _dailyGoal.value = c
        val updatedPlan = _studyPlan.value.copy(dailyNewWords = c)
        updateStudyPlan(updatedPlan)
    }

    fun updateStudyPlan(plan: StudyPlan) {
        _studyPlan.value = plan
        repository.saveStudyPlan(plan)
    }

    fun setTtsSpeed(rate: Float) {
        _ttsSpeed.value = rate
        ttsHelper.setSpeedRate(rate)
    }

    fun setTtsVoice(isUk: Boolean) {
        val locale = if (isUk) Locale.UK else Locale.US
        ttsHelper.setVoiceLocale(locale)
    }

    fun speak(text: String) {
        ttsHelper.speak(text, _ttsSpeed.value)
    }

    fun toggleDifficult(entry: VocabEntry) {
        viewModelScope.launch {
            val newDifficult = !entry.isDifficult
            repository.toggleDifficult(entry.entryNumber, newDifficult)
            _selectedEntry.value = _selectedEntry.value?.let {
                if (it.entryNumber == entry.entryNumber) it.copy(isDifficult = newDifficult) else it
            }
        }
    }

    fun toggleBookmark(entry: VocabEntry) {
        viewModelScope.launch {
            val newBookmark = !entry.isBookmarked
            repository.toggleBookmark(entry.entryNumber, newBookmark)
            _selectedEntry.value = _selectedEntry.value?.let {
                if (it.entryNumber == entry.entryNumber) it.copy(isBookmarked = newBookmark) else it
            }
        }
    }

    fun updateVerification(
        entryNumber: Int,
        status: UserVerificationStatus,
        correction: String?,
        note: String?
    ) {
        viewModelScope.launch {
            repository.updateVerification(entryNumber, status, correction, note)
            _selectedEntry.value = _selectedEntry.value?.let {
                if (it.entryNumber == entryNumber) {
                    it.copy(userStatus = status, userCorrection = correction, userNote = note)
                } else it
            }
        }
    }

    fun fetchOnlineDictionary(entry: VocabEntry) {
        viewModelScope.launch {
            _isDictionaryLoading.value = true
            _dictionaryMessage.value = null
            val result = repository.fetchOnlineDictionary(entry)
            _isDictionaryLoading.value = false
            if (result.isSuccess) {
                _dictionaryMessage.value = "Updated from Wiktionary & Datamuse!"
                _selectedEntry.value = repository.getEntryByNumberDirect(entry.entryNumber)
            } else {
                _dictionaryMessage.value = "Dictionary lookup failed. Check connection."
            }
        }
    }

    fun previewFsrsIntervals(entry: VocabEntry): List<FsrsSchedulePreview> {
        val desiredRetention = _studyPlan.value.desiredRetention
        return FsrsScheduler.previewIntervals(entry, desiredRetention)
    }

    /**
     * Records a recall outcome using the FSRS engine and advances session.
     */
    fun recordFsrsRating(entry: VocabEntry, rating: FsrsRating) {
        viewModelScope.launch {
            val desiredRetention = _studyPlan.value.desiredRetention
            repository.recordFsrsReview(entry, rating, "FSRS", desiredRetention)
            refreshForecast()
        }
        FeedbackFx.tap()
        when (rating) {
            FsrsRating.AGAIN -> recordMistake(entry.entryNumber, false, "FLASHCARD")
            FsrsRating.GOOD, FsrsRating.EASY -> recordMistake(entry.entryNumber, true, "FLASHCARD")
            FsrsRating.HARD -> {}
        }
        award(GamificationManager.xpForRating(rating.value), Activity(flashcard = true))

        val current = _sessionPhase.value
        if (current is SessionPhase.FsrsFlashcards) {
            if (current.currentIndex + 1 < current.entries.size) {
                _sessionPhase.value = current.copy(currentIndex = current.currentIndex + 1, isFlipped = false)
            } else {
                award(GamificationManager.XP_SESSION_BONUS)
                _sessionPhase.value = SessionPhase.Completed(
                    totalLearned = current.entries.size,
                    correctAnswers = current.entries.size,
                    totalQuestions = current.entries.size,
                    srsReviewed = current.entries.size
                )
            }
        }
    }

    fun flipCard() {
        val current = _sessionPhase.value
        if (current is SessionPhase.FsrsFlashcards) {
            _sessionPhase.value = current.copy(isFlipped = !current.isFlipped)
        }
    }

    fun evaluateWritingSentence(sentence: String, entry: VocabEntry): WritingEvaluationResult {
        return SentenceCoachAnalyzer.evaluate(sentence, entry)
    }

    fun startDailySession() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val due = dueInScope(now)
            val newWords = repository.getNewEntries(_studyPlan.value.dailyNewWords, _collectionScope.value)
            val sessionPool = (due + newWords).distinctBy { it.entryNumber }

            if (sessionPool.isNotEmpty()) {
                _sessionPhase.value = SessionPhase.FsrsFlashcards(0, sessionPool)
            } else {
                val fallbackPool = allEntries.value.shuffled().take(_studyPlan.value.dailyNewWords)
                _sessionPhase.value = SessionPhase.FsrsFlashcards(0, fallbackPool)
            }
        }
    }

    fun startReviewAllSession() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val due = dueInScope(now)
            if (due.isNotEmpty()) {
                _sessionPhase.value = SessionPhase.FsrsFlashcards(0, due)
            } else {
                // If no due reviews, review lowest stability
                val lowStability = allEntries.value.filter { it.studyStatus != StudyStatus.NEW }.sortedBy { it.fsrsStability }.take(20)
                _sessionPhase.value = SessionPhase.FsrsFlashcards(0, lowStability.ifEmpty { allEntries.value.take(20) })
            }
        }
    }

    fun startShortSession(count: Int = 5) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val due = dueInScope(now).take(count)
            val pool = if (due.isNotEmpty()) due else allEntries.value.shuffled().take(count)
            _sessionPhase.value = SessionPhase.FsrsFlashcards(0, pool)
        }
    }

    fun startSingleWordPractice(entry: VocabEntry) {
        val pool = allEntries.value
        val questions = LearningDirection.values().map { dir ->
            repository.createQuestion(entry, pool, dir)
        }
        _sessionPhase.value = SessionPhase.Quiz(0, questions, mutableMapOf())
    }

    fun startDirectionPractice(direction: LearningDirection, count: Int = 10) {
        val pool = allEntries.value
        if (pool.isEmpty()) return
        val targets = pool.shuffled().take(count)
        val questions = targets.map { target ->
            repository.createQuestion(target, pool, direction)
        }
        _sessionPhase.value = SessionPhase.Quiz(0, questions, mutableMapOf())
    }

    fun recordSkillPractice(entry: VocabEntry, direction: LearningDirection, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.recordSkillPractice(entry, direction, isCorrect)
        }
        recordMistake(entry.entryNumber, isCorrect, direction.name)
        rewardAnswer(isCorrect, direction)
    }

    /** Scores a quiz "production" answer with the writing coach; strong sentences count as correct. */
    fun evaluateProduction(sentence: String, question: QuizQuestion): WritingEvaluationResult? {
        val entry = everyEntry.value.find { it.entryNumber == question.entryNumber } ?: return null
        return SentenceCoachAnalyzer.evaluate(sentence, entry)
    }

    fun advanceIntroCard() {
        val current = _sessionPhase.value
        if (current is SessionPhase.IntroNew) {
            if (current.currentIndex + 1 < current.entries.size) {
                _sessionPhase.value = current.copy(currentIndex = current.currentIndex + 1)
            } else {
                val pool = allEntries.value
                val questions = mutableListOf<QuizQuestion>()
                for (entry in current.entries) {
                    questions.add(repository.createQuestion(entry, pool, LearningDirection.RECOGNITION))
                    questions.add(repository.createQuestion(entry, pool, LearningDirection.REVERSE_RECALL))
                }
                questions.shuffle()
                _sessionPhase.value = SessionPhase.Quiz(0, questions, mutableMapOf())
            }
        }
    }

    fun recordQuizAnswer(isCorrect: Boolean, question: QuizQuestion) {
        val current = _sessionPhase.value
        if (current is SessionPhase.Quiz) {
            current.userAnswers[current.currentIndex] = isCorrect
            viewModelScope.launch {
                val target = everyEntry.value.find { it.entryNumber == question.entryNumber }
                    ?: allEntries.value.find { it.headword.equals(question.targetWord, ignoreCase = true) }
                if (target != null) {
                    repository.recordStudyResult(target, isCorrect)
                    repository.recordSkillPractice(target, question.direction, isCorrect)
                }
            }

            if (current.currentIndex + 1 < current.questions.size) {
                _sessionPhase.value = current.copy(currentIndex = current.currentIndex + 1)
            } else {
                val mistakes = current.questions.filterIndexed { index, _ ->
                    current.userAnswers[index] == false
                }
                if (mistakes.isNotEmpty()) {
                    _sessionPhase.value = SessionPhase.MistakeReview(0, mistakes)
                } else {
                    val correctCount = current.userAnswers.values.count { it }
                    val perfect = current.questions.size >= 5
                    award(
                        xp = GamificationManager.XP_SESSION_BONUS + if (perfect) GamificationManager.XP_PERFECT_BONUS else 0,
                        activity = Activity(perfectSession = perfect)
                    )
                    _sessionPhase.value = SessionPhase.Completed(
                        totalLearned = current.questions.size,
                        correctAnswers = correctCount,
                        totalQuestions = current.questions.size
                    )
                }
            }
        } else if (current is SessionPhase.MistakeReview) {
            if (current.currentIndex + 1 < current.questions.size) {
                _sessionPhase.value = current.copy(currentIndex = current.currentIndex + 1)
            } else {
                _sessionPhase.value = SessionPhase.Completed(
                    totalLearned = current.questions.size,
                    correctAnswers = current.questions.size,
                    totalQuestions = current.questions.size
                )
            }
        }
    }

    fun finishSession() {
        _sessionPhase.value = SessionPhase.Idle
    }

    suspend fun exportJson(): String = repository.exportBackupJson()

    suspend fun exportCsv(): String = repository.exportBackupCsv()

    suspend fun restoreFromJson(json: String): Result<Int> {
        val res = repository.restoreFromJson(json)
        if (res.isSuccess) {
            _studyPlan.value = repository.getStudyPlan()
            refreshForecast()
        }
        return res
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}

/** Running totals for the current study session. */
data class SessionStats(val xp: Int = 0, val bestCombo: Int = 0, val luckyHits: Int = 0)
