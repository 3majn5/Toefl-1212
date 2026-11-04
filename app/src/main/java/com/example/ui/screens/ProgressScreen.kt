package com.example.ui.screens

import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ui.theme.WarmCoral
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Lavender
import com.example.ui.components.LiquidBlobs
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.ui.components.AccuracySparkPanel
import com.example.ui.components.AnimatedCounter
import com.example.ui.components.AnimatedEntrance
import com.example.ui.components.AnimatedProgressBar
import com.example.ui.components.WeeklyBarChart
import com.example.ui.theme.FreshMint
import com.example.ui.theme.HeroGradient
import com.example.ui.theme.SunnyAmber
import com.example.util.Badge
import com.example.ui.viewmodel.VocabViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit
) {
    val game by viewModel.gameState.collectAsState()
    val progress by viewModel.weeklyProgress.collectAsState()
    val stats by viewModel.studyStats.collectAsState()
    val bestChallenge by viewModel.bestChallenge.collectAsState()
    val wordsStudied by viewModel.wordsStudied.collectAsState()
    val shareContext = androidx.compose.ui.platform.LocalContext.current
    val cardData = com.example.util.ProgressCardData(
        streakDays = game.streakDays,
        level = game.level,
        totalXp = game.totalXp,
        wordsLearned = wordsStudied,
        wordsMastered = stats.masteredCount,
        badges = game.earnedBadges.size,
        bestChallenge = bestChallenge
    )
    val cardPreview = androidx.compose.runtime.remember(cardData) {
        com.example.util.ProgressCard.render(cardData).asImageBitmap()
    }
    var showTable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshGameState()
        viewModel.refreshProgress()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Badges", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Level, XP and streak summary
            item {
                AnimatedEntrance(index = 0) {
                    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.background(HeroGradient)) {
                            LiquidBlobs(colors = listOf(WarmCoral, SkyBlue, Lavender), modifier = Modifier.matchParentSize())
                            Column(modifier = Modifier.padding(22.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(64.dp).clip(CircleShape)
                                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${game.level}", color = androidx.compose.ui.graphics.Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Level ${game.level}", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text("${game.xpIntoLevel} / ${game.xpForNextLevel} XP to level ${game.level + 1}",
                                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    AnimatedProgressBar(
                                        progress = game.levelProgress,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                                SummaryStat("🔥", game.streakDays, "day streak")
                                SummaryStat("🏅", game.bestStreak, "best streak")
                                SummaryStat("⭐", game.totalXp, "total XP")
                            }
                        }
                        }
                    }
                }
            }

            // Daily goal
            item {
                AnimatedEntrance(index = 1) {
                    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row {
                                Text("Today's goal", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${game.todayXp} / ${game.dailyGoalXp} XP", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(10.dp))
                            AnimatedProgressBar(progress = game.dailyProgress, color = if (game.dailyProgress >= 1f) FreshMint else SunnyAmber, height = 12)
                            if (game.dailyProgress >= 1f) {
                                Spacer(Modifier.height(6.dp))
                                Text("Goal reached — great work today! ✅", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Shareable progress card
            item {
                AnimatedEntrance(index = 2) {
                    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Share your progress", fontWeight = FontWeight.Bold)
                            Text("Post your streak to Instagram, X, or send it to a study buddy", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            androidx.compose.foundation.Image(
                                bitmap = cardPreview,
                                contentDescription = "Progress card preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(com.example.util.ProgressCard.WIDTH / com.example.util.ProgressCard.HEIGHT.toFloat())
                                    .clip(RoundedCornerShape(18.dp))
                            )
                            Spacer(Modifier.height(12.dp))
                            androidx.compose.material3.Button(
                                onClick = { com.example.util.ProgressCard.share(shareContext, cardData) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Share progress card", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Words learned per week
            item {
                AnimatedEntrance(index = 2) {
                    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Words learned per week", fontWeight = FontWeight.Bold)
                                    Text("New words you studied for the first time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { showTable = !showTable }) { Text(if (showTable) "Chart" else "Table") }
                            }
                            Spacer(Modifier.height(12.dp))
                            val p = progress
                            when {
                                p == null -> Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                showTable -> DataTable(
                                    header = listOf("Week of", "Words"),
                                    rows = p.weekLabels.indices.map { listOf(p.weekLabels[it], "${p.wordsLearned[it]}") }
                                )
                                else -> WeeklyBarChart(labels = p.weekLabels, values = p.wordsLearned, unit = "words")
                            }
                        }
                    }
                }
            }

            // Accuracy per skill over time — small multiples on a shared 0–100% scale
            item {
                AnimatedEntrance(index = 3) {
                    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Accuracy per skill", fontWeight = FontWeight.Bold)
                            Text("Weekly accuracy, last 8 weeks · each panel 0–100% · tap a point", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            val p = progress
                            if (p == null) {
                                Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else if (showTable) {
                                DataTable(
                                    header = listOf("Skill") + p.weekLabels.takeLast(4),
                                    rows = LearningDirection.values().map { dir ->
                                        listOf(dir.displayName) + (p.skillAccuracy[dir] ?: emptyList()).takeLast(4).map { v -> v?.let { "${(it * 100).toInt()}%" } ?: "–" }
                                    }
                                )
                            } else {
                                LearningDirection.values().forEachIndexed { i, dir ->
                                    if (i > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    AccuracySparkPanel(title = dir.displayName, values = p.skillAccuracy[dir] ?: emptyList(), labels = p.weekLabels)
                                }
                            }
                        }
                    }
                }
            }

            // Badges
            item {
                AnimatedEntrance(index = 4) {
                    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Badges · ${game.earnedBadges.size} of ${Badge.values().size}", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Badge.values().forEach { badge ->
                                    BadgeTile(badge = badge, earned = badge in game.earnedBadges)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "${stats.learningCount + stats.reviewingCount + stats.masteredCount} words started · ${stats.masteredCount} mastered in the current list",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(emoji: String, value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        AnimatedCounter(
            value = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = androidx.compose.ui.graphics.Color.White
        )
        Text(label, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

@Composable
private fun BadgeTile(badge: Badge, earned: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
            .alpha(if (earned) 1f else 0.45f)
    ) {
        Text(if (earned) badge.emoji else "🔒", fontSize = 28.sp)
        Text(badge.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(badge.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 13.sp)
    }
}

/** Plain table fallback for the charts (screen readers, exact values). */
@Composable
private fun DataTable(header: List<String>, rows: List<List<String>>) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            header.forEachIndexed { i, h ->
                Text(h, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(if (i == 0) 1.6f else 1f))
            }
        }
        HorizontalDivider()
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                row.forEachIndexed { i, cell ->
                    Text(cell, fontSize = 12.sp, modifier = Modifier.weight(if (i == 0) 1.6f else 1f))
                }
            }
        }
    }
}
