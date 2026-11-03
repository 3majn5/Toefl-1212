package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ThemeMode
import com.example.data.model.TranslationLanguage
import com.example.ui.components.AnimatedEntrance
import com.example.ui.viewmodel.VocabViewModel
import com.example.util.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val language by viewModel.translationLanguage.collectAsState()
    val reminder by viewModel.reminderSettings.collectAsState()
    val game by viewModel.gameState.collectAsState()
    val soundOn by viewModel.soundEnabled.collectAsState()
    val hapticsOn by viewModel.hapticsEnabled.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        if (granted) viewModel.setReminderSettings(reminder.copy(enabled = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                AnimatedEntrance(index = 0) {
                    SettingsCard("Appearance", "Choose light, dark, or follow your phone") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.values().forEach { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(when (mode) { ThemeMode.SYSTEM -> "📱 System"; ThemeMode.LIGHT -> "☀️ Light"; ThemeMode.DARK -> "🌙 Dark" }) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                AnimatedEntrance(index = 1) {
                    SettingsCard("Translation", "Show a short meaning in your language next to each English definition") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TranslationLanguage.values().forEach { lang ->
                                FilterChip(
                                    selected = language == lang,
                                    onClick = { viewModel.setTranslationLanguage(lang) },
                                    label = { Text(if (lang == TranslationLanguage.NONE) "Off" else "${lang.nativeName} · ${lang.displayName}") }
                                )
                            }
                        }
                        if (language != TranslationLanguage.NONE) {
                            Spacer(Modifier.height(10.dp))
                            val sample = viewModel.everyEntry.value.firstOrNull { it.headword.equals("analyze", true) }
                            sample?.let {
                                Text(
                                    "Example: analyze → ${viewModel.translationFor(it, language) ?: "—"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                AnimatedEntrance(index = 2) {
                    SettingsCard("Sound & haptics", "Chimes and vibrations when you answer, combo up, or level up") {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Sound effects", modifier = Modifier.weight(1f))
                            Switch(checked = soundOn, onCheckedChange = viewModel::setSoundEnabled)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Vibration", modifier = Modifier.weight(1f))
                            Switch(checked = hapticsOn, onCheckedChange = viewModel::setHapticsEnabled)
                        }
                    }
                }
            }

            item {
                AnimatedEntrance(index = 2) {
                    SettingsCard("Daily reminder", "A notification with your due reviews, skipped on days you've already studied") {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(if (reminder.enabled) "On at ${reminder.timeLabel}" else "Off", modifier = Modifier.weight(1f))
                            Switch(
                                checked = reminder.enabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !ReminderScheduler.canPostNotifications(context)
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.setReminderSettings(reminder.copy(enabled = enabled))
                                    }
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { showTimePicker = true }) { Text("Change time (${reminder.timeLabel})") }
                        if (permissionDenied) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Notifications are blocked. Allow them in Android Settings → Apps → TOEFL 1212 to get reminders.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                AnimatedEntrance(index = 3) {
                    SettingsCard("Daily XP goal", "Earn XP from reviews and quizzes; reaching the goal unlocks a badge") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 60, 100, 150).forEach { goal ->
                                FilterChip(
                                    selected = game.dailyGoalXp == goal,
                                    onClick = { viewModel.setDailyXpGoal(goal) },
                                    label = { Text("$goal XP") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = reminder.hour, initialMinute = reminder.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Reminder time") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminderSettings(reminder.copy(hour = state.hour, minute = state.minute))
                    showTimePicker = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
