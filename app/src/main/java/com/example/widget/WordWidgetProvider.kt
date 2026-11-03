package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Home-screen widget: word of the day, current streak and reviews due.
 *
 * The app writes a snapshot (streak, due count and a rotation of upcoming words) whenever study
 * state changes; the widget picks today's word from that rotation, so it still changes daily
 * even if the app isn't opened.
 */
class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, buildViews(context)) }
    }

    companion object {
        private const val PREFS = "toefl_1212_widget"
        private const val KEY_SNAPSHOT = "snapshot"

        private fun today(): Long = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() + TimeZone.getDefault().getOffset(System.currentTimeMillis())
        )

        /** Saves the latest study state and redraws every placed widget. */
        fun saveSnapshot(context: Context, streak: Int, studiedToday: Boolean, due: Int, words: List<Pair<String, String>>) {
            val json = JSONObject()
                .put("streak", streak)
                .put("studiedDay", if (studiedToday) today() else -1L)
                .put("due", due)
                .put("startDay", today())
                .put("words", JSONArray().apply { words.forEach { (w, m) -> put(JSONObject().put("w", w).put("m", m)) } })
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_SNAPSHOT, json.toString()).apply()
            refreshAll(context)
        }

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val views = buildViews(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun buildViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_word)
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SNAPSHOT, null)
            runCatching {
                val json = JSONObject(raw ?: return@runCatching)
                val words = json.getJSONArray("words")
                if (words.length() > 0) {
                    val offset = (today() - json.getLong("startDay")).toInt().coerceAtLeast(0)
                    val word = words.getJSONObject(offset % words.length())
                    views.setTextViewText(R.id.widget_word, word.getString("w"))
                    views.setTextViewText(R.id.widget_meaning, word.getString("m"))
                }
                val streak = json.getInt("streak")
                views.setTextViewText(R.id.widget_streak, "🔥 $streak")
                val due = json.getInt("due")
                val studiedToday = json.optLong("studiedDay", -1L) == today()
                views.setTextViewText(
                    R.id.widget_due,
                    when {
                        due > 0 -> "$due due · tap to review"
                        studiedToday -> "✅ Done for today"
                        else -> "Tap to keep your streak"
                    }
                )
            }
            val open = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, open)
            return views
        }
    }
}
