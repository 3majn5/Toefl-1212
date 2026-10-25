package com.example.util

import com.example.data.model.StudyPlan
import com.example.data.model.StudyStatus
import com.example.data.model.ToeflBackupData
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import org.json.JSONArray
import org.json.JSONObject

object BackupRestoreManager {

    /**
     * Serializes all vocabulary entries and study plan settings to a portable JSON backup.
     */
    fun exportToJson(entries: List<VocabEntry>, plan: StudyPlan?): String {
        val root = JSONObject()
        root.put("version", 3)
        root.put("appName", "TOEFL 1212")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("totalEntriesCount", entries.size)

        var notesCount = 0
        var bookmarksCount = 0
        var reviewsCount = 0

        val entriesArray = JSONArray()
        for (e in entries) {
            if (!e.userNote.isNullOrBlank() || !e.userCorrection.isNullOrBlank()) notesCount++
            if (e.isBookmarked) bookmarksCount++
            if (e.reviewCount > 0 || e.repetitionCount > 0) reviewsCount++

            val obj = JSONObject()
            obj.put("entryNumber", e.entryNumber)
            obj.put("sourcePage", e.sourcePage)
            obj.put("headword", e.headword)
            obj.put("rawText", e.rawText)
            obj.put("primaryMeaning", e.primaryMeaning)
            obj.put("notes", e.notes)
            obj.put("userStatus", e.userStatus.name)
            obj.put("userCorrection", e.userCorrection ?: "")
            obj.put("userNote", e.userNote ?: "")
            obj.put("isDifficult", e.isDifficult)
            obj.put("isBookmarked", e.isBookmarked)
            obj.put("studyStatus", e.studyStatus.name)
            obj.put("repetitionCount", e.repetitionCount)
            obj.put("reviewCount", e.reviewCount)
            obj.put("forgottenCount", e.forgottenCount)
            obj.put("easeFactor", e.easeFactor.toDouble())
            obj.put("intervalDays", e.intervalDays)
            obj.put("nextReviewTimestamp", e.nextReviewTimestamp)
            obj.put("lastReviewedTimestamp", e.lastReviewedTimestamp ?: 0L)
            obj.put("fsrsStability", e.fsrsStability.toDouble())
            obj.put("fsrsDifficulty", e.fsrsDifficulty.toDouble())
            obj.put("fsrsState", e.fsrsState)
            obj.put("fsrsLapses", e.fsrsLapses)
            obj.put("lastRating", e.lastRating)

            // Skill counters
            obj.put("recognitionAttempts", e.recognitionAttempts)
            obj.put("recognitionSuccesses", e.recognitionSuccesses)
            obj.put("recallAttempts", e.recallAttempts)
            obj.put("recallSuccesses", e.recallSuccesses)
            obj.put("spellingAttempts", e.spellingAttempts)
            obj.put("spellingSuccesses", e.spellingSuccesses)
            obj.put("contextAttempts", e.contextAttempts)
            obj.put("contextSuccesses", e.contextSuccesses)
            obj.put("synonymAttempts", e.synonymAttempts)
            obj.put("synonymSuccesses", e.synonymSuccesses)
            obj.put("productionAttempts", e.productionAttempts)
            obj.put("productionSuccesses", e.productionSuccesses)
            obj.put("listeningAttempts", e.listeningAttempts)
            obj.put("listeningSuccesses", e.listeningSuccesses)

            entriesArray.put(obj)
        }

        root.put("userNotesCount", notesCount)
        root.put("bookmarksCount", bookmarksCount)
        root.put("reviewsCount", reviewsCount)
        root.put("entries", entriesArray)

        if (plan != null) {
            val planObj = JSONObject()
            planObj.put("targetExamTimestamp", plan.targetExamTimestamp ?: 0L)
            planObj.put("dailyMinutes", plan.dailyMinutes)
            planObj.put("dailyNewWords", plan.dailyNewWords)
            planObj.put("desiredRetention", plan.desiredRetention.toDouble())
            planObj.put("focusDirection", plan.focusDirection?.name ?: "")
            planObj.put("lastStudyDay", plan.lastStudyDay)
            planObj.put("streakDays", plan.streakDays)
            root.put("studyPlan", planObj)
        }

        return root.toString(2)
    }

    /**
     * Exports vocabulary data and learning status to standard CSV.
     */
    fun exportToCsv(entries: List<VocabEntry>): String {
        val sb = StringBuilder()
        sb.append("Entry Number,Source Page,Headword,Primary Meaning,Status,Repetitions,Next Review,Difficult,Bookmarked,User Note\n")
        for (e in entries) {
            val safeMeaning = e.primaryMeaning.replace("\"", "\"\"")
            val safeNote = (e.userNote ?: "").replace("\"", "\"\"")
            sb.append("${e.entryNumber},${e.sourcePage},\"${e.headword}\",\"$safeMeaning\",${e.studyStatus},${e.repetitionCount},${e.nextReviewTimestamp},${e.isDifficult},${e.isBookmarked},\"$safeNote\"\n")
        }
        return sb.toString()
    }

    fun importFromJson(jsonString: String, basePool: List<VocabEntry>): Result<ToeflBackupData> =
        validateAndParseJson(jsonString, basePool)

