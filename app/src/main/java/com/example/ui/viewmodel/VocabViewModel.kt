package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LearningDirection
import com.example.data.model.PageAudit
import com.example.data.model.QuizQuestion
import com.example.data.model.StudyStats
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import com.example.data.repository.VocabRepository
import com.example.util.TtsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VocabFilter {
    ALL,
    DUE_REVIEW,
    NEW_WORDS,
    MASTERED,
    DIFFICULT,
    BOOKMARKED,
    ANOMALIES,
    BY_PAGE
}

enum class VocabSort {
    BY_NUMBER,
    ALPHABETICAL,
    BY_MASTERY,
    BY_FORGOTTEN
}

sealed class SessionPhase {
    object Idle : SessionPhase()
    data class IntroNew(val currentIndex: Int, val entries: List<VocabEntry>) : SessionPhase()
    data class Quiz(val currentIndex: Int, val questions: List<QuizQuestion>, val userAnswers: MutableMap<Int, Boolean>) : SessionPhase()
    data class MistakeReview(val currentIndex: Int, val questions: List<QuizQuestion>) : SessionPhase()
    data class Completed(val totalLearned: Int, val correctAnswers: Int, val totalQuestions: Int) : SessionPhase()
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

    private val _sort = MutableStateFlow(VocabSort.BY_NUMBER)
    val sort: StateFlow<VocabSort> = _sort.asStateFlow()

    private val _selectedEntry = MutableStateFlow<VocabEntry?>(null)
    val selectedEntry: StateFlow<VocabEntry?> = _selectedEntry.asStateFlow()

    private val _dailyGoal = MutableStateFlow(10)
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _isDictionaryLoading = MutableStateFlow(false)
    val isDictionaryLoading: StateFlow<Boolean> = _isDictionaryLoading.asStateFlow()

    private val _dictionaryMessage = MutableStateFlow<String?>(null)
    val dictionaryMessage: StateFlow<String?> = _dictionaryMessage.asStateFlow()

    private val _sessionPhase = MutableStateFlow<SessionPhase>(SessionPhase.Idle)
    val sessionPhase: StateFlow<SessionPhase> = _sessionPhase.asStateFlow()

