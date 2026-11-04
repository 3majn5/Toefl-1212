package com.example.ui.screens

import com.example.ui.theme.SoftRose
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.WarmCoral
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Lavender
import com.example.ui.components.LiquidBlobs
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateFloat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.graphicsLayer
import com.example.data.model.CollectionScope
import com.example.data.model.ThemeMode
import com.example.ui.theme.SunnyAmber
import com.example.util.GamificationState
import com.example.data.model.LearningDirection
import com.example.ui.components.AnimatedCounter
import com.example.ui.components.AnimatedEntrance
import com.example.ui.components.AnimatedProgressBar
import com.example.ui.components.CollectionSwitcher
import com.example.ui.components.PulsingDot
import com.example.ui.components.bouncyClickable
import com.example.ui.components.pressScale
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandIndigoDark
import com.example.ui.theme.FreshMint
import com.example.ui.theme.HeroGradient
import com.example.ui.theme.WarmCoralLight
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
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSpeaking: () -> Unit = {},
    onNavigateToMistakes: () -> Unit = {},
    onNavigateToChallenge: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stats by viewModel.studyStats.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    val studyPlan by viewModel.studyPlan.collectAsState()
    val skillSummaries by viewModel.skillSummaries.collectAsState()
    val collectionScope by viewModel.collectionScope.collectAsState()
    val collectionCounts by viewModel.collectionCounts.collectAsState()
    val scopedEntries by viewModel.allEntries.collectAsState()
    val anomalyCount = scopedEntries.count { it.hasAnomaly }
    val game by viewModel.gameState.collectAsState()
    val mistakes by viewModel.mistakes.collectAsState()
    val bestChallenge by viewModel.bestChallenge.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.refreshGameState() }

    var showGoalDialog by remember { mutableStateOf(false) }
    var openedChest by remember { mutableStateOf<com.example.util.ChestReward?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TOEFL 35",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Studying: ${collectionScope.displayName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK) }) {
                        androidx.compose.animation.Crossfade(targetState = isDark, label = "themeIcon") { dark ->
                            Icon(
                                imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (dark) "Switch to light mode" else "Switch to dark mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("action_settings_button")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
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
                    Text("Preparing your TOEFL 1212 & AWL 570 word lists...")
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
                item {
                    AnimatedEntrance(index = 0) {
                        CollectionSwitcher(
                            selected = collectionScope,
                            counts = collectionCounts,
                            onSelect = viewModel::setCollectionScope
                        )
                    }
                }

                // Streak, level and daily goal
                item {
                    AnimatedEntrance(index = 1) {
                        StreakCard(game = game, onClick = onNavigateToProgress)
                    }
                }

                // Mystery chest appears once today's goal is reached
                if (game.chestAvailable || openedChest != null) {
                    item(key = "chest") {
                        MysteryChestCard(
                            reward = openedChest,
                            onOpen = { openedChest = viewModel.openChest() },
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                // Daily quests
                item {
                    AnimatedEntrance(index = 2) {
                        DailyQuestsCard(game = game)
                    }
                }

                // 60-second challenge banner
                item {
                    AnimatedEntrance(index = 3) {
                        ChallengeBanner(best = bestChallenge, onClick = onNavigateToChallenge)
                    }
                }

                // Speaking & mistakes
                item {
                    AnimatedEntrance(index = 3) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ActionCard(
                                title = "Speaking Studio",
                                subtitle = "TOEFL tasks & pronunciation",
                                icon = Icons.Default.Mic,
                                color = WarmCoral,
                                onClick = onNavigateToSpeaking,
                                modifier = Modifier.weight(1f).testTag("card_speaking")
                            )
                            ActionCard(
                                title = "Mistake Notebook",
                                subtitle = if (mistakes.isEmpty()) "Words you missed" else "${mistakes.size} word${if (mistakes.size == 1) "" else "s"} to fix",
                                icon = Icons.Default.EditNote,
                                color = SoftRose,
                                onClick = onNavigateToMistakes,
                                modifier = Modifier.weight(1f).testTag("card_mistakes")
                            )
                        }
                    }
                }

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
                                            text = "Daily Target: ${studyPlan.dailyNewWords} words (${studyPlan.dailyMinutes} mins) • ${game.streakDays}d streak",
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
                  AnimatedEntrance(index = 3) {
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
                                    text = when (collectionScope) {
                                        CollectionScope.TOEFL -> "Official 46-Page TOEFL PDF"
                                        CollectionScope.AWL -> "Academic Word List (Coxhead)"
                                        CollectionScope.ALL -> "TOEFL 1212 + Academic Word List"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = when (collectionScope) {
                                        CollectionScope.TOEFL -> "All ${stats.totalEntries} numbered entries intact with $anomalyCount documented source anomalies."
                                        CollectionScope.AWL -> "${stats.totalEntries} headwords used most across university texts, each with real example sentences."
                                        CollectionScope.ALL -> "${stats.totalEntries} words in one study pool. Shared words keep separate progress per list."
                                    },
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
                }

                // Main Hero: Today's FSRS Study Session
                item {
                  AnimatedEntrance(index = 2) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandIndigo),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.background(HeroGradient)) {
                            LiquidBlobs(colors = listOf(WarmCoral, SkyBlue, Lavender), modifier = Modifier.matchParentSize())
                            Column(modifier = Modifier.padding(22.dp)) {
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
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Today's Session",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = Color.White
                                        )
                                        if (stats.dueCount > 0) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            PulsingDot(color = WarmCoralLight)
                                        }
                                    }
                                    val estMinutes = ((stats.dueCount * 0.7f) + (dailyGoal * 1.5f)).toInt().coerceAtLeast(5)
                                    Text(
                                        text = "${stats.dueCount} due reviews + $dailyGoal new words (~$estMinutes mins)",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                IconButton(
                                    onClick = { showGoalDialog = true },
                                    modifier = Modifier.testTag("button_configure_goal")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Configure Daily Lesson Size",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatPill(
                                    title = "Due",
                                    count = stats.dueCount,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    title = "New",
                                    count = stats.newCount,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    title = "Mastered",
                                    count = stats.masteredCount,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            val startSource = remember { MutableInteractionSource() }
                            Button(
                                onClick = onStartDailySession,
                                interactionSource = startSource,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .pressScale(startSource)
                                    .testTag("button_start_session"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandIndigoDark),
                                shape = RoundedCornerShape(18.dp)
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
                  }
                }

                // 7 Practice Channels Skill Health Overview
                item {
                  AnimatedEntrance(index = 4) {
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
                                    AnimatedProgressBar(
                                        progress = summary.accuracy,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = if (summary.accuracy >= 0.8f) SuccessGreen else if (summary.accuracy >= 0.5f) PrimaryBlue else AccentGold,
                                        height = 6
                                    )
                                }
                            }
                        }
                    }
                  }
                }

                // Overall Completion Progress
                item {
                  AnimatedEntrance(index = 5) {
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

                            AnimatedProgressBar(
                                progress = stats.overallCompletionPct / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = FreshMint,
                                height = 12
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${stats.learningCount + stats.reviewingCount + stats.masteredCount} of ${stats.totalEntries} introduced",
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
                  AnimatedEntrance(index = 6) {
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
                }

                // Row: Confusable Words & Sentence Coach
                item {
                  AnimatedEntrance(index = 7) {
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
                }

                // Row: Browse & 7 Practice Modes
                item {
                  AnimatedEntrance(index = 8) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Browse ${stats.totalEntries} Words",
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
                }

                // Row: Study Planner & Backup/Restore
                item {
                  AnimatedEntrance(index = 9) {
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
                }

                // Progress & Settings
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Progress & Badges",
                            subtitle = "Weekly charts, XP & streaks",
                            icon = Icons.Default.Insights,
                            color = FreshMint,
                            onClick = onNavigateToProgress,
                            modifier = Modifier.weight(1f).testTag("card_progress")
                        )
                        ActionCard(
                            title = "Settings",
                            subtitle = "Theme, translation, reminders",
                            icon = Icons.Default.Settings,
                            color = PrimaryBlue,
                            onClick = onNavigateToSettings,
                            modifier = Modifier.weight(1f).testTag("card_settings")
                        )
                    }
                }

                // Difficult words & Source anomalies
                item {
                  AnimatedEntrance(index = 10) {
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
                            subtitle = if (anomalyCount > 0) "$anomalyCount duplicate/OCR notes" else "Page-by-page list audit",
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
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedCounter(
                value = count,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = color.copy(alpha = 0.85f)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier.bouncyClickable(onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
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

@Composable
private fun StreakCard(game: GamificationState, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Flame gently pulses while today's study is still pending
            val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "flame")
            val flameScale by transition.animateFloat(
                initialValue = 1f,
                targetValue = if (game.studiedToday) 1f else 1.15f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    androidx.compose.animation.core.tween(700),
                    androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "flameScale"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥", fontSize = 28.sp, modifier = Modifier.graphicsLayer { scaleX = flameScale; scaleY = flameScale })
                Text("${game.streakDays} day${if (game.streakDays == 1) "" else "s"}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Level ${game.level}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${game.totalXp} XP", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(6.dp))
                AnimatedProgressBar(progress = game.dailyProgress, color = if (game.dailyProgress >= 1f) FreshMint else SunnyAmber, height = 8)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when {
                        game.dailyProgress >= 1f -> "Daily goal reached ✅"
                        game.xpToGoal <= 20 -> "Only ${game.xpToGoal} XP to your goal — almost there!"
                        else -> "Today: ${game.todayXp} / ${game.dailyGoalXp} XP"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏅", fontSize = 22.sp)
                Text("${game.earnedBadges.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (game.streakFreezes > 0) {
                    Text("🧊×${game.streakFreezes}", fontSize = 11.sp, color = SkyBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DailyQuestsCard(game: GamificationState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Daily quests", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                val done = game.quests.count { it.done }
                Text(
                    "$done/${game.quests.size} • +25 XP each",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            game.quests.forEach { quest ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    val pop by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (quest.done) 1f else 0f,
                        animationSpec = com.example.ui.theme.Motion.jelly(),
                        label = "questDone"
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer { val sc = 1f + 0.15f * pop * (1f - pop) * 4f; scaleX = sc; scaleY = sc }
                            .background(
                                if (quest.done) FreshMint.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(if (quest.done) "✅" else quest.type.emoji, fontSize = 17.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            quest.type.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (quest.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        AnimatedProgressBar(
                            progress = quest.fraction,
                            color = if (quest.done) FreshMint else BrandIndigo,
                            height = 7
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "${quest.progress}/${quest.type.target}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Today's reward chest. It wobbles to invite a tap; opening it reveals a random reward
 * (XP, sometimes a streak freeze, rarely a jackpot) with a springy reveal.
 */
@Composable
private fun MysteryChestCard(reward: com.example.util.ChestReward?, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val wobble by androidx.compose.animation.core.rememberInfiniteTransition(label = "chest").animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "chestWobble"
    )
    val revealScale = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(reward) {
        if (reward != null) revealScale.animateTo(1f, com.example.ui.theme.Motion.jelly())
    }
    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth().bouncyClickable { if (reward == null) onOpen() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(SunnyAmber, WarmCoral)))) {
            LiquidBlobs(colors = listOf(Color.White, SunnyAmber, Lavender), modifier = Modifier.matchParentSize(), periodMillis = 8000)
            Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (reward == null) "🎁" else if (reward?.jackpot == true) "💰" else if (reward?.streakFreeze == true) "🧊" else "✨",
                    fontSize = 40.sp,
                    modifier = Modifier.graphicsLayer {
                        if (reward == null) rotationZ = wobble
                        else { scaleX = 0.6f + 0.4f * revealScale.value; scaleY = 0.6f + 0.4f * revealScale.value }
                    }
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    val r = reward
                    Text(
                        when {
                            r == null -> "Daily goal reached!"
                            r.jackpot -> "JACKPOT!"
                            r.streakFreeze -> "Streak freeze!"
                            else -> "Nice find!"
                        },
                        color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp
                    )
                    Text(
                        when {
                            r == null -> "Tap to open your mystery chest"
                            r.streakFreeze -> "+${r.xp} XP and a freeze that protects your streak"
                            else -> "+${r.xp} bonus XP"
                        },
                        color = Color.White.copy(alpha = 0.92f), fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeBanner(best: Int, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(WarmCoral, SunnyAmber)))) {
            LiquidBlobs(colors = listOf(Lavender, SoftRose), modifier = Modifier.matchParentSize(), periodMillis = 9000)
            Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⚡", fontSize = 34.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("60-Second Challenge", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Text(
                        if (best > 0) "Personal best: $best — can you beat it?" else "How many words can you get in a minute?",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 13.sp
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }
    }
}
