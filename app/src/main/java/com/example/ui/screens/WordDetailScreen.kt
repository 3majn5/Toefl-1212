package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.DictionaryApiService
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import com.example.ui.components.AnomalyBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TtsButton
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.VocabViewModel
import com.example.util.AcademicVocabularyEnricher
import com.example.util.FsrsScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    entry: VocabEntry,
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    onStartPracticeForWord: (VocabEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDictLoading by viewModel.isDictionaryLoading.collectAsState()
    val dictMessage by viewModel.dictionaryMessage.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    // Enrich entry with verified collocations, word families, grammar patterns, learner traps
    val enrichedEntry = remember(entry) { AcademicVocabularyEnricher.enrich(entry) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Entry #${entry.entryNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDifficult(entry) }) {
                        Icon(
                            imageVector = if (entry.isDifficult) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Toggle Difficult",
                            tint = if (entry.isDifficult) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.toggleBookmark(entry) }) {
                        Icon(
                            imageVector = if (entry.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Toggle Bookmark",
                            tint = if (entry.isBookmarked) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Main Headword Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${entry.entryNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Source Page ${entry.sourcePage} / 46",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = entry.studyStatus)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.headword,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TtsButton(text = entry.headword, onSpeak = { viewModel.speak(it) }, size = 24)
                        }

                        if (entry.grammaticalLabels.isNotEmpty()) {
                            Text(
                                text = "Part of speech: ${entry.grammaticalLabels.joinToString()}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (enrichedEntry.registerFormality.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Register: ${enrichedEntry.registerFormality}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VerificationBadge(status = entry.userStatus)
                            if (entry.hasAnomaly) {
                                AnomalyBadge(notes = entry.anomalyNotes)
                            }
                        }
                    }
                }
            }

            // FSRS Memory & Retention Science Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FSRS Memory Status",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            val retrievability = if (entry.lastReviewedTimestamp != null && entry.fsrsStability > 0f) {
                                val elapsedDays = ((System.currentTimeMillis() - entry.lastReviewedTimestamp) / 86400000L).toInt()
                                val r = FsrsScheduler.calculateRetrievability(entry.fsrsStability, elapsedDays)
                                "${(r * 100).toInt()}% recall prob"
                            } else {
                                "Untested"
                            }
                            Text(
                                text = retrievability,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (entry.fsrsStability > 2f) SuccessGreen else AccentGold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FsrsStatBox(
                                label = "Stability (S)",
                                value = if (entry.fsrsStability > 0f) "${String.format(Locale.US, "%.1f", entry.fsrsStability)}d" else "0d",
                                subtitle = "Memory half-life",
                                modifier = Modifier.weight(1f)
                            )
                            FsrsStatBox(
                                label = "Difficulty (D)",
                                value = if (entry.fsrsDifficulty > 0f) String.format(Locale.US, "%.1f", entry.fsrsDifficulty) else "5.0",
                                subtitle = "Scale 1 - 10",
                                modifier = Modifier.weight(1f)
                            )
                            FsrsStatBox(
                                label = "Lapses",
                                value = "${entry.fsrsLapses}",
                                subtitle = "Forgotten times",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val nextReviewText = if (entry.nextReviewTimestamp > 0L) {
                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val isOverdue = System.currentTimeMillis() > entry.nextReviewTimestamp
                            val prefix = if (isOverdue) "Overdue (due " else "Scheduled for "
                            prefix + sdf.format(Date(entry.nextReviewTimestamp)) + (if (isOverdue) ")" else "")
                        } else {
                            "Due immediately upon lesson start"
                        }

                        Text(
                            text = nextReviewText,
                            fontSize = 12.sp,
                            color = if (entry.nextReviewTimestamp > 0L && System.currentTimeMillis() > entry.nextReviewTimestamp) WarningOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Original PDF Verbatim Source
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Original PDF Source (Page ${entry.sourcePage} / 46)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.rawText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Definitions & Meanings Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Definitions & Meaning",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        entry.definitions.forEachIndexed { idx, def ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${idx + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryBlue,
                                    modifier = Modifier.width(22.dp)
                                )
                                Text(
                                    text = def,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (entry.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Note: ${entry.notes}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Academic Collocations & Prepositions (Knowledge Profile)
            if (enrichedEntry.collocations.isNotEmpty() || enrichedEntry.grammarPatterns.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Academic Collocations & Prepositions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            if (enrichedEntry.grammarPatterns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Required Syntax & Preposition Rules:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                enrichedEntry.grammarPatterns.forEach { pattern ->
                                    Surface(
                                        color = PrimaryBlue.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = pattern,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            if (enrichedEntry.collocations.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "High-Yield Academic Phrases:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                enrichedEntry.collocations.forEach { col ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• $col",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        TtsButton(text = col, onSpeak = { viewModel.speak(it) }, size = 18)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Word Family & Morphological Derivatives
            if (enrichedEntry.wordFamily.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Word Family Derivatives",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                enrichedEntry.wordFamily.forEach { form ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable { viewModel.speak(form) }
                                    ) {
                                        Text(
                                            text = form,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Common Learner Traps & Usage Mistakes
            if (enrichedEntry.commonMistakes.isNotBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Common Learner Trap / TOEFL Caution",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = WarningOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = enrichedEntry.commonMistakes,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Contextual Sentence & Authentic TOEFL Usage
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contextual Usage & Examples",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (entry.exampleSentence.isNotBlank()) {
                                TtsButton(
                                    text = entry.exampleSentence,
                                    onSpeak = { viewModel.speak(it) },
                                    size = 18
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (entry.exampleSentence.isNotBlank()) {
                            Text(
                                text = "\"${entry.exampleSentence}\"",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "The researchers observed that conditions were optimal to ${entry.headword} during the experiment.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (entry.dictionaryExamples.isNotEmpty() && entry.dictionaryExamples.size > 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Additional Authentic Examples:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                            entry.dictionaryExamples.drop(1).take(3).forEach { ex ->
                                Text(
                                    text = "• $ex",
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 7-Channel Skill Mastery Breakdown for this Word
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Skill-Specific Mastery (7 Channels)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        ChannelSkillRow("Definition Recognition", entry.recognitionAttempts, entry.recognitionSuccesses)
                        ChannelSkillRow("Reverse Recall", entry.recallAttempts, entry.recallSuccesses)
                        ChannelSkillRow("Spelling & Typing", entry.spellingAttempts, entry.spellingSuccesses)
                        ChannelSkillRow("Context Gap-Fill", entry.contextAttempts, entry.contextSuccesses)
                        ChannelSkillRow("Synonym Discrimination", entry.synonymAttempts, entry.synonymSuccesses)
                        ChannelSkillRow("Sentence Production", entry.productionAttempts, entry.productionSuccesses)
                        ChannelSkillRow("Listening Recognition", entry.listeningAttempts, entry.listeningSuccesses)
                    }
                }
            }

            // Academic Dictionary & Synonyms
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Synonyms by Dictionary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            OutlinedButton(
                                onClick = { viewModel.fetchOnlineDictionary(entry) },
                                enabled = !isDictLoading,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isDictLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fetch Online", fontSize = 12.sp)
                                }
                            }
                        }

                        dictMessage?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = it, fontSize = 12.sp, color = PrimaryBlue)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (entry.dictionarySynonyms.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                entry.dictionarySynonyms.take(5).forEach { syn ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryBlue.copy(alpha = 0.12f))
                                            .clickable { viewModel.speak(syn) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = syn,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = PrimaryBlue
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Speak $syn",
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Tap 'Fetch Online' to pull live synonyms and definitions from Wiktionary & Datamuse.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "External Authoritative Dictionaries:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExternalDictChip("Cambridge") {
                                val url = DictionaryApiService.getCambridgeUrl(entry.headword)
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                            ExternalDictChip("Merriam-Webster") {
                                val url = DictionaryApiService.getMerriamWebsterUrl(entry.headword)
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                            ExternalDictChip("Oxford") {
                                val url = DictionaryApiService.getOxfordUrl(entry.headword)
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }
                }
            }

            // User Verification & Corrections Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verification & User Corrections",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Verification", tint = PrimaryBlue)
                            }
                        }

                        Text(
                            text = "Status: ${entry.userStatus.name}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!entry.userCorrection.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your Correction: ${entry.userCorrection}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = SuccessGreen
                            )
                        }

                        if (!entry.userNote.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Personal Note: ${entry.userNote}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Practice This Word Action Button
            item {
                Button(
                    onClick = { onStartPracticeForWord(entry) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_practice_word"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Practice Word Across 7 Learning Modes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        var selectedStatus by remember { mutableStateOf(entry.userStatus) }
        var correctionText by remember { mutableStateOf(entry.userCorrection ?: "") }
        var noteText by remember { mutableStateOf(entry.userNote ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Entry Verification") },
            text = {
                Column {
                    Text("Audit Status for #${entry.entryNumber} '${entry.headword}':", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    UserVerificationStatus.values().forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = st }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = st.name, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = correctionText,
                        onValueChange = { correctionText = it },
                        label = { Text("Your Correction (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Personal Learning Note (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateVerification(
                            entry.entryNumber,
                            selectedStatus,
                            correctionText.ifBlank { null },
                            noteText.ifBlank { null }
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FsrsStatBox(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChannelSkillRow(name: String, attempts: Int, successes: Int) {
    val pct = if (attempts > 0) (successes.toFloat() / attempts.toFloat()) else 0f
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = if (attempts > 0) "${(pct * 100).toInt()}% ($successes/$attempts)" else "Not attempted",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (attempts > 0) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (pct >= 0.8f) SuccessGreen else if (pct >= 0.5f) PrimaryBlue else AccentGold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ExternalDictChip(name: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(text = name, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(12.dp))
    }
}
