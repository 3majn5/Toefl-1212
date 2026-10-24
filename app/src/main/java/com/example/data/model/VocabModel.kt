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

enum class LearningDirection(val label: String, val iconDescription: String) {
    RECOGNITION("Recognition", "Word to Definition"),
    REVERSE_RECALL("Reverse Recall", "Definition to Word"),
    SPELLING("Spelling", "Audio/Clue to Typed Word"),
    CONTEXT("Context", "Fill in the blank in sentence"),
    SYNONYM("Synonym Discrimination", "Identify closest TOEFL synonym"),
    PRODUCTION("Production", "Construct an original sentence"),
    LISTENING("Listening", "Audio pronunciation recognition")
}

data class VocabEntry(
    val entryNumber: Int,
    val sourcePage: Int,
    val headword: String,
    val rawText: String,
    val definitions: List<String>,
    val primaryMeaning: String,
    val grammaticalLabels: List<String>,
    val notes: String,
    val hasDuplicateHeadword: Boolean,
    val duplicateWithNumbers: List<Int>,
    val hasAnomaly: Boolean,
    val anomalyNotes: String,
    val userStatus: UserVerificationStatus = UserVerificationStatus.UNVERIFIED,
    val userCorrection: String? = null,
    val userNote: String? = null,
    val isDifficult: Boolean = false,
    val isBookmarked: Boolean = false,
    // Examples & Dictionary connection
    val exampleSentence: String = "",
    val dictionaryExamples: List<String> = emptyList(),
    val dictionaryDefinitions: List<String> = emptyList(),
    val dictionarySynonyms: List<String> = emptyList(),
    val phonetic: String? = null,
    // Learning progress fields
    val studyStatus: StudyStatus = StudyStatus.NEW,
    val repetitionCount: Int = 0,
    val reviewCount: Int = 0,
    val forgottenCount: Int = 0,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val nextReviewTimestamp: Long = 0L,
    val lastReviewedTimestamp: Long? = null
)

data class DictionaryResult(
    val term: String,
    val phonetic: String? = null,
    val definitions: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val source: String = "Wiktionary REST API"
)

data class PageAudit(
    val page: Int,
    val entryCount: Int,
    val startNumber: Int,
    val endNumber: Int,
    val anomalyCount: Int,
    val status: String
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
    val overallCompletionPercent: Float
)

data class QuizQuestion(
    val id: String,
    val entry: VocabEntry,
    val direction: LearningDirection,
    val prompt: String,
    val contextSentence: String? = null,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val sourceDefinition: String,
    val grammaticalLabel: String? = null
)