    // Base raw entries from Room
    val allEntries: StateFlow<List<VocabEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & sorted entries
    val displayEntries: StateFlow<List<VocabEntry>> = combine(
        allEntries,
        searchQuery,
        filter,
        selectedPage,
        sort
    ) { entries, query, currentFilter, page, currentSort ->
        val now = System.currentTimeMillis()
        var filtered = entries

        // Search
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

        // Filter
        filtered = when (currentFilter) {
            VocabFilter.ALL -> filtered
            VocabFilter.DUE_REVIEW -> filtered.filter { it.studyStatus != StudyStatus.NEW && it.nextReviewTimestamp <= now }
            VocabFilter.NEW_WORDS -> filtered.filter { it.studyStatus == StudyStatus.NEW }
            VocabFilter.MASTERED -> filtered.filter { it.studyStatus == StudyStatus.MASTERED }
            VocabFilter.DIFFICULT -> filtered.filter { it.isDifficult }
            VocabFilter.BOOKMARKED -> filtered.filter { it.isBookmarked }
            VocabFilter.ANOMALIES -> filtered.filter { it.hasAnomaly }
            VocabFilter.BY_PAGE -> filtered.filter { it.sourcePage == page }
        }

        // Sort
        when (currentSort) {
            VocabSort.BY_NUMBER -> filtered.sortedBy { it.entryNumber }
            VocabSort.ALPHABETICAL -> filtered.sortedBy { it.headword.lowercase() }
            VocabSort.BY_MASTERY -> filtered.sortedByDescending { it.repetitionCount }
            VocabSort.BY_FORGOTTEN -> filtered.sortedByDescending { it.forgottenCount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyStats: StateFlow<StudyStats> = allEntries.combine(_isInitializing) { entries, _ ->
        repository.computeStats(entries)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StudyStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0f)
    )

    val pagesAudit: List<PageAudit>
        get() = repository.getPagesAudit()

    init {
        viewModelScope.launch {
            try {
                repository.initializeDatabaseIfNeeded()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isInitializing.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: VocabFilter) {
        _filter.value = filter
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
        _dailyGoal.value = count.coerceIn(5, 50)
    }

    fun speak(text: String) {
        ttsHelper.speak(text)
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

    fun fetchDictionaryForEntry(entry: VocabEntry) {
        viewModelScope.launch {
            _isDictionaryLoading.value = true
            _dictionaryMessage.value = null
            val result = repository.fetchOnlineDictionary(entry)
            _isDictionaryLoading.value = false
            if (result.isSuccess) {
                val data = result.getOrNull()
                val exCount = data?.examples?.size ?: 0
                _dictionaryMessage.value = "Connected to dictionary: $exCount authentic examples retrieved"
                _selectedEntry.value = _selectedEntry.value?.let { current ->
                    if (current.entryNumber == entry.entryNumber) {
                        val combined = ((data?.examples ?: emptyList()) + current.exampleSentence).filter { s -> s.isNotBlank() }.distinct()
                        val combinedSynonyms = ((data?.synonyms ?: emptyList()) + current.dictionarySynonyms + current.definitions).filter { s -> s.isNotBlank() && s.lowercase() != current.headword.lowercase() }.distinct()
                        current.copy(
                            dictionaryExamples = combined,
                            dictionaryDefinitions = data?.definitions ?: current.dictionaryDefinitions,
                            dictionarySynonyms = combinedSynonyms,
                            phonetic = data?.phonetic ?: current.phonetic
                        )
                    } else current
                }
            } else {
                _dictionaryMessage.value = "Using curated academic examples (${result.exceptionOrNull()?.message ?: "Offline"})"
            }
        }
    }

    fun clearDictionaryMessage() {
        _dictionaryMessage.value = null
    }

    // Daily Learning Workflow session starter
    fun startDailySession() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dueWords = repository.getDueEntries(now).take(15)
            val newWords = repository.getNewEntries(_dailyGoal.value)

            val sessionEntries = (dueWords + newWords).distinctBy { it.entryNumber }
            if (sessionEntries.isEmpty()) {
                // If nothing due or new, take first unmastered or random words
                val fallback = allEntries.value.filter { it.studyStatus != StudyStatus.MASTERED }.take(_dailyGoal.value)
                setupSession(fallback)
            } else {
                setupSession(sessionEntries)
            }
        }
    }

    // Dedicated Practice mode session starter
    fun startPracticeMode(direction: LearningDirection, count: Int = 10, pageFilter: Int? = null) {
        viewModelScope.launch {
            val pool = if (pageFilter != null) {
                allEntries.value.filter { it.sourcePage == pageFilter }
            } else {
                allEntries.value
            }
            val selected = pool.shuffled().take(count)
            val questions = selected.map { target ->
                repository.createQuestion(target, allEntries.value, direction)
            }
            _sessionPhase.value = SessionPhase.Quiz(
                currentIndex = 0,
                questions = questions,
                userAnswers = mutableMapOf()
            )
        }
    }

    // Start practice for a single word
    fun startSingleWordPractice(entry: VocabEntry) {
        val directions = listOf(
            LearningDirection.RECOGNITION,
            LearningDirection.REVERSE_RECALL,
            LearningDirection.SPELLING,
            LearningDirection.CONTEXT,
            LearningDirection.SYNONYM,
            LearningDirection.LISTENING
        )
        val questions = directions.map { dir ->
            repository.createQuestion(entry, allEntries.value, dir)
        }
        _sessionPhase.value = SessionPhase.Quiz(
            currentIndex = 0,
            questions = questions,
            userAnswers = mutableMapOf()
        )
    }

    private fun setupSession(entries: List<VocabEntry>) {
        if (entries.isEmpty()) return
        // Phase 1: Intro new words
        _sessionPhase.value = SessionPhase.IntroNew(currentIndex = 0, entries = entries)
    }

    fun nextIntroWord() {
        val current = _sessionPhase.value
        if (current is SessionPhase.IntroNew) {
            if (current.currentIndex + 1 < current.entries.size) {
                _sessionPhase.value = current.copy(currentIndex = current.currentIndex + 1)
            } else {
                // Move to Quiz phase with mixed questions
                val directions = listOf(
                    LearningDirection.REVERSE_RECALL,
                    LearningDirection.RECOGNITION,
                    LearningDirection.CONTEXT,
                    LearningDirection.SYNONYM,
                    LearningDirection.SPELLING
                )
                val questions = current.entries.mapIndexed { idx, target ->
                    val dir = directions[idx % directions.size]
                    repository.createQuestion(target, allEntries.value, dir)
                }.shuffled()

                _sessionPhase.value = SessionPhase.Quiz(
                    currentIndex = 0,
                    questions = questions,
                    userAnswers = mutableMapOf()
                )
            }
        }
    }

    fun answerQuizQuestion(isCorrect: Boolean, rating: Int = if (isCorrect) 2 else 0) {
        val current = _sessionPhase.value
        if (current is SessionPhase.Quiz) {
            val q = current.questions[current.currentIndex]
            current.userAnswers[current.currentIndex] = isCorrect

            // Record in repository
            viewModelScope.launch {
                repository.recordStudyResult(q.entry, isCorrect, rating)
            }

            if (current.currentIndex + 1 < current.questions.size) {
                _sessionPhase.value = current.copy(currentIndex = current.currentIndex + 1)
            } else {
                // Check if any mistakes were made
                val mistakes = current.questions.filterIndexed { index, _ ->
                    current.userAnswers[index] == false
                }
                if (mistakes.isNotEmpty()) {
                    // Enter mistake review phase
                    _sessionPhase.value = SessionPhase.MistakeReview(
                        currentIndex = 0,
                        questions = mistakes
                    )
                } else {
                    val correctCount = current.userAnswers.values.count { it }
                    _sessionPhase.value = SessionPhase.Completed(
                        totalLearned = current.questions.size,
                        correctAnswers = correctCount,
                        totalQuestions = current.questions.size
                    )
                }
            }
        }
    }

    fun answerMistakeReviewQuestion(isCorrect: Boolean) {
        val current = _sessionPhase.value
        if (current is SessionPhase.MistakeReview) {
            val q = current.questions[current.currentIndex]
            viewModelScope.launch {
                repository.recordStudyResult(q.entry, isCorrect, rating = if (isCorrect) 1 else 0)
            }
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

    fun exitSession() {
        _sessionPhase.value = SessionPhase.Idle
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}
