package com.example.util

import android.content.SharedPreferences
import com.example.data.model.StudyStats
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random

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
    DAILY_GOAL("Goal Getter", "Reach your daily XP goal", "✅"),
    COMBO_10("Combo King", "Answer 10 questions correctly in a row", "⚡"),
    LUCKY("Lucky Star", "Hit a lucky double-XP bonus", "🍀"),
    QUEST_MASTER("Quest Master", "Complete all three daily quests in one day", "🗺️")
}

/** Small daily goals; three are picked each day so every day feels slightly different. */
enum class QuestType(val title: String, val emoji: String, val target: Int) {
    CORRECT_ANSWERS("Answer 15 questions correctly", "🎯", 15),
    COMBO("Reach a 5-answer combo", "⚡", 5),
    FLASHCARDS("Review 10 flashcards", "🃏", 10),
    EARN_XP("Earn 100 XP today", "⭐", 100),
    SKILLS("Practice 3 different skills", "🎨", 3),
    PERFECT("Finish a quiz with no mistakes", "💎", 1)
}

data class QuestProgress(val type: QuestType, val progress: Int, val done: Boolean) {
    val fraction: Float get() = (progress.toFloat() / type.target).coerceIn(0f, 1f)
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
    val studiedToday: Boolean = false,
    val streakFreezes: Int = 0,
    val quests: List<QuestProgress> = emptyList(),
    val chestAvailable: Boolean = false,
    val chestClaimedToday: Boolean = false
) {
    val levelProgress: Float get() = xpIntoLevel.toFloat() / xpForNextLevel
    val dailyProgress: Float get() = (todayXp.toFloat() / dailyGoalXp).coerceAtMost(1f)
    val xpToGoal: Int get() = (dailyGoalXp - todayXp).coerceAtLeast(0)
}

/** What the learner just did, so quests and badges can react to it. */
data class Activity(
    val correctAnswer: Boolean = false,
    val flashcard: Boolean = false,
    val combo: Int = 0,
    val skill: String? = null,
    val perfectSession: Boolean = false,
    val strongSentence: Boolean = false
)

/** Event drawn by the reward overlay: floating XP, combo/lucky chips, badges, quests and level-ups. */
data class RewardEvent(
    val xp: Int,
    val newBadges: List<Badge> = emptyList(),
    val lucky: Boolean = false,
    val multiplier: Float = 1f,
    val combo: Int = 0,
    val levelUp: Int? = null,
    val completedQuests: List<QuestType> = emptyList(),
    val freezeUsed: Boolean = false,
    val freezeEarned: Boolean = false,
    val id: Long = System.nanoTime()
)

/** What the mystery chest contained. */
data class ChestReward(val xp: Int, val streakFreeze: Boolean, val jackpot: Boolean)

const val LEVEL_STEP = 150
const val DEFAULT_DAILY_GOAL_XP = 60
const val MAX_STREAK_FREEZES = 2

/**
 * Tracks XP, levels, daily streaks, quests, streak freezes and badges in SharedPreferences.
 * Level n needs n × 150 XP to complete, so early levels come quickly and later ones take longer.
 */
class GamificationManager(private val prefs: SharedPreferences, private val random: Random = Random.Default) {

