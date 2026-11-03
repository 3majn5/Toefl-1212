package com.example.util

/**
 * Typo-tolerant answer checking for typed answers (spelling quizzes, cloze, dictation).
 *
 * A single slip — one wrong, missing, extra, or swapped letter — in a word of six or more
 * letters still counts as correct, but the learner is shown exactly where the slip was.
 */
object SpellingChecker {

    enum class Verdict { EXACT, MINOR_TYPO, CLOSE, WRONG }

    data class Result(
        val verdict: Verdict,
        val distance: Int,
        /** The correct answer split into segments; `true` marks letters the learner got wrong or missed. */
        val highlightedAnswer: List<Pair<String, Boolean>>
    ) {
        /** Exact answers and minor typos earn credit. */
        val isAccepted: Boolean get() = verdict == Verdict.EXACT || verdict == Verdict.MINOR_TYPO

        val feedback: String
            get() = when (verdict) {
                Verdict.EXACT -> "Perfect spelling!"
                Verdict.MINOR_TYPO -> "Accepted — just one letter off. Watch the highlighted letter."
                Verdict.CLOSE -> "So close! $distance letters off — compare with the highlighted letters."
                Verdict.WRONG -> "Not quite. Study the correct spelling below."
            }
    }

    fun check(input: String, answer: String): Result {
        val typed = normalize(input)
        val target = normalize(answer)
        val distance = damerauLevenshtein(typed, target)
        val verdict = when {
            distance == 0 -> Verdict.EXACT
            distance == 1 && target.length >= 6 -> Verdict.MINOR_TYPO
            distance <= maxOf(1, target.length / 4) -> Verdict.CLOSE
            else -> Verdict.WRONG
        }
        return Result(verdict, distance, highlight(typed, answer.trim()))
    }

    private fun normalize(s: String): String =
        s.trim().lowercase().replace(Regex("\\s+"), " ").replace('’', '\'')

    /** Optimal string alignment distance (Levenshtein plus adjacent transpositions). */
    fun damerauLevenshtein(a: String, b: String): Int {
        val d = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) d[i][0] = i
        for (j in 0..b.length) d[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    d[i][j] = minOf(d[i][j], d[i - 2][j - 2] + 1)
                }
            }
        }
        return d[a.length][b.length]
    }

    /** Marks each letter of [answer] that the typed text did not match, using an LCS alignment. */
    private fun highlight(typed: String, answer: String): List<Pair<String, Boolean>> {
        val target = answer.lowercase()
        val lcs = Array(typed.length + 1) { IntArray(target.length + 1) }
        for (i in typed.length - 1 downTo 0) {
            for (j in target.length - 1 downTo 0) {
                lcs[i][j] = if (typed[i] == target[j]) lcs[i + 1][j + 1] + 1 else maxOf(lcs[i + 1][j], lcs[i][j + 1])
            }
        }
        val matched = BooleanArray(target.length)
        var i = 0
        var j = 0
        while (i < typed.length && j < target.length) {
            when {
                typed[i] == target[j] -> { matched[j] = true; i++; j++ }
                lcs[i + 1][j] >= lcs[i][j + 1] -> i++
                else -> j++
            }
        }
        // Merge consecutive letters with the same state into segments
        val segments = mutableListOf<Pair<String, Boolean>>()
        answer.forEachIndexed { idx, ch ->
            val wrong = !matched[idx] && !ch.isWhitespace()
            if (segments.isNotEmpty() && segments.last().second == wrong) {
                val last = segments.removeAt(segments.lastIndex)
                segments.add(last.first + ch to wrong)
            } else {
                segments.add(ch.toString() to wrong)
            }
        }
        return segments
    }
}
