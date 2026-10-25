package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.data.model.VocabEntry
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.VocabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningLabScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allEntries by viewModel.allEntries.collectAsState()
    val ttsSpeed by viewModel.ttsSpeed.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isUkVoice by remember { mutableStateOf(false) }

    val tabs = listOf("Audio Quiz", "Dictation Spelling", "Speed & Settings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Listening & Pronunciation Lab",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Speech audio recognition and phonetics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("listening_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("listening_tab_$index")
                    )
                }
            }

            when (selectedTab) {
                0 -> AudioQuizView(entries = allEntries, viewModel = viewModel)
                1 -> DictationSpellingView(entries = allEntries, viewModel = viewModel)
                2 -> AudioSettingsView(
                    ttsSpeed = ttsSpeed,
                    isUkVoice = isUkVoice,
                    onSpeedChange = { viewModel.setTtsSpeed(it) },
                    onVoiceChange = {
                        isUkVoice = it
                        viewModel.setTtsVoice(it)
                    },
                    onTestAudio = {
                        viewModel.speak("Welcome to the TOEFL 1212 listening and pronunciation laboratory.")
                    }
                )
            }
        }
    }
}

@Composable
private fun AudioQuizView(
    entries: List<VocabEntry>,
    viewModel: VocabViewModel
) {
    if (entries.isEmpty()) return

    var currentPool by remember(entries) {
        mutableStateOf(entries.shuffled().take(10))
    }
    var cardIndex by remember { mutableIntStateOf(0) }
    val current = currentPool.getOrNull(cardIndex) ?: return

    val distractors = remember(current) {
        entries.filter { it.entryNumber != current.entryNumber }.shuffled().take(3)
    }
    val options = remember(current, distractors) {
        (distractors.map { it.primaryMeaning } + current.primaryMeaning).shuffled()
    }

    var selectedOpt by remember(cardIndex) { mutableStateOf<String?>(null) }
    var submitted by remember(cardIndex) { mutableStateOf(false) }

    LaunchedEffect(cardIndex) {
        viewModel.speak(current.headword)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Listen & Identify (${cardIndex + 1}/${currentPool.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IconButton(
                        onClick = { viewModel.speak(current.headword) },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f))
                            .testTag("audio_quiz_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Replay audio",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tap to replay audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (submitted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = current.headword,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        current.phonetic?.let {
                            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        items(options.size) { optIdx ->
            val opt = options[optIdx]
            val isSelected = selectedOpt == opt
            val isTarget = opt == current.primaryMeaning

            val bg = when {
                !submitted -> if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                isTarget -> SuccessGreen.copy(alpha = 0.2f)
                isSelected -> ErrorRed.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }

            Card(
                onClick = {
                    if (!submitted) {
                        selectedOpt = opt
                        submitted = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                modifier = Modifier.fillMaxWidth().testTag("audio_opt_$optIdx")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || (submitted && isTarget)) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (submitted) {
                        if (isTarget) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        } else if (isSelected) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = ErrorRed)
                        }
                    }
                }
            }
        }

        if (submitted) {
            item {
                Button(
                    onClick = {
                        cardIndex = (cardIndex + 1) % currentPool.size
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Next Audio Word", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DictationSpellingView(
    entries: List<VocabEntry>,
    viewModel: VocabViewModel
) {
    if (entries.isEmpty()) return

    var currentPool by remember(entries) {
        mutableStateOf(entries.shuffled().take(10))
    }
    var cardIndex by remember { mutableIntStateOf(0) }
    val current = currentPool.getOrNull(cardIndex) ?: return

    var inputSpelling by remember(cardIndex) { mutableStateOf("") }
    var submitted by remember(cardIndex) { mutableStateOf(false) }
    val isCorrect = inputSpelling.trim().equals(current.headword.trim(), ignoreCase = true)

    LaunchedEffect(cardIndex) {
        viewModel.speak(current.headword)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Audio Dictation Spelling (${cardIndex + 1}/${currentPool.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IconButton(
                        onClick = { viewModel.speak(current.headword) },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentGold.copy(alpha = 0.15f))
                            .testTag("dictation_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = AccentGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Listen carefully and spell the word below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Meaning clue: \"${current.primaryMeaning}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = inputSpelling,
                onValueChange = { if (!submitted) inputSpelling = it },
                label = { Text("Spell the spoken word") },
                singleLine = true,
                enabled = !submitted,
                modifier = Modifier.fillMaxWidth().testTag("dictation_input")
            )
        }

        item {
            Button(
                onClick = { submitted = true },
                enabled = inputSpelling.isNotBlank() && !submitted,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dictation_submit_button")
            ) {
                Text("Check Spelling", fontWeight = FontWeight.Bold)
            }
        }

        if (submitted) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCorrect) SuccessGreen else ErrorRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCorrect) "Correct Spelling: '${current.headword}'" else "Correct: '${current.headword}'",
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) SuccessGreen else ErrorRed
                            )
                        }
                        if (current.phonetic != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Phonetic: ${current.phonetic}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { cardIndex = (cardIndex + 1) % currentPool.size },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Next Dictation Word", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AudioSettingsView(
    ttsSpeed: Float,
    isUkVoice: Boolean,
    onSpeedChange: (Float) -> Unit,
    onVoiceChange: (Boolean) -> Unit,
    onTestAudio: () -> Unit
) {
    val speeds = listOf(0.75f, 1.0f, 1.25f)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Playback Speed Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        speeds.forEach { rate ->
                            FilterChip(
                                selected = ttsSpeed == rate,
                                onClick = { onSpeedChange(rate) },
                                label = { Text("${rate}x") },
                                modifier = Modifier.testTag("speed_chip_$rate")
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Accent Variety",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !isUkVoice,
                            onClick = { onVoiceChange(false) },
                            label = { Text("General American (US)") }
                        )
                        FilterChip(
                            selected = isUkVoice,
                            onClick = { onVoiceChange(true) },
                            label = { Text("British Received (UK)") }
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onTestAudio,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("test_audio_button")
            ) {
                Icon(imageVector = Icons.Default.Hearing, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Audio Output", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Audio runs 100% offline using your Android device's installed Text-to-Speech synthesis engine. For highest naturalness, ensure Google Speech Services offline voice data is downloaded in your device system settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