    private fun today(): Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() + java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()))

    private fun levelFor(xp: Int): Triple<Int, Int, Int> {
        var level = 1
        var remaining = xp
        while (remaining >= level * LEVEL_STEP) {
            remaining -= level * LEVEL_STEP
            level++
        }
        return Triple(level, remaining, level * LEVEL_STEP)
    }

    fun state(): GamificationState {
        val xp = prefs.getInt(KEY_XP, 0)
        val (level, remaining, next) = levelFor(xp)
        val lastDay = prefs.getLong(KEY_LAST_DAY, -1L)
        val todayDay = today()
        val freezes = prefs.getInt(KEY_FREEZES, 0)
        // A streak survives until the end of the day after the last study day, plus one day per freeze
        val missedDays = if (lastDay < 0) Long.MAX_VALUE else todayDay - lastDay - 1
        val streak = if (missedDays <= freezes) prefs.getInt(KEY_STREAK, 0) else 0
        val todayXp = if (prefs.getLong(KEY_XP_DAY, -1L) == todayDay) prefs.getInt(KEY_TODAY_XP, 0) else 0
        val goal = prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL_XP)
        val claimed = prefs.getLong(KEY_CHEST_DAY, -1L) == todayDay
        return GamificationState(
            totalXp = xp,
            level = level,
            xpIntoLevel = remaining,
            xpForNextLevel = next,
            streakDays = streak,
            bestStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            todayXp = todayXp,
            dailyGoalXp = goal,
            earnedBadges = prefs.getStringSet(KEY_BADGES, emptySet()).orEmpty()
                .mapNotNull { runCatching { Badge.valueOf(it) }.getOrNull() }.toSet(),
            studiedToday = lastDay == todayDay,
            streakFreezes = freezes,
            quests = questsFor(todayDay),
            chestAvailable = todayXp >= goal && !claimed,
            chestClaimedToday = claimed
        )
    }

    /** Today's three quests, chosen by a day-seeded shuffle so they stay fixed all day. */
    fun todaysQuestTypes(day: Long = today()): List<QuestType> =
        QuestType.values().toList().shuffled(Random(day)).take(3)

    private fun questsFor(day: Long): List<QuestProgress> {
        val sameDay = prefs.getLong(KEY_QUEST_DAY, -1L) == day
        return todaysQuestTypes(day).map { type ->
            QuestProgress(
                type = type,
                progress = if (sameDay) prefs.getInt(KEY_QUEST_PREFIX + type.name, 0) else 0,
                done = sameDay && prefs.getBoolean(KEY_QUEST_DONE_PREFIX + type.name, false)
            )
        }
    }

    /**
     * Adds XP (after the combo [multiplier] and the [lucky] double), updates the streak and quests,
     * and returns the reward with any level-up and newly earned badges.
     */
    fun award(
        baseXp: Int,
        stats: StudyStats?,
        skillsPracticed: Int = 0,
        activity: Activity = Activity(),
        multiplier: Float = 1f,
        lucky: Boolean = false
    ): RewardEvent {
        val todayDay = today()
        val editor = prefs.edit()
        val levelBefore = levelFor(prefs.getInt(KEY_XP, 0)).first

        // Streak, spending freezes to bridge missed days
        val lastDay = prefs.getLong(KEY_LAST_DAY, -1L)
        var freezes = prefs.getInt(KEY_FREEZES, 0)
        var freezeUsed = false
        var freezeEarned = false
        val streak = when {
            lastDay == todayDay -> prefs.getInt(KEY_STREAK, 1)
            lastDay == todayDay - 1 -> prefs.getInt(KEY_STREAK, 0) + 1
            lastDay >= 0 && todayDay - lastDay - 1 <= freezes -> {
                freezes -= (todayDay - lastDay - 1).toInt()
                freezeUsed = true
                prefs.getInt(KEY_STREAK, 0) + 1
            }
            else -> 1
        }
        // Every 7th streak day earns a freeze (up to the cap)
        if (lastDay != todayDay && streak % 7 == 0 && freezes < MAX_STREAK_FREEZES) {
            freezes++
            freezeEarned = true
        }
        editor.putInt(KEY_FREEZES, freezes)
        editor.putLong(KEY_LAST_DAY, todayDay).putInt(KEY_STREAK, streak)
        editor.putInt(KEY_BEST_STREAK, maxOf(streak, prefs.getInt(KEY_BEST_STREAK, 0)))

        // XP
        var xp = (baseXp * multiplier).roundToInt() * if (lucky) 2 else 1
        val previousTodayXp = if (prefs.getLong(KEY_XP_DAY, -1L) == todayDay) prefs.getInt(KEY_TODAY_XP, 0) else 0

        // Quests
        val sameQuestDay = prefs.getLong(KEY_QUEST_DAY, -1L) == todayDay
        if (!sameQuestDay) {
            QuestType.values().forEach {
                editor.remove(KEY_QUEST_PREFIX + it.name).remove(KEY_QUEST_DONE_PREFIX + it.name)
            }
            editor.remove(KEY_QUEST_SKILLS)
        }
        editor.putLong(KEY_QUEST_DAY, todayDay)
        val skillsToday = (if (sameQuestDay) prefs.getStringSet(KEY_QUEST_SKILLS, emptySet()).orEmpty() else emptySet())
            .toMutableSet().apply { activity.skill?.let { add(it) } }
        editor.putStringSet(KEY_QUEST_SKILLS, skillsToday)

        val completed = mutableListOf<QuestType>()
        var questsDoneToday = 0
        for (type in todaysQuestTypes(todayDay)) {
            val old = if (sameQuestDay) prefs.getInt(KEY_QUEST_PREFIX + type.name, 0) else 0
            val wasDone = sameQuestDay && prefs.getBoolean(KEY_QUEST_DONE_PREFIX + type.name, false)
            val progress = when (type) {
                QuestType.CORRECT_ANSWERS -> old + if (activity.correctAnswer) 1 else 0
                QuestType.COMBO -> maxOf(old, activity.combo)
                QuestType.FLASHCARDS -> old + if (activity.flashcard) 1 else 0
                QuestType.EARN_XP -> previousTodayXp + xp
                QuestType.SKILLS -> skillsToday.size
                QuestType.PERFECT -> old + if (activity.perfectSession) 1 else 0
            }.coerceAtMost(type.target)
            editor.putInt(KEY_QUEST_PREFIX + type.name, progress)
            if (!wasDone && progress >= type.target) {
                editor.putBoolean(KEY_QUEST_DONE_PREFIX + type.name, true)
                completed += type
            }
            if (wasDone || progress >= type.target) questsDoneToday++
        }
        xp += completed.size * QUEST_XP

        val totalXp = prefs.getInt(KEY_XP, 0) + xp
        val todayXp = previousTodayXp + xp
        editor.putInt(KEY_XP, totalXp).putInt(KEY_TODAY_XP, todayXp).putLong(KEY_XP_DAY, todayDay)
        val levelAfter = levelFor(totalXp).first

        val sentences = prefs.getInt(KEY_SENTENCES, 0) + if (activity.strongSentence) 1 else 0
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
                Badge.PERFECT_SESSION -> activity.perfectSession
                Badge.ALL_SKILLS -> skillsPracticed >= 7
                Badge.WRITER -> sentences >= 10
                Badge.DAILY_GOAL -> todayXp >= prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL_XP)
                Badge.COMBO_10 -> activity.combo >= 10
                Badge.LUCKY -> lucky
                Badge.QUEST_MASTER -> questsDoneToday >= 3
            }
        }
        if (unlocked.isNotEmpty()) {
            earned += unlocked.map { it.name }
            editor.putStringSet(KEY_BADGES, earned)
        }
        editor.apply()
        return RewardEvent(
            xp = xp,
            newBadges = unlocked,
            lucky = lucky,
            multiplier = multiplier,
            combo = activity.combo,
            levelUp = levelAfter.takeIf { it > levelBefore },
            completedQuests = completed,
            freezeUsed = freezeUsed,
            freezeEarned = freezeEarned
        )
    }

    /**
     * Opens today's mystery chest (only once the daily goal is reached). Rewards vary on purpose:
     * mostly a modest XP bonus, sometimes a streak freeze, occasionally a jackpot.
     */
    fun openChest(stats: StudyStats?): Pair<ChestReward, RewardEvent>? {
        if (!state().chestAvailable) return null
        prefs.edit().putLong(KEY_CHEST_DAY, today()).apply()
        val roll = random.nextFloat()
        val freezes = prefs.getInt(KEY_FREEZES, 0)
        val reward = when {
            roll < 0.08f -> ChestReward(xp = 150, streakFreeze = false, jackpot = true)
            roll < 0.25f && freezes < MAX_STREAK_FREEZES -> ChestReward(xp = 20, streakFreeze = true, jackpot = false)
            roll < 0.55f -> ChestReward(xp = random.nextInt(60, 101), streakFreeze = false, jackpot = false)
            else -> ChestReward(xp = random.nextInt(25, 56), streakFreeze = false, jackpot = false)
        }
        if (reward.streakFreeze) prefs.edit().putInt(KEY_FREEZES, freezes + 1).apply()
        val event = award(reward.xp, stats)
        return reward to event.copy(freezeEarned = reward.streakFreeze)
    }

    fun setDailyGoal(xp: Int) = prefs.edit().putInt(KEY_DAILY_GOAL, xp.coerceIn(20, 500)).apply()

    companion object {
        // XP rewards
        fun xpForRating(rating: Int): Int = when (rating) { 1 -> 2; 2 -> 6; 3 -> 10; else -> 12 }
        const val XP_CORRECT = 10
        const val XP_WRONG = 2
        const val XP_SESSION_BONUS = 20
        const val XP_PERFECT_BONUS = 30
        const val QUEST_XP = 25

        /** Chance that a correct answer earns a surprise double-XP bonus. */
        const val LUCKY_CHANCE = 0.12f

        /** Consecutive correct answers raise the XP multiplier in steps. */
        fun comboMultiplier(combo: Int): Float = when {
            combo >= 10 -> 3f
            combo >= 5 -> 2f
            combo >= 3 -> 1.5f
            else -> 1f
        }

        private const val KEY_XP = "game_total_xp"
        private const val KEY_TODAY_XP = "game_today_xp"
        private const val KEY_XP_DAY = "game_xp_day"
        private const val KEY_LAST_DAY = "game_last_study_day"
        private const val KEY_STREAK = "game_streak"
        private const val KEY_BEST_STREAK = "game_best_streak"
        private const val KEY_BADGES = "game_badges"
        private const val KEY_DAILY_GOAL = "game_daily_goal_xp"
        private const val KEY_SENTENCES = "game_strong_sentences"
        private const val KEY_FREEZES = "game_streak_freezes"
        private const val KEY_CHEST_DAY = "game_chest_day"
        private const val KEY_QUEST_DAY = "game_quest_day"
        private const val KEY_QUEST_PREFIX = "game_quest_"
        private const val KEY_QUEST_DONE_PREFIX = "game_quest_done_"
        private const val KEY_QUEST_SKILLS = "game_quest_skills"
    }
}
