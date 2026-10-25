package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConfusablePair
import com.example.data.model.LearningDirection
import com.example.data.model.VocabEntry
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryIndigo
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.VocabViewModel
import com.example.util.ConfusableWordsData

private enum class ConfusableScope(val label: String) {
    CURATED("Curated Traps (24)"),
    ALL_1212("All 1,212 Words"),
    BY_PAGE("By Page (1–46)"),
    ANOMALIES("27 PDF Anomalies & Duplicates")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfusableWordsScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allEntries by viewModel.allEntries.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedScope by remember { mutableStateOf(ConfusableScope.CURATED) }
    var selectedPage by remember { mutableIntStateOf(1) }

    val activePairs = remember(allEntries, selectedScope, selectedPage) {
        when (selectedScope) {
            ConfusableScope.CURATED -> ConfusableWordsData.PAIRS
            ConfusableScope.ALL_1212 -> ConfusableWordsData.build1212ContrastPairs(allEntries)
            ConfusableScope.BY_PAGE -> ConfusableWordsData.build1212ContrastPairs(allEntries, pageFilter = selectedPage)
            ConfusableScope.ANOMALIES -> ConfusableWordsData.build1212ContrastPairs(allEntries, anomaliesOnly = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Confusable Words & 1,212 Contrast Lab",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Master TOEFL look-alikes, synonyms & all 1,212 contrasts",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Study Pairs (${activePairs.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.CompareArrows, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Discrimination Quiz (${activePairs.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) }
                )
            }

            // Scope & Page Filter bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ConfusableScope.entries) { scope ->
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

                    if (selectedScope == ConfusableScope.BY_PAGE) {
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
                0 -> ConfusableStudyList(pairs = activePairs, onSpeak = { viewModel.speak(it) })
                1 -> ConfusableQuiz(
                    pairs = activePairs,
                    allEntries = allEntries,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun ConfusableStudyList(
    pairs: List<ConfusablePair>,
    onSpeak: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPairs = remember(searchQuery, pairs) {
        if (searchQuery.isBlank()) pairs
        else pairs.filter {
            it.wordA.contains(searchQuery, ignoreCase = true) ||
                    it.wordB.contains(searchQuery, ignoreCase = true) ||
                    it.distinction.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search ${pairs.size} confusable & contrast pairs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_confusables"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filteredPairs, key = { it.id }) { pair ->
            ConfusablePairCard(pair = pair, onSpeak = onSpeak)
        }
    }
}

@Composable
private fun ConfusablePairCard(
    pair: ConfusablePair,
    onSpeak: (String) -> Unit
) {
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
                        text = pair.wordA,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    IconButton(
                        onClick = { onSpeak(pair.wordA.substringBefore(" (")) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak ${pair.wordA}",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "vs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Text(
                        text = pair.wordB,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                    IconButton(
                        onClick = { onSpeak(pair.wordB.substringBefore(" (")) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak ${pair.wordB}",
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = PrimaryBlue.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pair.distinction,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Example A
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "${pair.wordA}:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Text(
                    text = "\"${pair.exampleA}\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Example B
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "${pair.wordB}:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Text(
                    text = "\"${pair.exampleB}\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConfusableQuiz(
    pairs: List<ConfusablePair>,
    allEntries: List<VocabEntry>,
    viewModel: VocabViewModel
) {
    var currentIndex by remember(pairs) { mutableIntStateOf(0) }
    var selectedOption by remember(pairs, currentIndex) { mutableStateOf<String?>(null) }
    var score by remember(pairs) { mutableIntStateOf(0) }
    var completedCount by remember(pairs) { mutableIntStateOf(0) }

    val currentPair = pairs.getOrNull(currentIndex)

    if (currentPair == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quiz Finished!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score: $score / ${pairs.size}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        currentIndex = 0
                        selectedOption = null
                        score = 0
                        completedCount = 0
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restart Quiz")
                }
            }
        }
        return
    }

    val options = remember(currentPair) {
        listOf(currentPair.wordA, currentPair.wordB).shuffled()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${pairs.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )
                Text(
                    text = "Score: $score / $completedCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Complete the academic sentence with the correct term:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentPair.exerciseSentence,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { opt ->
                    val isSelected = selectedOption == opt
                    val isCorrect = opt.equals(currentPair.correctWord, ignoreCase = true) ||
                            (currentPair.correctWord.startsWith(opt, ignoreCase = true) && opt.length >= 4)
                    val showEvaluation = selectedOption != null

                    val bgColor = when {
                        !showEvaluation -> MaterialTheme.colorScheme.surface
                        isCorrect -> SuccessGreen.copy(alpha = 0.15f)
                        isSelected -> ErrorRed.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val borderColor = when {
                        !showEvaluation -> MaterialTheme.colorScheme.outlineVariant
                        isCorrect -> SuccessGreen
                        isSelected -> ErrorRed
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedOption == null) {
                                selectedOption = opt
                                completedCount++
                                if (isCorrect) score++
                                val cleanHeadword = currentPair.correctWord.substringBefore(" (")
                                allEntries.firstOrNull {
                                    it.headword.equals(cleanHeadword, ignoreCase = true)
                                }?.let { matchedEntry ->
                                    viewModel.recordSkillPractice(
                                        entry = matchedEntry,
                                        direction = LearningDirection.SYNONYM,
                                        isCorrect = isCorrect
                                    )
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = opt,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (showEvaluation) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedOption != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Distinction & Linguistic Rule",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentPair.explanation,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        currentIndex++
                        selectedOption = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("button_next_confusable"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentIndex + 1 < pairs.size) "Next Question" else "View Results",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
