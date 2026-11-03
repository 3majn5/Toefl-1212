package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Daily study reminder settings, persisted alongside the other study preferences. */
data class ReminderSettings(val enabled: Boolean = false, val hour: Int = 19, val minute: Int = 0) {
    val timeLabel: String get() = "%02d:%02d".format(hour, minute)
}

object ReminderScheduler {
    private const val CHANNEL_ID = "daily_study_reminder"
    private const val WORK_NAME = "daily_study_reminder"
    private const val NOTIFICATION_ID = 1212
    private const val PREFS = "toefl_1212_study_prefs"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Daily study reminder", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "A daily nudge to review your due words and keep your streak"
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    fun load(context: Context): ReminderSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ReminderSettings(
            enabled = prefs.getBoolean("reminder_enabled", false),
            hour = prefs.getInt("reminder_hour", 19),
            minute = prefs.getInt("reminder_minute", 0)
        )
    }

    fun save(context: Context, settings: ReminderSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("reminder_enabled", settings.enabled)
            .putInt("reminder_hour", settings.hour)
            .putInt("reminder_minute", settings.minute)
            .apply()
        apply(context, settings)
    }

    /** Schedules (or cancels) a once-a-day reminder starting at the chosen time. */
    fun apply(context: Context, settings: ReminderSettings) {
        val workManager = WorkManager.getInstance(context)
        if (!settings.enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, settings.hour)
            set(Calendar.MINUTE, settings.minute)
            set(Calendar.SECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(next.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    internal fun show(context: Context, title: String, text: String) {
        if (!canPostNotifications(context)) return
        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification) }
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("toefl_1212_study_prefs", Context.MODE_PRIVATE)
        val state = GamificationManager(prefs).state()
        if (state.studiedToday) return Result.success()

        val due = runCatching {
            AppDatabase.getInstance(applicationContext).vocabDao().getDueEntriesDirect(System.currentTimeMillis()).size
        }.getOrDefault(0)
        val title = if (state.streakDays > 0) "Keep your ${state.streakDays}-day streak alive 🔥" else "Time for today's vocabulary 📚"
        val text = when {
            due > 0 -> "$due words are due for review. A few minutes now keeps them in long-term memory."
            else -> "No reviews due — learn a few new words to stay on track."
        }
        ReminderScheduler.show(applicationContext, title, text)
        return Result.success()
    }
}
