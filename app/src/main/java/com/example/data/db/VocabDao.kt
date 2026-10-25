package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {
    @Query("SELECT * FROM vocab_entries ORDER BY entryNumber ASC")
    fun getAll(): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocab_entries ORDER BY entryNumber ASC")
    suspend fun getAllDirect(): List<VocabEntity>

    @Query("SELECT * FROM vocab_entries WHERE entryNumber = :num LIMIT 1")
    suspend fun getByNumberDirect(num: Int): VocabEntity?

    @Query("SELECT * FROM vocab_entries WHERE entryNumber = :num LIMIT 1")
    fun getByNumber(num: Int): Flow<VocabEntity?>

    @Query("SELECT * FROM vocab_entries WHERE sourcePage = :page ORDER BY entryNumber ASC")
    fun getByPage(page: Int): Flow<List<VocabEntity>>

    @Query("""
        SELECT * FROM vocab_entries 
        WHERE headword LIKE '%' || :query || '%' 
           OR primaryMeaning LIKE '%' || :query || '%'
           OR rawText LIKE '%' || :query || '%'
           OR CAST(entryNumber AS TEXT) LIKE '%' || :query || '%'
        ORDER BY entryNumber ASC
    """)
    fun search(query: String): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocab_entries WHERE isDifficult = 1 ORDER BY entryNumber ASC")
    fun getDifficultEntries(): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocab_entries WHERE isBookmarked = 1 ORDER BY entryNumber ASC")
    fun getBookmarkedEntries(): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocab_entries WHERE hasAnomaly = 1 ORDER BY entryNumber ASC")
    fun getAnomalyEntries(): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocab_entries WHERE studyStatus != 'NEW' AND nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    suspend fun getDueEntriesDirect(currentTime: Long): List<VocabEntity>

    @Query("SELECT * FROM vocab_entries WHERE studyStatus != 'NEW' AND nextReviewTimestamp < :currentTime - (24 * 60 * 60 * 1000) ORDER BY nextReviewTimestamp ASC")
    suspend fun getOverdueEntriesDirect(currentTime: Long): List<VocabEntity>

    @Query("SELECT * FROM vocab_entries WHERE nextReviewTimestamp >= :start AND nextReviewTimestamp < :end")
    suspend fun getEntriesDueBetween(start: Long, end: Long): List<VocabEntity>

    @Query("SELECT * FROM vocab_entries WHERE studyStatus = 'NEW' ORDER BY entryNumber ASC LIMIT :limit")
    suspend fun getNewEntriesDirect(limit: Int): List<VocabEntity>

    @Query("SELECT * FROM vocab_entries WHERE forgottenCount > 0 ORDER BY forgottenCount DESC, entryNumber ASC")
    fun getFrequentlyForgottenEntries(): Flow<List<VocabEntity>>

    @Query("SELECT COUNT(*) FROM vocab_entries")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<VocabEntity>)

    @Update
    suspend fun update(entry: VocabEntity)

    @Query("UPDATE vocab_entries SET isDifficult = :isDifficult WHERE entryNumber = :entryNumber")
    suspend fun updateDifficult(entryNumber: Int, isDifficult: Boolean)

    @Query("UPDATE vocab_entries SET isBookmarked = :isBookmarked WHERE entryNumber = :entryNumber")
    suspend fun updateBookmarked(entryNumber: Int, isBookmarked: Boolean)

    @Query("""
        UPDATE vocab_entries 
        SET userStatus = :userStatus, 
            userCorrection = :correction, 
            userNote = :note 
        WHERE entryNumber = :entryNumber
    """)
    suspend fun updateVerification(entryNumber: Int, userStatus: String, correction: String?, note: String?)

    @Query("""
        UPDATE vocab_entries 
        SET studyStatus = :studyStatus,
            repetitionCount = :repetitionCount,
            reviewCount = :reviewCount,
            forgottenCount = :forgottenCount,
            easeFactor = :easeFactor,
            intervalDays = :intervalDays,
            nextReviewTimestamp = :nextReview,
            lastReviewedTimestamp = :lastReviewed,
            fsrsStability = :stability,
            fsrsDifficulty = :difficulty,
            fsrsState = :state,
            fsrsLapses = :lapses,
            lastRating = :rating
        WHERE entryNumber = :entryNumber
    """)
    suspend fun updateFsrsProgress(
        entryNumber: Int,
        studyStatus: String,
        repetitionCount: Int,
        reviewCount: Int,
        forgottenCount: Int,
        easeFactor: Float,
        intervalDays: Int,
        nextReview: Long,
        lastReviewed: Long,
        stability: Float,
        difficulty: Float,
        state: Int,
        lapses: Int,
        rating: Int
    )

    @Query("""
        UPDATE vocab_entries 
        SET dictionaryExamples = :examples,
            dictionaryDefinitions = :definitions,
            dictionarySynonyms = :synonyms,
            phonetic = :phonetic
        WHERE entryNumber = :entryNumber
    """)
    suspend fun updateDictionaryData(
        entryNumber: Int,
        examples: List<String>,
        definitions: List<String>,
        synonyms: List<String>,
        phonetic: String?
    )

    @Query("""
        UPDATE vocab_entries 
        SET collocations = :collocations,
            wordFamily = :wordFamily,
            grammarPatterns = :grammarPatterns,
            commonMistakes = :commonMistakes,
            registerFormality = :registerFormality,
            antonyms = :antonyms
        WHERE entryNumber = :entryNumber
    """)
    suspend fun updateLinguisticProfile(
        entryNumber: Int,
        collocations: List<String>,
        wordFamily: List<String>,
        grammarPatterns: List<String>,
        commonMistakes: String,
        registerFormality: String,
        antonyms: List<String>
    )

    @Query("""
        UPDATE vocab_entries
        SET recognitionAttempts = recognitionAttempts + :recAtt,
            recognitionSuccesses = recognitionSuccesses + :recSucc,
            recallAttempts = recallAttempts + :recallAtt,
            recallSuccesses = recallSuccesses + :recallSucc,
            spellingAttempts = spellingAttempts + :spellAtt,
            spellingSuccesses = spellingSuccesses + :spellSucc,
            contextAttempts = contextAttempts + :ctxAtt,
            contextSuccesses = contextSuccesses + :ctxSucc,
            synonymAttempts = synonymAttempts + :synAtt,
            synonymSuccesses = synonymSuccesses + :synSucc,
            productionAttempts = productionAttempts + :prodAtt,
            productionSuccesses = productionSuccesses + :prodSucc,
            listeningAttempts = listeningAttempts + :listAtt,
            listeningSuccesses = listeningSuccesses + :listSucc
        WHERE entryNumber = :entryNumber
    """)
    suspend fun incrementSkillStats(
        entryNumber: Int,
        recAtt: Int = 0, recSucc: Int = 0,
        recallAtt: Int = 0, recallSucc: Int = 0,
        spellAtt: Int = 0, spellSucc: Int = 0,
        ctxAtt: Int = 0, ctxSucc: Int = 0,
        synAtt: Int = 0, synSucc: Int = 0,
        prodAtt: Int = 0, prodSucc: Int = 0,
        listAtt: Int = 0, listSucc: Int = 0
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: ReviewLogEntity)

    @Query("SELECT * FROM review_logs WHERE entryNumber = :entryNumber ORDER BY reviewTimestamp DESC")
    suspend fun getReviewLogsForWord(entryNumber: Int): List<ReviewLogEntity>

    @Query("SELECT * FROM review_logs ORDER BY reviewTimestamp DESC LIMIT :limit")
    suspend fun getRecentReviewLogs(limit: Int): List<ReviewLogEntity>

    @Query("DELETE FROM vocab_entries")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM review_logs")
    suspend fun deleteAllReviewLogs()
}
