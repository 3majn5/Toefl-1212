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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.ui.theme.AccentGold
import com.example.ui.theme.Navy800
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.VocabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeModesScreen(
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDirection by remember { mutableStateOf(LearningDirection.REVERSE_RECALL) }
    var questionCount by remember { mutableIntStateOf(10) }
    var selectedPageFilter by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("7 Learning Directions") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("practice_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Select Training Direction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Target different cognitive pathways from the official 1212 list",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Direction Cards
            item {
                DirectionOptionCard(
                    title = "1. Reverse Recall",
                    description = "Show PDF meaning/synonym → Recall the headword",
                    icon = Icons.Default.SyncAlt,
                    iconColor = PrimaryBlue,
                    isSelected = selectedDirection == LearningDirection.REVERSE_RECALL,
                    onSelect = { selectedDirection = LearningDirection.REVERSE_RECALL },
                    testTag = "direction_reverse_recall"
                )
            }

            item {
                DirectionOptionCard(
                    title = "2. Recognition",
                    description = "Show headword → Choose the correct definition",
                    icon = Icons.Default.Visibility,
                    iconColor = SuccessGreen,
                    isSelected = selectedDirection == LearningDirection.RECOGNITION,
                    onSelect = { selectedDirection = LearningDirection.RECOGNITION },
                    testTag = "direction_recognition"
                )
            }

            item {
                DirectionOptionCard(
                    title = "3. Spelling",
                    description = "Hear pronunciation or read definition → Type word",
                    icon = Icons.Default.Spellcheck,
                    iconColor = AccentGold,
                    isSelected = selectedDirection == LearningDirection.SPELLING,
                    onSelect = { selectedDirection = LearningDirection.SPELLING },
                    testTag = "direction_spelling"
                )
            }

            item {
                DirectionOptionCard(
                    title = "4. Context Usage",
                    description = "Fill in the blank with appropriate TOEFL vocabulary",
                    icon = Icons.Default.MenuBook,
                    iconColor = WarningOrange,
                    isSelected = selectedDirection == LearningDirection.CONTEXT,
                    onSelect = { selectedDirection = LearningDirection.CONTEXT },
                    testTag = "direction_context"
                )
            }

            item {
                DirectionOptionCard(
                    title = "5. Synonym Discrimination",
                    description = "Distinguish close synonyms and nuance differences",
                    icon = Icons.Default.Shuffle,
                    iconColor = Color(0xFF8B5CF6),
                    isSelected = selectedDirection == LearningDirection.SYNONYM,
                    onSelect = { selectedDirection = LearningDirection.SYNONYM },
                    testTag = "direction_synonym"
                )
            }

            item {
                DirectionOptionCard(
                    title = "6. Production",
                    description = "Compose original academic sentence using the target word",
                    icon = Icons.Default.Create,
                    iconColor = Color(0xFFEC4899),
                    isSelected = selectedDirection == LearningDirection.PRODUCTION,
                    onSelect = { selectedDirection = LearningDirection.PRODUCTION },
                    testTag = "direction_production"
                )
            }

            item {
                DirectionOptionCard(
                    title = "7. Listening",
                    description = "Listen to Text-To-Speech audio → Identify headword",
                    icon = Icons.Default.Hearing,
                    iconColor = Color(0xFF06B6D4),
                    isSelected = selectedDirection == LearningDirection.LISTENING,
                    onSelect = { selectedDirection = LearningDirection.LISTENING },
                    testTag = "direction_listening"
                )
            }

            // Question Count Selector
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Number of Questions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(5, 10, 15, 20).forEach { count ->
                        FilterChip(
                            selected = questionCount == count,
                            onClick = { questionCount = count },
                            label = { Text("$count questions") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Optional Page Filter
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scope Filter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedPageFilter == null,
                            onClick = { selectedPageFilter = null },
                            label = { Text("All 46 Pages (1212 Words)") }
                        )
                    }
                    items(46) { index ->
                        val page = index + 1
                        FilterChip(
                            selected = selectedPageFilter == page,
                            onClick = { selectedPageFilter = page },
                            label = { Text("Page $page") }
                        )
                    }
                }
            }

            // Start Practice Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.startPracticeMode(
                            direction = selectedDirection,
                            count = questionCount,
                            pageFilter = selectedPageFilter
                        )
                        onStartSession()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("launch_practice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start ${selectedDirection.label} Practice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DirectionOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        shape = RoundedCornerShape(14.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue) else null,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}
