package com.example.util

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/** One word in the mistake notebook. */
data class MistakeRecord(
    val entryNumber: Int,
    val wrongCount: Int,
    val lastWrongAt: Long,
    /** Skill channels (LearningDirection names) where the word was missed. */
    val channels: Set<String>,
    /** Correct answers in a row since the last miss; two in a row clears the word. */
    val correctStreak: Int = 0
)

/**
 * Collects every word answered wrongly in any mode. A word leaves the notebook once it's
 * answered correctly [CLEAR_AFTER] times in a row, so the list always reflects current weak spots.
 */
class MistakeNotebook(private val prefs: SharedPreferences) {

    fun all(): List<MistakeRecord> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val ch = o.optJSONArray("channels")
                MistakeRecord(
                    entryNumber = o.getInt("n"),
                    wrongCount = o.getInt("wrong"),
                    lastWrongAt = o.getLong("last"),
                    channels = if (ch == null) emptySet() else (0 until ch.length()).map { ch.getString(it) }.toSet(),
                    correctStreak = o.optInt("streak", 0)
                )
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Records an answer for [entryNumber]. Returns true when this correct answer cleared the
     * word from the notebook.
     */
    fun record(entryNumber: Int, isCorrect: Boolean, channel: String, now: Long = System.currentTimeMillis()): Boolean {
        val records = all().associateBy { it.entryNumber }.toMutableMap()
        val existing = records[entryNumber]
        var cleared = false
        if (!isCorrect) {
            records[entryNumber] = MistakeRecord(
                entryNumber = entryNumber,
                wrongCount = (existing?.wrongCount ?: 0) + 1,
                lastWrongAt = now,
                channels = (existing?.channels ?: emptySet()) + channel,
                correctStreak = 0
            )
        } else if (existing != null) {
            val streak = existing.correctStreak + 1
            if (streak >= CLEAR_AFTER) {
                records.remove(entryNumber)
                cleared = true
            } else {
                records[entryNumber] = existing.copy(correctStreak = streak)
            }
        } else {
            return false
        }
        save(records.values)
        return cleared
    }

    fun remove(entryNumber: Int) = save(all().filter { it.entryNumber != entryNumber })

    fun clear() = prefs.edit().remove(KEY).apply()

    private fun save(records: Collection<MistakeRecord>) {
        val arr = JSONArray()
        records.sortedByDescending { it.lastWrongAt }.take(MAX_RECORDS).forEach { r ->
            arr.put(
                JSONObject()
                    .put("n", r.entryNumber)
                    .put("wrong", r.wrongCount)
                    .put("last", r.lastWrongAt)
                    .put("channels", JSONArray(r.channels.toList()))
                    .put("streak", r.correctStreak)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    companion object {
        const val CLEAR_AFTER = 2
        private const val MAX_RECORDS = 500
        private const val KEY = "mistake_notebook_v1"
    }
}
