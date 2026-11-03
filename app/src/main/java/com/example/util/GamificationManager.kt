package com.example.util

import android.content.SharedPreferences
import com.example.data.model.StudyStats
import java.util.concurrent.TimeUnit

enum class Badge(val title: String, val description: String, val emoji: String) {
    FIRST_STEP("First Step", "Complete your first review", "🌱"),
    STREAK_3("On a Roll", "Study 3 days in a row", "🔥"),
    STREAK_7("Week Warrior", "Study 7 days in a row", "⚡"),
    STREAK_30("Unstoppable", "Study 30 days in a row", "🏆"),
    WORDS_50("Word Collector", "Start learning 50 words", "📚"),
    WORDS_250("Vocabulary Builder", "Start learning 250 words", "🧱"),
    WORDS_1000("Lexicon Legend", "Start learning 1,000 words", "👑"),
    MASTER_25("Memory Master", "Master 25 words", "🧠"),
    MASTER_100("Long-Term Memory", "Master 100 words", "💎"),
    XP_1000("Rising Scholar", "Earn 1,000 XP", "⭐"),
    XP_5000("Academic Star", "Earn 5,000 XP", "🌟"),
    PERFECT_SESSION("Flawless", "Finish a quiz of 5+ questions with no mistakes", "🎯"),
    ALL_SKILLS("All-Rounder", "Practice all 7 skill channels", "🎨"),
    WRITER("Wordsmith", "Write 10 strong sentences", "✍️"),
    DAILY_GOAL("Goal Getter", "Reach your daily XP goal", "✅")
}

data class GamificationState(
    val totalXp: Int = 0,
    val level: Int = 1,
    val xpIntoLevel: Int = 0,
    val xpForNextLevel: Int = LEVEL_STEP,
    val streakDays: Int = 0,
    val bestStreak: Int = 0,
    val todayXp: Int = 0,
    val dailyGoalXp: Int = DEFAULT_DAILY_GOAL_XP,
    val earnedBadges: Set<Badge> = emptySet(),
    val studiedToday: Boolean = false
) {
    val levelProgress: Float get() = xpIntoLevel.toFloat() / xpForNextLevel
    val dailyProgress: Float get() = (todayXp.toFloat() / dailyGoalXp).coerceAtMost(1f)
}

/** Event shown as a floating "+10 XP" or a badge toast. */
data class RewardEvent(val xp: Int, val newBadges: List<Badge> = emptyList(), val id: Long = System.nanoTime())

const val LEVEL_STEP = 150
const val DEFAULT_DAILY_GOAL_XP = 60

/**
 * Tracks XP, levels, daily streaks and badges in SharedPreferences.
 * Level n needs n × 150 XP to complete, so early levels come quickly and later ones take longer.
 */
class GamificationManager(private val prefs: SharedPreferences) {

