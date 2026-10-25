package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.AcademicReadingQuestion
import com.example.data.model.CompleteWordsExercise
import com.example.data.model.LearningDirection
import com.example.data.model.StudyStatus
import com.example.data.model.VocabEntry
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryIndigo
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.VocabViewModel
import com.example.util.SentenceCoachAnalyzer
import com.example.util.Toefl2026PracticeData

private enum class LabPracticeScope(val label: String) {
    ALL_1212("All 1,212 Words"),
    BY_PAGE("By PDF Page (1–46)"),
    UNMASTERED("Due / Unmastered"),
    DIFFICULT("Difficult / Saved"),
    CURATED_ETS("Curated ETS Set")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToeflPracticeScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allEntries by viewModel.allEntries.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedScope by remember { mutableStateOf(LabPracticeScope.ALL_1212) }
    var selectedPage by remember { mutableIntStateOf(1) }

    val tabs = listOf("Complete the Words", "Academic Reading", "Discussion Prompts")

    val scopedEntries = remember(allEntries, selectedScope, selectedPage) {
        when (selectedScope) {
            LabPracticeScope.ALL_1212 -> allEntries.sortedBy { it.entryNumber }
            LabPracticeScope.BY_PAGE -> allEntries.filter { it.sourcePage == selectedPage }.sortedBy { it.entryNumber }
            LabPracticeScope.UNMASTERED -> allEntries.filter { it.studyStatus != StudyStatus.MASTERED }.sortedBy { it.entryNumber }
            LabPracticeScope.DIFFICULT -> allEntries.filter { it.isDifficult || it.isBookmarked || it.hasAnomaly }.sortedBy { it.entryNumber }
            LabPracticeScope.CURATED_ETS -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TOEFL 2026 Academic Lab (1,212 Words)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Full 1,212-word ETS Cloze, Reading & Discussion Lab",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("toefl_back_button")
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
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("toefl_tab_$index")
                    )
                }
            }

            // Scope & Page 1-46 selector bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(LabPracticeScope.entries) { scope ->
                            FilterChip(
                                selected = selectedScope == scope,
                                onClick = { selectedScope = scope },
                                label = {
                                    val countSuffix = when (scope) {
                                        LabPracticeScope.ALL_1212 -> if (allEntries.isNotEmpty()) " (${allEntries.size})" else ""
                                        LabPracticeScope.BY_PAGE -> " (P.$selectedPage)"
                                        LabPracticeScope.CURATED_ETS -> " (25+)"
                                        else -> ""
                                    }
                                    Text(
                                        text = scope.label + countSuffix,
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

                    if (selectedScope == LabPracticeScope.BY_PAGE) {
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

            when (selectedTab) {
                0 -> CompleteTheWordsTab(
                    scopedEntries = scopedEntries,
                    useCurated = selectedScope == LabPracticeScope.CURATED_ETS || scopedEntries.isEmpty(),
                    viewModel = viewModel
                )
                1 -> AcademicReadingTab(
                    scopedEntries = scopedEntries,
                    allEntries = allEntries,
                    useCurated = selectedScope == LabPracticeScope.CURATED_ETS || scopedEntries.isEmpty(),
                    viewModel = viewModel
                )
                2 -> AcademicDiscussionTab(
                    scopedEntries = scopedEntries,
                    useCurated = selectedScope == LabPracticeScope.CURATED_ETS || scopedEntries.isEmpty(),
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun CompleteTheWordsTab(
    scopedEntries: List<VocabEntry>,
    useCurated: Boolean,
    viewModel: VocabViewModel
) {
    val totalCount = if (useCurated) Toefl2026PracticeData.CLOZE_EXERCISES.size else scopedEntries.size.coerceAtLeast(1)
    var currentIndex by remember(scopedEntries, useCurated) { mutableIntStateOf(0) }
    val safeIndex = currentIndex.coerceIn(0, (totalCount - 1).coerceAtLeast(0))

    val currentEntry: VocabEntry? = if (!useCurated) scopedEntries.getOrNull(safeIndex) else null
    val current: CompleteWordsExercise = remember(safeIndex, currentEntry, useCurated) {
        if (currentEntry != null) {
            Toefl2026PracticeData.buildClozeForEntry(currentEntry)
        } else {
            val list = Toefl2026PracticeData.CLOZE_EXERCISES
            list[safeIndex % list.size]
        }
    }

    var userInput by remember(safeIndex, current.id) { mutableStateOf("") }
    var showHint by remember(safeIndex, current.id) { mutableStateOf(false) }
    var submitted by remember(safeIndex, current.id) { mutableStateOf(false) }
    val isCorrect = userInput.trim().equals(current.targetWord, ignoreCase = true)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (currentEntry != null) {
                                "1,212-Word Cloze Lab: Word ${safeIndex + 1} of $totalCount (#${currentEntry.entryNumber} • Page ${currentEntry.sourcePage})"
                            } else {
                                "Curated ETS Cloze Lab: Passage ${safeIndex + 1} of $totalCount"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = {
                                currentIndex = if (safeIndex > 0) safeIndex - 1 else totalCount - 1
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Prev", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                currentIndex = (safeIndex + 1) % totalCount
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Next", fontSize = 11.sp)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = current.passageTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${safeIndex + 1}/$totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = current.textWithBlanks,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Obscured Target: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SecondaryIndigo.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = current.obscuredWord,
                                    color = SecondaryIndigo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.speak(current.targetWord) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronounce word clue",
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = userInput,
                onValueChange = { if (!submitted) userInput = it },
                label = { Text("Type complete word (${current.targetWord.length} letters)") },
                singleLine = true,
                enabled = !submitted,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cloze_input")
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showHint = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hint")
                }

                Button(
                    onClick = {
                        submitted = true
                        currentEntry?.let { entry ->
                            viewModel.recordSkillPractice(
                                entry = entry,
                                direction = LearningDirection.CONTEXT,
                                isCorrect = userInput.trim().equals(current.targetWord, ignoreCase = true)
                            )
                        }
                    },
                    enabled = userInput.isNotBlank() && !submitted,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("cloze_submit_button")
                ) {
                    Text("Submit Answer", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showHint && !submitted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hint: ${current.hint}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
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
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isCorrect) SuccessGreen else ErrorRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCorrect) {
                                    "Correct! Target word: '${current.targetWord}'"
                                } else {
                                    "Incorrect. Correct word: '${current.targetWord}'"
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) SuccessGreen else ErrorRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = current.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        currentIndex = (safeIndex + 1) % totalCount
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Next Passage", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AcademicReadingTab(
    scopedEntries: List<VocabEntry>,
    allEntries: List<VocabEntry>,
    useCurated: Boolean,
    viewModel: VocabViewModel
) {
    val totalCount = if (useCurated) Toefl2026PracticeData.READING_QUESTIONS.size else scopedEntries.size.coerceAtLeast(1)
    var qIndex by remember(scopedEntries, useCurated) { mutableIntStateOf(0) }
    val safeIndex = qIndex.coerceIn(0, (totalCount - 1).coerceAtLeast(0))

    val currentEntry: VocabEntry? = if (!useCurated) scopedEntries.getOrNull(safeIndex) else null
    val current: AcademicReadingQuestion = remember(safeIndex, currentEntry, useCurated, allEntries) {
        if (currentEntry != null) {
            Toefl2026PracticeData.buildReadingQuestionForEntry(currentEntry, allEntries)
        } else {
            val list = Toefl2026PracticeData.READING_QUESTIONS
            list[safeIndex % list.size]
        }
    }

    var selectedOpt by remember(safeIndex, current.id) { mutableStateOf<Int?>(null) }
    var submitted by remember(safeIndex, current.id) { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentEntry != null) {
                        "1,212 Reading Lab • #${currentEntry.entryNumber} (Page ${currentEntry.sourcePage})"
                    } else {
                        "Curated ETS Reading Passage"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { qIndex = if (safeIndex > 0) safeIndex - 1 else totalCount - 1 },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Prev", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { qIndex = (safeIndex + 1) % totalCount },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Next (${safeIndex + 1}/$totalCount)", fontSize = 11.sp)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOEFL Academic Passage",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        IconButton(
                            onClick = { viewModel.speak(current.targetWord) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak target word",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = current.passage,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = current.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(current.options.size) { optIndex ->
            val optText = current.options[optIndex]
            val isSelected = selectedOpt == optIndex
            val isCorrect = optIndex == current.correctIndex

            val bg = when {
                !submitted -> if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                isCorrect -> SuccessGreen.copy(alpha = 0.2f)
                isSelected -> ErrorRed.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }

            Card(
                onClick = {
                    if (!submitted) {
                        selectedOpt = optIndex
                        submitted = true
                        currentEntry?.let { entry ->
                            viewModel.recordSkillPractice(
                                entry = entry,
                                direction = LearningDirection.RECOGNITION,
                                isCorrect = optIndex == current.correctIndex
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reading_option_$optIndex")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = optText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || (submitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (submitted) {
                        if (isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Correct",
                                tint = SuccessGreen
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Wrong",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }
        }

        if (submitted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOpt == current.correctIndex) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = current.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = { qIndex = (safeIndex + 1) % totalCount },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Next Question", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AcademicDiscussionTab(
    scopedEntries: List<VocabEntry>,
    useCurated: Boolean,
    viewModel: VocabViewModel
) {
    val prompts = Toefl2026PracticeData.DISCUSSION_WRITING_PROMPTS
    val totalCount = if (useCurated) prompts.size else scopedEntries.size.coerceAtLeast(1)
    var promptIndex by remember(scopedEntries, useCurated) { mutableIntStateOf(0) }
    val safeIndex = promptIndex.coerceIn(0, (totalCount - 1).coerceAtLeast(0))

    val currentEntry: VocabEntry? = if (!useCurated) scopedEntries.getOrNull(safeIndex) else null
    val promptText = remember(safeIndex, currentEntry, useCurated) {
        if (currentEntry != null) {
            Toefl2026PracticeData.buildDiscussionPromptForEntry(currentEntry)
        } else {
            prompts[safeIndex % prompts.size]
        }
    }

    var responseText by remember(safeIndex, currentEntry?.entryNumber) { mutableStateOf("") }
    var submitted by remember(safeIndex, currentEntry?.entryNumber) { mutableStateOf(false) }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Writing for an Academic Discussion (${safeIndex + 1}/$totalCount)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(
                                onClick = { promptIndex = if (safeIndex > 0) safeIndex - 1 else totalCount - 1 },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Prev", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { promptIndex = (safeIndex + 1) % totalCount },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Next", fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = promptText,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = responseText,
                onValueChange = { responseText = it },
                label = {
                    val wordLabel = currentEntry?.let { "using '${it.headword}' (#${it.entryNumber})" } ?: "your academic discussion response..."
                    Text("Draft $wordLabel")
                },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discussion_input")
            )
        }

        item {
            Button(
                onClick = {
                    submitted = true
                    currentEntry?.let { entry ->
                        val eval = SentenceCoachAnalyzer.evaluate(responseText, entry)
                        viewModel.recordSkillPractice(
                            entry = entry,
                            direction = LearningDirection.PRODUCTION,
                            isCorrect = eval.isTargetWordPresent && eval.estimatedAcademicScore >= 3
                        )
                    }
                },
                enabled = responseText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("discussion_submit_button")
            ) {
                Text("Analyze Discussion Post", fontWeight = FontWeight.Bold)
            }
        }

        if (submitted) {
            val wordsCount = responseText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val eval = currentEntry?.let { SentenceCoachAnalyzer.evaluate(responseText, it) }
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Post Feedback & Metrics" + (eval?.let { " • Score: ${it.estimatedAcademicScore}/5" } ?: ""),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Word Count: $wordsCount words (Recommended for TOEFL discussion: 100-150 words).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (eval != null) {
                            Text(
                                text = "• Target Word Usage ('${eval.targetWord}'): ${if (eval.isTargetWordPresent) "Detected in context" else "Not detected — try including '${eval.targetWord}' explicitly"}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (eval.isTargetWordPresent) SuccessGreen else WarningOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                            eval.strengths.forEach { str ->
                                Text(text = "• Strength: $str", style = MaterialTheme.typography.bodySmall)
                            }
                            eval.suggestions.forEach { sug ->
                                Text(text = "• Tip: $sug", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Text(
                                text = "• Syntactic Complexity: Appropriate sentence length with clear stance.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Academic Register: Clear academic vocabulary integration.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { promptIndex = (safeIndex + 1) % totalCount },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Next Discussion Prompt")
                }
            }
        }
    }
}
