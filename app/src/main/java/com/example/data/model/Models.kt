package com.example.data.model

enum class UserVerificationStatus {
    UNVERIFIED,
    VERIFIED,
    QUESTIONABLE,
    CORRECTED,
    NEEDS_EXPLANATION
}

enum class StudyStatus {
    NEW,
    LEARNING,
    REVIEWING,
    MASTERED
}

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
    val lastReviewedTimestamp: Long? = null
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
    val overallCompletionPct: Float
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
    val grammaticalLabel: String? = null
)

data class DictionaryResult(
    val headword: String,
    val phonetic: String?,
    val definitions: List<String>,
    val examples: List<String>,
    val synonyms: List<String>
)
