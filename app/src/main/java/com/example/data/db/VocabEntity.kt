package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry

@Entity(tableName = "vocab_entries")
data class VocabEntity(
    @PrimaryKey val entryNumber: Int,
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
    val userStatus: String = UserVerificationStatus.UNVERIFIED.name,
    val userCorrection: String? = null,
    val userNote: String? = null,
    val isDifficult: Boolean = false,
    val isBookmarked: Boolean = false,
    // Examples & Dictionary integration
    val exampleSentence: String = "",
    val dictionaryExamples: List<String> = emptyList(),
    val dictionaryDefinitions: List<String> = emptyList(),
    val dictionarySynonyms: List<String> = emptyList(),
    val phonetic: String? = null,
    // Spaced Repetition & Study State
    val studyStatus: String = StudyStatus.NEW.name,
    val repetitionCount: Int = 0,
    val reviewCount: Int = 0,
    val forgottenCount: Int = 0,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val nextReviewTimestamp: Long = 0L,
    val lastReviewedTimestamp: Long? = null
) {
    fun toDomain(): VocabEntry {
        return VocabEntry(
            entryNumber = entryNumber,
            sourcePage = sourcePage,
            headword = headword,
            rawText = rawText,
            definitions = definitions,
            primaryMeaning = primaryMeaning,
            grammaticalLabels = grammaticalLabels,
            notes = notes,
            hasDuplicateHeadword = hasDuplicateHeadword,
            duplicateWithNumbers = duplicateWithNumbers,
            hasAnomaly = hasAnomaly,
            anomalyNotes = anomalyNotes,
            userStatus = runCatching { UserVerificationStatus.valueOf(userStatus) }.getOrDefault(UserVerificationStatus.UNVERIFIED),
            userCorrection = userCorrection,
            userNote = userNote,
            isDifficult = isDifficult,
            isBookmarked = isBookmarked,
            exampleSentence = exampleSentence,
            dictionaryExamples = dictionaryExamples,
            dictionaryDefinitions = dictionaryDefinitions,
            dictionarySynonyms = dictionarySynonyms,
            phonetic = phonetic,
            studyStatus = runCatching { StudyStatus.valueOf(studyStatus) }.getOrDefault(StudyStatus.NEW),
            repetitionCount = repetitionCount,
            reviewCount = reviewCount,
            forgottenCount = forgottenCount,
            easeFactor = easeFactor,
            intervalDays = intervalDays,
            nextReviewTimestamp = nextReviewTimestamp,
            lastReviewedTimestamp = lastReviewedTimestamp
        )
    }

    companion object {
        fun fromDomain(entry: VocabEntry): VocabEntity {
            return VocabEntity(
                entryNumber = entry.entryNumber,
                sourcePage = entry.sourcePage,
                headword = entry.headword,
                rawText = entry.rawText,
                definitions = entry.definitions,
                primaryMeaning = entry.primaryMeaning,
                grammaticalLabels = entry.grammaticalLabels,
                notes = entry.notes,
                hasDuplicateHeadword = entry.hasDuplicateHeadword,
                duplicateWithNumbers = entry.duplicateWithNumbers,
                hasAnomaly = entry.hasAnomaly,
                anomalyNotes = entry.anomalyNotes,
                userStatus = entry.userStatus.name,
                userCorrection = entry.userCorrection,
                userNote = entry.userNote,
                isDifficult = entry.isDifficult,
                isBookmarked = entry.isBookmarked,
                exampleSentence = entry.exampleSentence,
                dictionaryExamples = entry.dictionaryExamples,
                dictionaryDefinitions = entry.dictionaryDefinitions,
                dictionarySynonyms = entry.dictionarySynonyms,
                phonetic = entry.phonetic,
                studyStatus = entry.studyStatus.name,
                repetitionCount = entry.repetitionCount,
                reviewCount = entry.reviewCount,
                forgottenCount = entry.forgottenCount,
                easeFactor = entry.easeFactor,
                intervalDays = entry.intervalDays,
                nextReviewTimestamp = entry.nextReviewTimestamp,
                lastReviewedTimestamp = entry.lastReviewedTimestamp
            )
        }
    }
}
