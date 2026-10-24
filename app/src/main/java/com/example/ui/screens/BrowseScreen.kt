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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VocabEntry
import com.example.ui.components.AnomalyBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TtsButton
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.VocabFilter
import com.example.ui.viewmodel.VocabSort
import com.example.ui.viewmodel.VocabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    onWordSelected: (VocabEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.displayEntries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val selectedPage by viewModel.selectedPage.collectAsState()
    val currentSort by viewModel.sort.collectAsState()

    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "1,212 Vocabulary Entries",
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
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort Vocabulary",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Original Number (1 - 1,212)") },
                                onClick = {
                                    viewModel.setSort(VocabSort.BY_NUMBER)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alphabetical (A - Z)") },
                                onClick = {
                                    viewModel.setSort(VocabSort.ALPHABETICAL)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mastery Level") },
                                onClick = {
                                    viewModel.setSort(VocabSort.BY_MASTERY)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Frequently Forgotten") },
                                onClick = {
                                    viewModel.setSort(VocabSort.BY_FORGOTTEN)
                                    sortMenuExpanded = false
                                }
                            )
                        }
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("input_search_vocab"),
                placeholder = { Text("Search word, meaning, or entry #...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.ALL,
                        onClick = { viewModel.setFilter(VocabFilter.ALL) },
                        label = { Text("All (1,212)") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.BY_PAGE,
                        onClick = { viewModel.setFilter(VocabFilter.BY_PAGE) },
                        label = { Text("By Page ($selectedPage/46)") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.DUE_REVIEW,
                        onClick = { viewModel.setFilter(VocabFilter.DUE_REVIEW) },
                        label = { Text("Due Review") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.NEW_WORDS,
                        onClick = { viewModel.setFilter(VocabFilter.NEW_WORDS) },
                        label = { Text("New Words") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.DIFFICULT,
                        onClick = { viewModel.setFilter(VocabFilter.DIFFICULT) },
                        label = { Text("Difficult") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.BOOKMARKED,
                        onClick = { viewModel.setFilter(VocabFilter.BOOKMARKED) },
                        label = { Text("Bookmarked") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.ANOMALIES,
                        onClick = { viewModel.setFilter(VocabFilter.ANOMALIES) },
                        label = { Text("Flags / Anomalies") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.MASTERED,
                        onClick = { viewModel.setFilter(VocabFilter.MASTERED) },
                        label = { Text("Mastered") }
                    )
                }
            }

            // Page Selector if Filter by Page is active
            if (currentFilter == VocabFilter.BY_PAGE) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items((1..46).toList()) { p ->
                        FilterChip(
                            selected = selectedPage == p,
                            onClick = { viewModel.setSelectedPage(p) },
                            label = { Text("Page $p") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Result count indicator
            Text(
                text = "Showing ${entries.size} entries",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Vocabulary List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp, top = 4.dp)
            ) {
                items(entries, key = { it.entryNumber }) { entry ->
                    VocabItemCard(
                        entry = entry,
                        onClick = { onWordSelected(entry) },
                        onSpeak = { viewModel.speak(it) },
                        onToggleDifficult = { viewModel.toggleDifficult(entry) },
                        onToggleBookmark = { viewModel.toggleBookmark(entry) }
                    )
                }
            }
        }
    }
}

@Composable
fun VocabItemCard(
    entry: VocabEntry,
    onClick: () -> Unit,
    onSpeak: (String) -> Unit,
    onToggleDifficult: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_vocab_item_${entry.entryNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${entry.entryNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Page ${entry.sourcePage}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = entry.studyStatus)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onToggleDifficult,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isDifficult) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Difficult Toggle",
                            tint = if (entry.isDifficult) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark Toggle",
                            tint = if (entry.isBookmarked) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.headword,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (entry.grammaticalLabels.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${entry.grammaticalLabels.joinToString()})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TtsButton(text = entry.headword, onSpeak = onSpeak, size = 18)
            }

            Text(
                text = entry.primaryMeaning,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Dictionary Synonyms preview badge if available
            if (entry.dictionarySynonyms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Syn (Dict): ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = entry.dictionarySynonyms.take(3).joinToString(", "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Anomaly note if present
            if (entry.hasAnomaly) {
                Spacer(modifier = Modifier.height(6.dp))
                AnomalyBadge(notes = entry.anomalyNotes)
            }
        }
    }
}
