package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Navy800
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.VocabFilter
import com.example.ui.viewmodel.VocabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VocabViewModel,
    onNavigateToBrowse: () -> Unit,
    onNavigateToAudit: () -> Unit,
    onNavigateToPractice: () -> Unit,
    onStartDailySession: () -> Unit,
    onNavigateToToeflPractice: () -> Unit = {},
    onNavigateToListeningLab: () -> Unit = {},
    onNavigateToStudyPlanner: () -> Unit = {},
    onNavigateToSentenceCoach: () -> Unit = {},
    onNavigateToConfusables: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stats by viewModel.studyStats.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    val studyPlan by viewModel.studyPlan.collectAsState()
    val skillSummaries by viewModel.skillSummaries.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TOEFL 1212",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Intelligent Spaced Repetition Mastery",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToStudyPlanner,
                        modifier = Modifier.testTag("action_planner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Study Planner",
                            tint = PrimaryBlue
                        )
                    }
                    IconButton(
                        onClick = onNavigateToBackupRestore,
                        modifier = Modifier.testTag("action_backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = "Backup and Export",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAudit,
                        modifier = Modifier.testTag("action_audit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = "PDF Extraction Audit",
                            tint = MaterialTheme.colorScheme.primary
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
        if (isInitializing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading 1,212 entries from 46 PDF pages...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Exam Countdown & Streak Banner
                studyPlan.targetExamTimestamp?.let {
                    val days = studyPlan.daysUntilExam ?: 0
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToStudyPlanner() }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(PrimaryBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "$days Days Until TOEFL Exam",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = "Daily Target: ${studyPlan.dailyNewWords} words (${studyPlan.dailyMinutes} mins) • ${studyPlan.streakDays}d streak",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = PrimaryBlue
                                )
                            }
                        }
                    }
                }

                // Official PDF Extraction Integrity Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(PrimaryBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "PDF Extraction Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Official 46-Page PDF Extracted",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "All 1,212 numbered entries intact with 27 documented source anomalies.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            IconButton(onClick = onNavigateToAudit) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View Audit",
                                    tint = PrimaryBlue
                                )
                            }
                        }
                    }
                }

                // Main Hero: Today's FSRS Study Session
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
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
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Today's FSRS Session",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    val estMinutes = ((stats.dueCount * 0.7f) + (dailyGoal * 1.5f)).toInt().coerceAtLeast(5)
                                    Text(
                                        text = "${stats.dueCount} due reviews + $dailyGoal new words (~$estMinutes mins)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { showGoalDialog = true },
                                    modifier = Modifier.testTag("button_configure_goal")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Configure Daily Lesson Size",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatPill(
                                    title = "Due Reviews",
                                    count = stats.dueCount,
                                    color = if (stats.dueCount > 0) WarningOrange else SuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    title = "New Words",
                                    count = stats.newCount,
                                    color = PrimaryBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    title = "Mastered",
                                    count = stats.masteredCount,
                                    color = SuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onStartDailySession,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("button_start_session"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (stats.dueCount > 0) "Review ${stats.dueCount} Due Words Now" else "Start Daily Lesson ($dailyGoal New Words)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 7 Practice Channels Skill Health Overview
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
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "7-Skill Mastery Health",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = "Practice Modes",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    modifier = Modifier.clickable { onNavigateToPractice() }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Compact rows for 7 channels
                            skillSummaries.forEach { summary ->
                                Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = summary.direction.displayName, fontSize = 12.sp)
                                        Text(
                                            text = if (summary.attempts > 0) "${(summary.accuracy * 100).toInt()}% (${summary.successes}/${summary.attempts})" else "0%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (summary.accuracy >= 0.75f) SuccessGreen else PrimaryBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { summary.accuracy },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (summary.accuracy >= 0.8f) SuccessGreen else if (summary.accuracy >= 0.5f) PrimaryBlue else AccentGold,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Overall Completion Progress
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
                                    text = "Vocabulary Progress",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${stats.overallCompletionPct.toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = PrimaryBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (stats.overallCompletionPct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = PrimaryBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${stats.learningCount + stats.reviewingCount + stats.masteredCount} of 1,212 introduced",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${stats.masteredCount} mastered",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }

                // Academic Labs & Advanced Practice Section
                item {
                    Text(
                        text = "Academic Labs & Tools",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                // Row: TOEFL 2026 Lab & Listening Lab
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "TOEFL 2026 Lab",
                            subtitle = "Cloze words, reading & discussion",
                            icon = Icons.Default.AutoAwesome,
                            color = PrimaryBlue,
                            onClick = onNavigateToToeflPractice,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_toefl_lab")
                        )

                        ActionCard(
                            title = "Listening & Pronounce",
                            subtitle = "Audio quiz, dictation & speed",
                            icon = Icons.Default.Hearing,
                            color = AccentGold,
                            onClick = onNavigateToListeningLab,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_listening_lab")
                        )
                    }
                }

                // Row: Confusable Words & Sentence Coach
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Confusable Words",
                            subtitle = "High-frequency TOEFL traps",
                            icon = Icons.Default.CompareArrows,
                            color = WarningOrange,
                            onClick = onNavigateToConfusables,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_confusables")
                        )

                        ActionCard(
                            title = "Sentence Coach",
                            subtitle = "Writing quality & feedback",
                            icon = Icons.Default.Edit,
                            color = SuccessGreen,
                            onClick = onNavigateToSentenceCoach,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_sentence_coach")
                        )
                    }
                }

                // Row: Browse & 7 Practice Modes
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Browse 1,212 Words",
                            subtitle = "By #number, A-Z, or page",
                            icon = Icons.Default.CollectionsBookmark,
                            color = PrimaryBlue,
                            onClick = {
                                viewModel.setFilter(VocabFilter.ALL)
                                onNavigateToBrowse()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_browse_words")
                        )

                        ActionCard(
                            title = "7 Learning Modes",
                            subtitle = "Recall, spelling, listening",
                            icon = Icons.Default.School,
                            color = AccentGold,
                            onClick = onNavigateToPractice,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_practice_modes")
                        )
                    }
                }

                // Row: Study Planner & Backup/Restore
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Study Planner",
                            subtitle = "Exam forecast & retention goal",
                            icon = Icons.Default.CalendarToday,
                            color = PrimaryBlue,
                            onClick = onNavigateToStudyPlanner,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_study_planner")
                        )

                        ActionCard(
                            title = "Backup & Export",
                            subtitle = "JSON & CSV data portability",
                            icon = Icons.Default.Backup,
                            color = SuccessGreen,
                            onClick = onNavigateToBackupRestore,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_backup_export")
                        )
                    }
                }

                // Difficult words & Source anomalies
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Difficult Words",
                            subtitle = "${stats.difficultCount} flagged words",
                            icon = Icons.Default.Star,
                            color = WarningOrange,
                            onClick = {
                                viewModel.setFilter(VocabFilter.DIFFICULT)
                                onNavigateToBrowse()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_difficult_words")
                        )

                        ActionCard(
                            title = "Source Anomalies",
                            subtitle = "27 duplicate/OCR notes",
                            icon = Icons.Default.Warning,
                            color = ErrorRed,
                            onClick = onNavigateToAudit,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_audit_matrix")
                        )
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Configure Daily Lesson Size") },
            text = {
                Column {
                    Text("Select how many new words you want to introduce in each daily lesson session:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20).forEach { count ->
                            FilterChip(
                                selected = dailyGoal == count,
                                onClick = {
                                    viewModel.setDailyGoal(count)
                                },
                                label = { Text("$count words") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showGoalDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun StatPill(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
