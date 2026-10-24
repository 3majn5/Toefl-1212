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

    val allEntries: StateFlow<List<VocabEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayEntries: StateFlow<List<VocabEntry>> = combine(
        allEntries,
        searchQuery,
        filter,
        selectedPage,
        sort
    ) { entries, query, currentFilter, page, currentSort ->
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
        }

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

    fun startDailySession() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val due = repository.getDueEntries(now)
            val newWords = repository.getNewEntries(_dailyGoal.value)
            val sessionPool = (due + newWords).distinctBy { it.entryNumber }

            if (sessionPool.isNotEmpty()) {
                _sessionPhase.value = SessionPhase.IntroNew(0, sessionPool)
            } else {
                val fallbackPool = allEntries.value.shuffled().take(_dailyGoal.value)
                _sessionPhase.value = SessionPhase.IntroNew(0, fallbackPool)
            }
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
                val target = allEntries.value.find { it.headword.equals(question.targetWord, ignoreCase = true) || it.primaryMeaning.equals(question.targetWord, ignoreCase = true) }
                    ?: allEntries.value.find { it.entryNumber.toString() == question.id }
                if (target != null) {
                    repository.recordStudyResult(target, isCorrect)
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

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}
