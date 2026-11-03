package com.example.data.model

enum class UserVerificationStatus {
    UNVERIFIED,
    VERIFIED,
    QUESTIONABLE,
    CORRECTED,
    NEEDS_EXPLANATION
}

enum class WordCollection(val key: String, val displayName: String, val shortName: String) {
    TOEFL("TOEFL", "TOEFL 1212", "TOEFL"),
    AWL("AWL", "Academic Word List", "AWL");

    companion object {
        fun fromKey(key: String?): WordCollection = values().firstOrNull { it.key == key } ?: TOEFL
    }
}

/** Which collection(s) the learner is currently studying. */
enum class CollectionScope(val displayName: String, val shortName: String) {
    TOEFL("TOEFL 1212", "TOEFL"),
    AWL("AWL 570", "AWL"),
    ALL("Both Lists", "Both");

    fun includes(entry: VocabEntry): Boolean = when (this) {
        TOEFL -> entry.collection == WordCollection.TOEFL.key
        AWL -> entry.collection == WordCollection.AWL.key
        ALL -> true
    }
}

enum class StudyStatus {
    NEW,
    LEARNING,
    REVIEWING,
    MASTERED
}

enum class FsrsRating(val value: Int, val displayName: String, val shortDesc: String) {
    AGAIN(1, "Again", "Forgot completely"),
    HARD(2, "Hard", "Recalled with effort"),
    GOOD(3, "Good", "Recalled correctly"),
    EASY(4, "Easy", "Recalled instantly")
}

enum class FsrsState(val value: Int) {
    NEW(0),
    LEARNING(1),
    REVIEW(2),
    RELEARNING(3)
}

data class FsrsSchedulePreview(
    val rating: FsrsRating,
    val intervalText: String,
    val intervalDays: Int,
    val newStability: Float,
    val newDifficulty: Float
)

enum class LearningDirection(val displayName: String, val description: String) {
    RECOGNITION("Recognition", "Word to meaning selection"),
    REVERSE_RECALL("Reverse Recall", "Meaning to target word recall"),
    SPELLING("Spelling & Typing", "Spell the word from pronunciation/meaning"),
    CONTEXT("Context Gap-Fill", "Complete the missing word in context"),
    SYNONYM("Synonym Discrimination", "Identify synonymous or distinguishing terms"),
    PRODUCTION("Production Writing", "Write an authentic sentence using the word"),
    LISTENING("Listening & Audio", "Identify the word spoken via speech audio")
}

data class VocabEntry(
    val entryNumber: Int,
    val sourcePage: Int,
    val headword: String,
    val rawText: String,
    val definitions: List<String>,
    val primaryMeaning: String,
    val grammaticalLabels: List<String>,
    val notes: String = "",
    val hasDuplicateHeadword: Boolean = false,
    val duplicateWithNumbers: List<Int> = emptyList(),
    val hasAnomaly: Boolean = false,
    val anomalyNotes: String = "",
    val userStatus: UserVerificationStatus = UserVerificationStatus.UNVERIFIED,
    val userCorrection: String? = null,
    val userNote: String? = null,
    val isDifficult: Boolean = false,
    val isBookmarked: Boolean = false,
    val exampleSentence: String = "",
    val dictionaryExamples: List<String> = emptyList(),
    val dictionaryDefinitions: List<String> = emptyList(),
    val dictionarySynonyms: List<String> = emptyList(),
    val phonetic: String? = null,
    val studyStatus: StudyStatus = StudyStatus.NEW,
    val repetitionCount: Int = 0,
    val reviewCount: Int = 0,
    val forgottenCount: Int = 0,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val nextReviewTimestamp: Long = 0L,
    val lastReviewedTimestamp: Long? = null,

    // FSRS Spaced Repetition Parameters
    val fsrsStability: Float = 0f,
    val fsrsDifficulty: Float = 5.0f,
    val fsrsState: Int = 0,
    val fsrsLapses: Int = 0,
    val lastRating: Int = 0,

    // Skill-Specific Mastery Tracking (7 Channels)
    val recognitionAttempts: Int = 0,
    val recognitionSuccesses: Int = 0,
    val recallAttempts: Int = 0,
    val recallSuccesses: Int = 0,
    val spellingAttempts: Int = 0,
    val spellingSuccesses: Int = 0,
    val contextAttempts: Int = 0,
    val contextSuccesses: Int = 0,
    val synonymAttempts: Int = 0,
    val synonymSuccesses: Int = 0,
    val productionAttempts: Int = 0,
    val productionSuccesses: Int = 0,
    val listeningAttempts: Int = 0,
    val listeningSuccesses: Int = 0,

    // Rich Linguistic Profile
    val collocations: List<String> = emptyList(),
    val wordFamily: List<String> = emptyList(),
    val grammarPatterns: List<String> = emptyList(),
    val commonMistakes: String = "",
    val registerFormality: String = "",
    val antonyms: List<String> = emptyList(),

    // Source collection ("TOEFL" or "AWL")
    val collection: String = WordCollection.TOEFL.key,

    // AWL frequency sublist 1 (most frequent) to 10; 0 for words outside the AWL
    val awlSublist: Int = 0
) {
    val isAwl: Boolean
        get() = collection == WordCollection.AWL.key

    /** Number within the word's own list (AWL entries are stored with a 2000 offset). */
    val listNumber: Int
        get() = if (isAwl) entryNumber - AWL_NUMBER_OFFSET else entryNumber

    /** Human-readable reference such as "#57" or "AWL #12". */
    val listLabel: String
        get() = if (isAwl) "AWL #$listNumber" else "#$entryNumber"

    /** Headword without source annotations like "(verb)" or "[pl.]" — the form learners type and read. */
    val answerForm: String
        get() = headword.replace(Regex("\\(.*?\\)|\\[.*?\\]"), "").trim().ifBlank { headword.trim() }

    /** Example sentences that contain the exact answer form, best first. */
    val usableExamples: List<String>
        get() {
            val regex = Regex("(?<![A-Za-z])${Regex.escape(answerForm)}(?![A-Za-z])", RegexOption.IGNORE_CASE)
            return (listOf(exampleSentence) + dictionaryExamples)
                .filter { it.isNotBlank() && regex.containsMatchIn(it) }
                .distinct()
        }

    val totalAttempts: Int
        get() = recognitionAttempts + recallAttempts + spellingAttempts +
                contextAttempts + synonymAttempts + productionAttempts + listeningAttempts

    val totalSuccesses: Int
        get() = recognitionSuccesses + recallSuccesses + spellingSuccesses +
                contextSuccesses + synonymSuccesses + productionSuccesses + listeningSuccesses

    val multiSkillAccuracy: Float
        get() = if (totalAttempts > 0) totalSuccesses.toFloat() / totalAttempts else 0f

    val isRecognized: Boolean
        get() = recognitionSuccesses >= 1 || recallSuccesses >= 1

    val isActivelyUsable: Boolean
        get() = (contextSuccesses >= 1 || spellingSuccesses >= 1 || productionSuccesses >= 1) && repetitionCount >= 2

    val isMasteredByRecall: Boolean
        get() = fsrsStability >= 14f && multiSkillAccuracy >= 0.85f && repetitionCount >= 3
}

