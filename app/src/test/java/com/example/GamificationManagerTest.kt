package com.example

import android.content.SharedPreferences
import com.example.util.Activity
import com.example.util.GamificationManager
import com.example.util.QuestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GamificationManagerTest {

    private fun today(): Long = TimeUnit.MILLISECONDS.toDays(
        System.currentTimeMillis() + TimeZone.getDefault().getOffset(System.currentTimeMillis())
    )

    @Test
    fun comboMultiplierStepsUp() {
        assertEquals(1f, GamificationManager.comboMultiplier(2))
        assertEquals(1.5f, GamificationManager.comboMultiplier(3))
        assertEquals(2f, GamificationManager.comboMultiplier(5))
        assertEquals(3f, GamificationManager.comboMultiplier(12))
    }

    @Test
    fun multiplierAndLuckyApplyToXp() {
        val game = GamificationManager(FakePrefs())
        val plain = game.award(10, null)
        val boosted = game.award(10, null, multiplier = 2f, lucky = true)
        // 10 × 2 × 2, ignoring any quest bonus that might have completed
        assertEquals(10, plain.xp - plain.completedQuests.size * GamificationManager.QUEST_XP)
        assertEquals(40, boosted.xp - boosted.completedQuests.size * GamificationManager.QUEST_XP)
        assertTrue(boosted.lucky)
    }

    @Test
    fun levelUpIsReported() {
        val game = GamificationManager(FakePrefs())
        assertNull(game.award(100, null).levelUp)
        assertEquals(2, game.award(60, null).levelUp)
    }

    @Test
    fun streakFreezeBridgesOneMissedDay() {
        val prefs = FakePrefs()
        prefs.edit().putLong("game_last_study_day", today() - 2).putInt("game_streak", 5)
            .putInt("game_streak_freezes", 1).apply()
        val game = GamificationManager(prefs)
        assertEquals(5, game.state().streakDays)
        val event = game.award(10, null)
        assertTrue(event.freezeUsed)
        assertEquals(6, game.state().streakDays)
        assertEquals(0, game.state().streakFreezes)
    }

    @Test
    fun streakResetsWithoutFreeze() {
        val prefs = FakePrefs()
        prefs.edit().putLong("game_last_study_day", today() - 2).putInt("game_streak", 5).apply()
        val game = GamificationManager(prefs)
        assertEquals(0, game.state().streakDays)
        game.award(10, null)
        assertEquals(1, game.state().streakDays)
    }

    @Test
    fun questsProgressAndCompleteOnce() {
        val game = GamificationManager(FakePrefs())
        val quests = game.todaysQuestTypes()
        assertEquals(3, quests.size)
        assertEquals(3, quests.toSet().size)
        // Push every quest type over its target
        repeat(20) {
            game.award(10, null, activity = Activity(correctAnswer = true, flashcard = true, combo = it + 1, skill = "S${it % 4}", perfectSession = true))
        }
        val state = game.state()
        assertTrue(state.quests.all { it.done })
        // Completion is only rewarded once
        assertTrue(game.award(10, null, activity = Activity(correctAnswer = true)).completedQuests.isEmpty())
        assertTrue(QuestType.values().isNotEmpty())
    }

    @Test
    fun chestOnlyOpensOnceAfterGoal() {
        val game = GamificationManager(FakePrefs(), Random(7))
        assertNull(game.openChest(null))
        game.setDailyGoal(20)
        game.award(30, null)
        assertTrue(game.state().chestAvailable)
        val opened = game.openChest(null)
        assertNotNull(opened)
        assertTrue(opened!!.first.xp > 0)
        assertFalse(game.state().chestAvailable)
        assertNull(game.openChest(null))
    }
}

/** Minimal in-memory SharedPreferences for JVM tests. */
private class FakePrefs : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map
    override fun getString(key: String?, defValue: String?) = map[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?) = (map[key] as? Set<String>)?.toMutableSet() ?: defValues
    override fun getInt(key: String?, defValue: Int) = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long) = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float) = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean) = map[key] as? Boolean ?: defValue
    override fun contains(key: String?) = map.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()
        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values?.toSet() }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun remove(key: String) = apply { removals += key }
        override fun clear() = apply { map.clear() }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() {
            removals.forEach { map.remove(it) }
            map.putAll(pending)
        }
    }
}
