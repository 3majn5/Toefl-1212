package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.data.model.StudyStatus
import com.example.data.model.VocabEntry
import com.example.data.model.WritingEvaluationResult
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryIndigo
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.VocabViewModel
import com.example.util.AcademicVocabularyEnricher
import com.example.util.SentenceCoachAnalyzer

private enum class CoachWordScope(val label: String) {
    ALL_1212("All 1,212 Words"),
    BY_PAGE("By Page (1–46)"),
    UNMASTERED("Due / Unmastered"),
    DIFFICULT("Difficult / Saved")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentenceCoachScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.allEntries.collectAsState()
    val selectedEntryFromVm by viewModel.selectedEntry.collectAsState()

    var selectedScope by remember { mutableStateOf(CoachWordScope.ALL_1212) }
    var selectedPage by remember { mutableIntStateOf(1) }
    var wordSearchQuery by remember { mutableStateOf("") }

    val candidateWords = remember(entries, selectedScope, selectedPage, wordSearchQuery) {
        if (entries.isEmpty()) emptyList()
        else {
            val base = when (selectedScope) {
                CoachWordScope.ALL_1212 -> entries.sortedBy { it.entryNumber }
                CoachWordScope.BY_PAGE -> entries.filter { it.sourcePage == selectedPage }.sortedBy { it.entryNumber }
                CoachWordScope.UNMASTERED -> entries.filter { it.studyStatus != StudyStatus.MASTERED }.sortedBy { it.entryNumber }
                CoachWordScope.DIFFICULT -> entries.filter { it.isDifficult || it.isBookmarked || it.hasAnomaly }.sortedBy { it.entryNumber }
            }.ifEmpty { entries.sortedBy { it.entryNumber } }

            if (wordSearchQuery.isBlank()) base
            else {
                val q = wordSearchQuery.trim().removePrefix("#")
                base.filter {
                    it.headword.contains(q, ignoreCase = true) ||
                            it.entryNumber.toString() == q ||
                            it.primaryMeaning.contains(q, ignoreCase = true)
                }.ifEmpty { base }
            }
        }
    }

    var currentTargetEntry by remember(candidateWords, selectedEntryFromVm) {
        mutableStateOf(selectedEntryFromVm ?: candidateWords.firstOrNull())
    }

    var userInputText by remember { mutableStateOf("") }
    var evaluationResult by remember { mutableStateOf<WritingEvaluationResult?>(null) }
    var practiceRecorded by remember { mutableStateOf(false) }

    val enrichedTarget = remember(currentTargetEntry) {
        currentTargetEntry?.let { AcademicVocabularyEnricher.enrich(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sentence Writing Coach (1,212 Words)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Authentic Academic Sentence Production for #1–#1212",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            // 1,212 Scope & Search Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CoachWordScope.entries) { scope ->
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

                    if (selectedScope == CoachWordScope.BY_PAGE) {
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

                    OutlinedTextField(
                        value = wordSearchQuery,
                        onValueChange = { wordSearchQuery = it },
                        label = { Text("Jump to #1–#1212 or search headword...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Select Target Word (${candidateWords.size} available):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(candidateWords.take(60), key = { it.entryNumber }) { entry ->
                            FilterChip(
                                selected = currentTargetEntry?.entryNumber == entry.entryNumber,
                                onClick = {
                                    currentTargetEntry = entry
                                    evaluationResult = null
                                    practiceRecorded = false
                                },
                                label = {
                                    Text(
                                        text = "#${entry.entryNumber} ${entry.headword}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Word Guidance Card with Prev / Next across all 1,212 entries
            enrichedTarget?.let { entry ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${entry.entryNumber} ${entry.headword}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.speak(entry.headword) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Speak",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            if (candidateWords.isNotEmpty()) {
                                                val idx = candidateWords.indexOfFirst { it.entryNumber == entry.entryNumber }
                                                val prevIdx = if (idx > 0) idx - 1 else candidateWords.size - 1
                                                currentTargetEntry = candidateWords[prevIdx]
                                                evaluationResult = null
                                                practiceRecorded = false
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Prev", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            if (candidateWords.isNotEmpty()) {
                                                val idx = candidateWords.indexOfFirst { it.entryNumber == entry.entryNumber }
                                                val nextIdx = if (idx >= 0) (idx + 1) % candidateWords.size else 0
                                                currentTargetEntry = candidateWords[nextIdx]
                                                evaluationResult = null
                                                practiceRecorded = false
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Next", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = PrimaryBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Page ${entry.sourcePage} • ${entry.grammaticalLabels.joinToString(", ")}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    color = SecondaryIndigo.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = entry.registerFormality.ifBlank { "Academic" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SecondaryIndigo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = entry.primaryMeaning.ifBlank { entry.definitions.firstOrNull() ?: "" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (entry.collocations.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Collocations: ${entry.collocations.take(3).joinToString(" • ")}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (entry.grammarPatterns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Rule: ${entry.grammarPatterns.first()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AccentGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Text Field Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Compose an Academic Sentence:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Write a complete formal sentence using '${currentTargetEntry?.headword ?: "the word"}' in a university context.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = userInputText,
                            onValueChange = {
                                userInputText = it
                                practiceRecorded = false
                            },
                            placeholder = { Text("e.g., The researcher could not attribute the anomalies to faulty laboratory equipment.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("input_sentence_coach"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val wordCount = userInputText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$wordCount words (target: 8-15 words)",
                                fontSize = 11.sp,
                                color = if (wordCount >= 8) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                currentTargetEntry?.let { entry ->
                                    val res = SentenceCoachAnalyzer.evaluate(userInputText, entry)
                                    evaluationResult = res
                                }
                            },
                            enabled = userInputText.trim().isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("button_evaluate_sentence"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze Academic Quality", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Evaluation Results
            evaluationResult?.let { res ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Coach Assessment",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(res.estimatedAcademicScore) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AccentGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${res.estimatedAcademicScore}/5",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AccentGold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Strengths
                            if (res.strengths.isNotEmpty()) {
                                Text(
                                    text = "Strengths:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.strengths.forEach { str ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = str, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            // Suggestions
                            if (res.suggestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Recommendations for Improvement:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningOrange
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.suggestions.forEach { sug ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = WarningOrange,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = sug, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            // Model Sentence Card
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Academic Model Sentence:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                        IconButton(
                                            onClick = { viewModel.speak(res.modelSentence) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Speak Model Sentence",
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${res.modelSentence}\"",
                                        fontSize = 13.sp,
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 19.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Record to Skill Tracking
                            Spacer(modifier = Modifier.height(16.dp))
                            val isSuccessful = res.estimatedAcademicScore >= 3 && res.isTargetWordPresent
                            Button(
                                onClick = {
                                    currentTargetEntry?.let { entry ->
                                        viewModel.recordSkillPractice(
                                            entry = entry,
                                            direction = LearningDirection.PRODUCTION,
                                            isCorrect = isSuccessful
                                        )
                                        practiceRecorded = true
                                    }
                                },
                                enabled = !practiceRecorded,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("button_record_production_skill"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (practiceRecorded) SuccessGreen else PrimaryBlue
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (practiceRecorded) Icons.Default.CheckCircle else Icons.Default.Edit,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (practiceRecorded) "Recorded into Skill Progress!" else "Save Production Attempt",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