    private fun today(): Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() + java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()))

    fun state(): GamificationState {
        val xp = prefs.getInt(KEY_XP, 0)
        var level = 1
        var remaining = xp
        while (remaining >= level * LEVEL_STEP) {
            remaining -= level * LEVEL_STEP
            level++
        }
        val lastDay = prefs.getLong(KEY_LAST_DAY, -1L)
        val todayDay = today()
        // A streak survives until the end of the day after the last study day
        val streak = if (lastDay == todayDay || lastDay == todayDay - 1) prefs.getInt(KEY_STREAK, 0) else 0
        return GamificationState(
            totalXp = xp,
            level = level,
            xpIntoLevel = remaining,
            xpForNextLevel = level * LEVEL_STEP,
            streakDays = streak,
            bestStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            todayXp = if (prefs.getLong(KEY_XP_DAY, -1L) == todayDay) prefs.getInt(KEY_TODAY_XP, 0) else 0,
            dailyGoalXp = prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL_XP),
            earnedBadges = prefs.getStringSet(KEY_BADGES, emptySet()).orEmpty()
                .mapNotNull { runCatching { Badge.valueOf(it) }.getOrNull() }.toSet(),
            studiedToday = lastDay == todayDay
        )
    }

    /** Adds XP, updates the streak, and returns the reward (including any newly earned badges). */
    fun award(xp: Int, stats: StudyStats?, skillsPracticed: Int = 0, perfectSession: Boolean = false, strongSentence: Boolean = false): RewardEvent {
        val todayDay = today()
        val editor = prefs.edit()

        // Streak
        val lastDay = prefs.getLong(KEY_LAST_DAY, -1L)
        val streak = when (lastDay) {
            todayDay -> prefs.getInt(KEY_STREAK, 1)
            todayDay - 1 -> prefs.getInt(KEY_STREAK, 0) + 1
            else -> 1
        }
        editor.putLong(KEY_LAST_DAY, todayDay).putInt(KEY_STREAK, streak)
        editor.putInt(KEY_BEST_STREAK, maxOf(streak, prefs.getInt(KEY_BEST_STREAK, 0)))

        // XP
        val totalXp = prefs.getInt(KEY_XP, 0) + xp
        val todayXp = (if (prefs.getLong(KEY_XP_DAY, -1L) == todayDay) prefs.getInt(KEY_TODAY_XP, 0) else 0) + xp
        editor.putInt(KEY_XP, totalXp).putInt(KEY_TODAY_XP, todayXp).putLong(KEY_XP_DAY, todayDay)

        val sentences = prefs.getInt(KEY_SENTENCES, 0) + if (strongSentence) 1 else 0
        editor.putInt(KEY_SENTENCES, sentences)

        // Badges
        val earned = prefs.getStringSet(KEY_BADGES, emptySet()).orEmpty().toMutableSet()
        val learned = stats?.let { it.learningCount + it.reviewingCount + it.masteredCount } ?: 0
        val unlocked = Badge.values().filter { badge ->
            badge.name !in earned && when (badge) {
                Badge.FIRST_STEP -> true
                Badge.STREAK_3 -> streak >= 3
                Badge.STREAK_7 -> streak >= 7
                Badge.STREAK_30 -> streak >= 30
                Badge.WORDS_50 -> learned >= 50
                Badge.WORDS_250 -> learned >= 250
                Badge.WORDS_1000 -> learned >= 1000
                Badge.MASTER_25 -> (stats?.masteredCount ?: 0) >= 25
                Badge.MASTER_100 -> (stats?.masteredCount ?: 0) >= 100
                Badge.XP_1000 -> totalXp >= 1000
                Badge.XP_5000 -> totalXp >= 5000
                Badge.PERFECT_SESSION -> perfectSession
                Badge.ALL_SKILLS -> skillsPracticed >= 7
                Badge.WRITER -> sentences >= 10
                Badge.DAILY_GOAL -> todayXp >= prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL_XP)
            }
        }
        if (unlocked.isNotEmpty()) {
            earned += unlocked.map { it.name }
            editor.putStringSet(KEY_BADGES, earned)
        }
        editor.apply()
        return RewardEvent(xp = xp, newBadges = unlocked)
    }

    fun setDailyGoal(xp: Int) = prefs.edit().putInt(KEY_DAILY_GOAL, xp.coerceIn(20, 500)).apply()

    companion object {
        // XP rewards
        fun xpForRating(rating: Int): Int = when (rating) { 1 -> 2; 2 -> 6; 3 -> 10; else -> 12 }
        const val XP_CORRECT = 10
        const val XP_WRONG = 2
        const val XP_SESSION_BONUS = 20
        const val XP_PERFECT_BONUS = 30

        private const val KEY_XP = "game_total_xp"
        private const val KEY_TODAY_XP = "game_today_xp"
        private const val KEY_XP_DAY = "game_xp_day"
        private const val KEY_LAST_DAY = "game_last_study_day"
        private const val KEY_STREAK = "game_streak"
        private const val KEY_BEST_STREAK = "game_best_streak"
        private const val KEY_BADGES = "game_badges"
        private const val KEY_DAILY_GOAL = "game_daily_goal_xp"
        private const val KEY_SENTENCES = "game_strong_sentences"
    }
}
