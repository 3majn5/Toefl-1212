package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.data.model.StudyStatus
import com.example.data.model.VocabEntry
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryIndigo
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.VocabViewModel

private enum class ListeningScope(val label: String) {
    ALL_1212("All 1,212 Words"),
    BY_PAGE("By PDF Page (1–46)"),
    UNMASTERED("Due / Unmastered"),
    DIFFICULT("Difficult / Saved"),
    SHUFFLE_25("Quick 25 Shuffle")
}

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
    var selectedScope by remember { mutableStateOf(ListeningScope.ALL_1212) }
    var selectedPage by remember { mutableIntStateOf(1) }

    val tabs = listOf("Audio Quiz", "Dictation Spelling", "Speed & Settings")

    val activePool = remember(allEntries, selectedScope, selectedPage) {
        if (allEntries.isEmpty()) emptyList()
        else {
            val filtered = when (selectedScope) {
                ListeningScope.ALL_1212 -> allEntries.sortedBy { it.entryNumber }
                ListeningScope.BY_PAGE -> allEntries.filter { it.sourcePage == selectedPage }.sortedBy { it.entryNumber }
                ListeningScope.UNMASTERED -> allEntries.filter { it.studyStatus != StudyStatus.MASTERED }.sortedBy { it.entryNumber }
                ListeningScope.DIFFICULT -> allEntries.filter { it.isDifficult || it.isBookmarked || it.hasAnomaly }.sortedBy { it.entryNumber }
                ListeningScope.SHUFFLE_25 -> allEntries.shuffled().take(25)
            }
            filtered.ifEmpty { allEntries.sortedBy { it.entryNumber } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Listening & Pronunciation Lab (1,212 Words)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Full 1,212-word speech recognition, dictation & phonetics",
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

            if (selectedTab == 0 || selectedTab == 1) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ListeningScope.entries) { scope ->
                                FilterChip(
                                    selected = selectedScope == scope,
                                    onClick = { selectedScope = scope },
                                    label = {
                                        Text(
                                            text = scope.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedScope == scope) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (selectedScope == ListeningScope.BY_PAGE) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items((1..46).toList()) { page ->
                                    FilterChip(
                                        selected = selectedPage == page,
                                        onClick = { selectedPage = page },
                                        label = { Text("Page $page", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SecondaryIndigo,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> AudioQuizView(
                    currentPool = activePool,
                    allEntries = allEntries,
                    viewModel = viewModel
                )
                1 -> DictationSpellingView(
                    currentPool = activePool,
                    viewModel = viewModel
                )
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
    currentPool: List<VocabEntry>,
    allEntries: List<VocabEntry>,
    viewModel: VocabViewModel
) {
    if (currentPool.isEmpty()) return

    var cardIndex by remember(currentPool) { mutableIntStateOf(0) }
    val safeIndex = cardIndex.coerceIn(0, (currentPool.size - 1).coerceAtLeast(0))
    val current = currentPool.getOrNull(safeIndex) ?: return

    val distractors = remember(current, allEntries) {
        allEntries.filter { it.entryNumber != current.entryNumber }.shuffled().take(3)
    }
    val options = remember(current, distractors) {
        (distractors.map { it.primaryMeaning } + current.primaryMeaning).shuffled()
    }

    var selectedOpt by remember(safeIndex, current.entryNumber) { mutableStateOf<String?>(null) }
    var submitted by remember(safeIndex, current.entryNumber) { mutableStateOf(false) }

    LaunchedEffect(safeIndex, current.entryNumber) {
        viewModel.speak(current.headword)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entry #${current.entryNumber} • Page ${current.sourcePage} (${safeIndex + 1}/${currentPool.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { cardIndex = if (safeIndex > 0) safeIndex - 1 else currentPool.size - 1 },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Prev", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { cardIndex = (safeIndex + 1) % currentPool.size },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Next", fontSize = 11.sp)
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Listen & Identify (${safeIndex + 1}/${currentPool.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                contentDescription = "Replay word audio",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        if (current.exampleSentence.isNotBlank()) {
                            OutlinedButton(
                                onClick = { viewModel.speak(current.exampleSentence) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Hear sentence in context",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hear in Sentence", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tap speaker to replay headword (${current.grammaticalLabels.joinToString(", ")})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (submitted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "#${current.entryNumber} ${current.headword}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        current.phonetic?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (current.exampleSentence.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"${current.exampleSentence}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                        viewModel.recordSkillPractice(
                            entry = current,
                            direction = LearningDirection.LISTENING,
                            isCorrect = isTarget
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_opt_$optIdx")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }
        }

        if (submitted) {
            item {
                Button(
                    onClick = {
                        cardIndex = (safeIndex + 1) % currentPool.size
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Next Audio Word", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DictationSpellingView(
    currentPool: List<VocabEntry>,
    viewModel: VocabViewModel
) {
    if (currentPool.isEmpty()) return

    var cardIndex by remember(currentPool) { mutableIntStateOf(0) }
    val safeIndex = cardIndex.coerceIn(0, (currentPool.size - 1).coerceAtLeast(0))
    val current = currentPool.getOrNull(safeIndex) ?: return

    var inputSpelling by remember(safeIndex, current.entryNumber) { mutableStateOf("") }
    var submitted by remember(safeIndex, current.entryNumber) { mutableStateOf(false) }
    val isCorrect = inputSpelling.trim().equals(current.headword.trim(), ignoreCase = true)

    LaunchedEffect(safeIndex, current.entryNumber) {
        viewModel.speak(current.headword)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dictation #${current.entryNumber} • Page ${current.sourcePage} (${safeIndex + 1}/${currentPool.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { cardIndex = if (safeIndex > 0) safeIndex - 1 else currentPool.size - 1 },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Prev", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { cardIndex = (safeIndex + 1) % currentPool.size },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Next", fontSize = 11.sp)
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Audio Dictation Spelling (${safeIndex + 1}/${currentPool.size})",
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
                        text = "Listen carefully and spell the ${current.headword.length}-letter word (starts with '${current.headword.first().uppercaseChar()}')",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dictation_input")
            )
        }

        item {
            Button(
                onClick = {
                    submitted = true
                    viewModel.recordSkillPractice(
                        entry = current,
                        direction = LearningDirection.SPELLING,
                        isCorrect = inputSpelling.trim().equals(current.headword.trim(), ignoreCase = true)
                    )
                },
                enabled = inputSpelling.isNotBlank() && !submitted,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dictation_submit_button")
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
                        if (current.exampleSentence.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Example: \"${current.exampleSentence}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { cardIndex = (safeIndex + 1) % currentPool.size },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_audio_button")
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
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Audio runs 100% offline using your Android device's installed Text-to-Speech synthesis engine across all 1,212 headwords and academic context sentences.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
