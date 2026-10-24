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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VocabEntry
import com.example.ui.components.AnomalyBadge
import com.example.ui.components.AudioSpeakButton
import com.example.ui.components.BookmarkToggleButton
import com.example.ui.components.DifficultToggleButton
import com.example.ui.components.EntryNumberBadge
import com.example.ui.components.PageBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.PrimaryBlue
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
    val displayEntries by viewModel.displayEntries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val selectedPage by viewModel.selectedPage.collectAsState()
    val currentSort by viewModel.sort.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vocabulary Library (1,212 Words)") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort Options")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("By Number (#1 - #1212)") },
                                onClick = {
                                    viewModel.setSort(VocabSort.BY_NUMBER)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alphabetical (A - Z)") },
                                onClick = {
                                    viewModel.setSort(VocabSort.ALPHABETICAL)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Mastery Level") },
                                onClick = {
                                    viewModel.setSort(VocabSort.BY_MASTERY)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Frequently Forgotten") },
                                onClick = {
                                    viewModel.setSort(VocabSort.BY_FORGOTTEN)
                                    showSortMenu = false
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
                placeholder = { Text("Search word, meaning, note, or # (e.g. 19, cope with)...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input")
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.ALL,
                        onClick = { viewModel.setFilter(VocabFilter.ALL) },
                        label = { Text("All (1212)") }
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
                        label = { Text("New") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.MASTERED,
                        onClick = { viewModel.setFilter(VocabFilter.MASTERED) },
                        label = { Text("Mastered") }
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
                        label = { Text("Source Flags") }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == VocabFilter.BY_PAGE,
                        onClick = { viewModel.setFilter(VocabFilter.BY_PAGE) },
                        label = { Text("By Page (1-46)") }
                    )
                }
            }

            // If "By Page" is selected, show page selector scrollable tab row
            if (currentFilter == VocabFilter.BY_PAGE) {
                ScrollableTabRow(
                    selectedTabIndex = selectedPage - 1,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (page in 1..46) {
                        Tab(
                            selected = selectedPage == page,
                            onClick = { viewModel.setSelectedPage(page) },
                            text = { Text("P. $page") }
                        )
                    }
                }
            }

            // Results summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${displayEntries.size} entries found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (currentSort) {
                        VocabSort.BY_NUMBER -> "Sorted: #1 → #1212"
                        VocabSort.ALPHABETICAL -> "Sorted: A → Z"
                        VocabSort.BY_MASTERY -> "Sorted: Mastery"
                        VocabSort.BY_FORGOTTEN -> "Sorted: Forgotten"
                    },
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            // Vocabulary List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = displayEntries,
                    key = { it.entryNumber }
                ) { entry ->
                    VocabItemCard(
                        entry = entry,
                        onWordClick = {
                            viewModel.selectEntry(entry)
                            onWordSelected(entry)
                        },
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
    onWordClick: () -> Unit,
    onSpeak: (String) -> Unit,
    onToggleDifficult: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onWordClick() }
            .testTag("vocab_item_${entry.entryNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EntryNumberBadge(number = entry.entryNumber)
                    PageBadge(page = entry.sourcePage)
                    StatusBadge(status = entry.studyStatus)
                    if (entry.hasAnomaly) {
                        AnomalyBadge()
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AudioSpeakButton(
                        textToSpeak = entry.headword,
                        onSpeak = onSpeak
                    )
                    DifficultToggleButton(
                        isDifficult = entry.isDifficult,
                        onToggle = onToggleDifficult
                    )
                    BookmarkToggleButton(
                        isBookmarked = entry.isBookmarked,
                        onToggle = onToggleBookmark
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.headword,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (entry.grammaticalLabels.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${entry.grammaticalLabels.joinToString(", ")})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entry.definitions.joinToString(" • "),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (entry.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Related: ${entry.notes}",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (entry.dictionarySynonyms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Syn (Dict)",
                            color = Color(0xFF7C3AED),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.dictionarySynonyms.joinToString(", "),
                        fontSize = 12.sp,
                        color = Color(0xFF6D28D9),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