const val AWL_NUMBER_OFFSET = 2000

enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

/** Native-language glosses shown next to English definitions. */
enum class TranslationLanguage(val code: String, val displayName: String, val nativeName: String) {
    NONE("", "Off", "English only"),
    PERSIAN("fa", "Persian", "فارسی"),
    HINDI("hi", "Hindi", "हिन्दी"),
    CHINESE("zh", "Chinese", "中文")
}

data class SkillMasterySummary(
    val direction: LearningDirection,
    val attempts: Int,
    val successes: Int,
    val accuracy: Float,
    val masteredWordsCount: Int
)

data class StudyStats(
    val totalEntries: Int,
    val verifiedEntries: Int,
    val unverifiedEntries: Int,
    val questionableEntries: Int,
    val correctedEntries: Int,
    val newCount: Int,
    val learningCount: Int,
    val reviewingCount: Int,
    val masteredCount: Int,
    val dueCount: Int,
    val forgottenCount: Int,
    val difficultCount: Int,
    val bookmarkedCount: Int,
    val overallCompletionPct: Float,
    val retentionRatePct: Float = 90f,
    val estimatedStudyMinutesRemaining: Int = 0,
    val activeRecalledCount: Int = 0
)

data class PageAudit(
    val page: Int,
    val entryCount: Int,
    val startNumber: Int,
    val endNumber: Int,
    val anomalyCount: Int,
    val status: String
)

data class QuizQuestion(
    val id: String,
    val direction: LearningDirection,
    val prompt: String,
    val targetWord: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val sourceDefinition: String,
    val grammaticalLabel: String? = null,
    val entryNumber: Int = 0
)

data class DictionaryResult(
    val headword: String,
    val phonetic: String?,
    val definitions: List<String>,
    val examples: List<String>,
    val synonyms: List<String>
)

data class ReviewLog(
    val id: Long = 0,
    val entryNumber: Int,
    val rating: Int,
    val reviewTimestamp: Long,
    val scheduledDays: Int,
    val stability: Float,
    val difficulty: Float,
    val channel: String = "FSRS"
)

data class WorkloadForecast(
    val dayOffset: Int,
    val dateLabel: String,
    val dueCount: Int
)

data class StudyPlan(
    val targetExamTimestamp: Long? = null,
    val dailyMinutes: Int = 20,
    val dailyNewWords: Int = 10,
    val desiredRetention: Float = 0.90f,
    val focusDirection: LearningDirection? = null,
    val lastStudyDay: Long = 0L,
    val streakDays: Int = 0
) {
    val daysUntilExam: Int?
        get() = targetExamTimestamp?.let {
            val diff = it - System.currentTimeMillis()
            if (diff > 0) (diff / (1000L * 60 * 60 * 24)).toInt() else 0
        }
}

data class ConfusablePair(
    val id: String,
    val wordA: String,
    val wordB: String,
    val distinction: String,
    val exampleA: String,
    val exampleB: String,
    val exerciseSentence: String,
    val correctWord: String,
    val explanation: String
)

data class CompleteWordsExercise(
    val id: String,
    val passageTitle: String,
    val textWithBlanks: String,
    val targetWord: String,
    val obscuredWord: String, // e.g. "c__si__ent"
    val hint: String,
    val explanation: String
)

data class AcademicReadingQuestion(
    val id: String,
    val passage: String,
    val question: String,
    val targetWord: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class WritingEvaluationResult(
    val targetWord: String,
    val submittedSentence: String,
    val isTargetWordPresent: Boolean,
    val strengths: List<String>,
    val suggestions: List<String>,
    val requiredPrepositionCheck: String?,
    val modelSentence: String,
    val estimatedAcademicScore: Int // 1 to 5
)

data class ToeflBackupData(
    val version: Int = 3,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val totalEntriesCount: Int,
    val userNotesCount: Int,
    val bookmarksCount: Int,
    val reviewsCount: Int,
    val entries: List<VocabEntry>,
    val studyPlan: StudyPlan? = null
)

/** Chart data: one value per week, oldest first. Accuracy is null for weeks without attempts. */
data class WeeklyProgress(
    val weekLabels: List<String>,
    val wordsLearned: List<Int>,
    val skillAccuracy: Map<LearningDirection, List<Float?>>
)
