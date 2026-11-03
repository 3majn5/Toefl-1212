package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.ui.components.AnimatedEntrance
import com.example.ui.components.TtsButton
import com.example.ui.components.bouncyClickable
import com.example.ui.theme.FreshMint
import com.example.ui.theme.SoftRose
import com.example.ui.theme.WarmCoral
import com.example.ui.viewmodel.VocabViewModel
import com.example.util.MistakeNotebook
import java.util.concurrent.TimeUnit

/**
 * Every word missed in any mode, newest first. "Drill my mistakes" quizzes up to 10 of them;
 * two correct answers in a row clears a word from the notebook.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MistakeNotebookScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    onStartDrill: () -> Unit,
    onWordSelected: (com.example.data.model.VocabEntry) -> Unit
) {
    val mistakes by viewModel.mistakes.collectAsState()
    val everyEntry by viewModel.everyEntry.collectAsState()
    val byNumber = remember(everyEntry.size) { everyEntry.associateBy { it.entryNumber } }
    val rows = mistakes.sortedByDescending { it.lastWrongAt }.mapNotNull { r -> byNumber[r.entryNumber]?.let { r to it } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mistake Notebook", fontWeight = FontWeight.Bold)
                        Text(
                            "${rows.size} word${if (rows.size == 1) "" else "s"} to fix",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (rows.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📒", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No mistakes yet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Any word you miss in a quiz, lab, flashcard or pronunciation check lands here, " +
                            "so you can drill exactly what you find hard.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                AnimatedEntrance(index = 0) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmCoral.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Turn mistakes into strengths", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Answer a word correctly ${MistakeNotebook.CLEAR_AFTER} times in a row and it leaves the notebook.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { if (viewModel.startMistakeDrill()) onStartDrill() },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmCoral),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Drill my mistakes (${minOf(rows.size, 10)})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            items(rows, key = { it.first.entryNumber }) { (record, entry) ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().animateItem().bouncyClickable { onWordSelected(entry) }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(42.dp).background(SoftRose.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                        ) {
                            Text("×${record.wrongCount}", color = SoftRose, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.headword, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (record.correctStreak > 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Text("✓ ${record.correctStreak}/${MistakeNotebook.CLEAR_AFTER}", color = FreshMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                entry.primaryMeaning,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                record.channels.forEach { ch ->
                                    Text(
                                        channelLabel(ch),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(ago(record.lastWrongAt), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        TtsButton(text = entry.answerForm, onSpeak = { viewModel.speak(it) })
                        IconButton(onClick = { viewModel.removeMistake(entry.entryNumber) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove from notebook", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun channelLabel(channel: String): String = when (channel) {
    "FLASHCARD" -> "Flashcards"
    "PRONUNCIATION" -> "Pronunciation"
    else -> runCatching { LearningDirection.valueOf(channel).displayName }.getOrDefault(channel)
}

private fun ago(timestamp: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamp)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
    }
}