    /**
     * Validates and parses an incoming JSON backup safely without corrupting data.
     */
    fun validateAndParseJson(jsonString: String, basePool: List<VocabEntry>): Result<ToeflBackupData> {
        return runCatching {
            val root = JSONObject(jsonString)
            val version = root.optInt("version", 1)
            val entriesArray = root.getJSONArray("entries")

            val baseMap = basePool.associateBy { it.entryNumber }.toMutableMap()
            val parsedEntries = mutableListOf<VocabEntry>()

            for (i in 0 until entriesArray.length()) {
                val obj = entriesArray.getJSONObject(i)
                val num = obj.getInt("entryNumber")
                val base = baseMap[num] ?: continue

                val restored = base.copy(
                    userStatus = runCatching { UserVerificationStatus.valueOf(obj.optString("userStatus", "UNVERIFIED")) }.getOrDefault(base.userStatus),
                    userCorrection = obj.optString("userCorrection").takeIf { it.isNotBlank() },
                    userNote = obj.optString("userNote").takeIf { it.isNotBlank() },
                    isDifficult = obj.optBoolean("isDifficult", base.isDifficult),
                    isBookmarked = obj.optBoolean("isBookmarked", base.isBookmarked),
                    studyStatus = runCatching { StudyStatus.valueOf(obj.optString("studyStatus", "NEW")) }.getOrDefault(base.studyStatus),
                    repetitionCount = obj.optInt("repetitionCount", base.repetitionCount),
                    reviewCount = obj.optInt("reviewCount", base.reviewCount),
                    forgottenCount = obj.optInt("forgottenCount", base.forgottenCount),
                    easeFactor = obj.optDouble("easeFactor", base.easeFactor.toDouble()).toFloat(),
                    intervalDays = obj.optInt("intervalDays", base.intervalDays),
                    nextReviewTimestamp = obj.optLong("nextReviewTimestamp", base.nextReviewTimestamp),
                    lastReviewedTimestamp = obj.optLong("lastReviewedTimestamp", base.lastReviewedTimestamp ?: 0L).takeIf { it > 0 },
                    fsrsStability = obj.optDouble("fsrsStability", base.fsrsStability.toDouble()).toFloat(),
                    fsrsDifficulty = obj.optDouble("fsrsDifficulty", base.fsrsDifficulty.toDouble()).toFloat(),
                    fsrsState = obj.optInt("fsrsState", base.fsrsState),
                    fsrsLapses = obj.optInt("fsrsLapses", base.fsrsLapses),
                    lastRating = obj.optInt("lastRating", base.lastRating),
                    recognitionAttempts = obj.optInt("recognitionAttempts", base.recognitionAttempts),
                    recognitionSuccesses = obj.optInt("recognitionSuccesses", base.recognitionSuccesses),
                    recallAttempts = obj.optInt("recallAttempts", base.recallAttempts),
                    recallSuccesses = obj.optInt("recallSuccesses", base.recallSuccesses),
                    spellingAttempts = obj.optInt("spellingAttempts", base.spellingAttempts),
                    spellingSuccesses = obj.optInt("spellingSuccesses", base.spellingSuccesses),
                    contextAttempts = obj.optInt("contextAttempts", base.contextAttempts),
                    contextSuccesses = obj.optInt("contextSuccesses", base.contextSuccesses),
                    synonymAttempts = obj.optInt("synonymAttempts", base.synonymAttempts),
                    synonymSuccesses = obj.optInt("synonymSuccesses", base.synonymSuccesses),
                    productionAttempts = obj.optInt("productionAttempts", base.productionAttempts),
                    productionSuccesses = obj.optInt("productionSuccesses", base.productionSuccesses),
                    listeningAttempts = obj.optInt("listeningAttempts", base.listeningAttempts),
                    listeningSuccesses = obj.optInt("listeningSuccesses", base.listeningSuccesses)
                )
                parsedEntries.add(restored)
                baseMap[num] = restored
            }

            // Fill any missing from base pool so all 1,212 remain intact
            val fullFinalList = basePool.map { baseMap[it.entryNumber] ?: it }

            var plan: StudyPlan? = null
            if (root.has("studyPlan")) {
                val pObj = root.getJSONObject("studyPlan")
                plan = StudyPlan(
                    targetExamTimestamp = pObj.optLong("targetExamTimestamp", 0L).takeIf { it > 0 },
                    dailyMinutes = pObj.optInt("dailyMinutes", 20),
                    dailyNewWords = pObj.optInt("dailyNewWords", 10),
                    desiredRetention = pObj.optDouble("desiredRetention", 0.90).toFloat(),
                    lastStudyDay = pObj.optLong("lastStudyDay", 0L),
                    streakDays = pObj.optInt("streakDays", 0)
                )
            }

            ToeflBackupData(
                version = version,
                exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis()),
                totalEntriesCount = fullFinalList.size,
                userNotesCount = root.optInt("userNotesCount", 0),
                bookmarksCount = root.optInt("bookmarksCount", 0),
                reviewsCount = root.optInt("reviewsCount", 0),
                entries = fullFinalList,
                studyPlan = plan
            )
        }
    }
}
