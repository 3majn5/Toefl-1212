package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningDirection
import com.example.data.model.QuizQuestion
import com.example.data.model.VocabEntry
import com.example.ui.components.EntryNumberBadge
import com.example.ui.components.PageBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.SessionPhase
import com.example.ui.viewmodel.VocabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(
    viewModel: VocabViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phase by viewModel.sessionPhase.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (phase) {
                            is SessionPhase.IntroNew -> "Introduction Phase"
                            is SessionPhase.Quiz -> "Practice & Recall Phase"
                            is SessionPhase.MistakeReview -> "Mistake Review"
                            is SessionPhase.Completed -> "Session Summary"
                            SessionPhase.Idle -> "Study Session"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.exitSession()
                            onFinish()
                        },
                        modifier = Modifier.testTag("session_close_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Session")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val current = phase) {
                is SessionPhase.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active session.")
                    }
                }
                is SessionPhase.IntroNew -> {
                    IntroNewWordView(
                        currentIndex = current.currentIndex,
                        total = current.entries.size,
                        entry = current.entries[current.currentIndex],
                        onSpeak = { viewModel.speak(it) },
                        onNext = { viewModel.nextIntroWord() }
                    )
                }
                is SessionPhase.Quiz -> {
                    val q = current.questions[current.currentIndex]
                    QuizQuestionView(
                        question = q,
                        currentIndex = current.currentIndex,
                        total = current.questions.size,
                        onSpeak = { viewModel.speak(it) },
                        onAnswer = { isCorrect, rating ->
                            viewModel.answerQuizQuestion(isCorrect, rating)
                        }
                    )
                }
                is SessionPhase.MistakeReview -> {
                    val q = current.questions[current.currentIndex]
                    QuizQuestionView(
                        question = q,
                        currentIndex = current.currentIndex,
                        total = current.questions.size,
                        onSpeak = { viewModel.speak(it) },
                        onAnswer = { isCorrect, _ ->
                            viewModel.answerMistakeReviewQuestion(isCorrect)
                        },
                        isMistakeReview = true
                    )
                }
                is SessionPhase.Completed -> {
                    SessionCompletedView(
                        completed = current,
                        onDone = {
                            viewModel.exitSession()
                            onFinish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun IntroNewWordView(
    currentIndex: Int,
    total: Int,
    entry: VocabEntry,
    onSpeak: (String) -> Unit,
    onNext: () -> Unit
) {
    var isFlipped by remember(entry) { mutableStateOf(false) }
    val progress = (currentIndex + 1).toFloat() / total

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Word ${currentIndex + 1} of $total",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Tap card to flip",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryBlue
                )
            }
        }

        // Flashcard Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFlipped) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clickable { isFlipped = !isFlipped }
                    .testTag("flashcard_item")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EntryNumberBadge(number = entry.entryNumber)
                        PageBadge(page = entry.sourcePage)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = entry.headword,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (entry.grammaticalLabels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(${entry.grammaticalLabels.joinToString(", ")})",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    IconButton(
                        onClick = { onSpeak(entry.headword) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryBlue.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Pronounce Word",
                            tint = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isFlipped) {
                        if (entry.dictionarySynonyms.isNotEmpty()) {
                            Surface(
                                color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Syn: ${entry.dictionarySynonyms.take(3).joinToString(", ")}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = "Tap to view PDF definition & synonyms",
                            fontSize = 13.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = entry.definitions.joinToString(" • "),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                textAlign = TextAlign.Center
                            )

                            if (entry.dictionarySynonyms.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Synonyms by Dictionary: ${entry.dictionarySynonyms.joinToString(" • ")}",
                                        color = Color(0xFF7C3AED),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (entry.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Related: ${entry.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (entry.exampleSentence.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "“${entry.exampleSentence}”",
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { onSpeak(entry.exampleSentence) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Read Example",
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action button
        item {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("flashcard_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (currentIndex + 1 < total) "Next Word" else "Proceed to Quiz",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun QuizQuestionView(
    question: QuizQuestion,
    currentIndex: Int,
    total: Int,
    onSpeak: (String) -> Unit,
    onAnswer: (Boolean, Int) -> Unit,
    isMistakeReview: Boolean = false
) {
    var selectedOption by remember(question) { mutableStateOf<String?>(null) }
    var typedAnswer by remember(question) { mutableStateOf("") }
    var isSubmitted by remember(question) { mutableStateOf(false) }
    var isCorrect by remember(question) { mutableStateOf(false) }

    val progress = (currentIndex + 1).toFloat() / total

    // Auto-play audio for listening questions
    LaunchedEffect(question) {
        if (question.direction == LearningDirection.LISTENING) {
            onSpeak(question.correctAnswer)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMistakeReview) "Mistake Review: ${currentIndex + 1}/$total" else "Question ${currentIndex + 1} of $total",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = question.direction.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isMistakeReview) WarningOrange else PrimaryBlue
                )
            }
        }

        // Question Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = question.prompt,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    when (question.direction) {
                        LearningDirection.RECOGNITION -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = question.entry.headword,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onSpeak(question.entry.headword) }) {
                                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Listen", tint = PrimaryBlue)
                                }
                            }
                        }
                        LearningDirection.REVERSE_RECALL -> {
                            Text(
                                text = "\"${question.entry.definitions.joinToString(" • ")}\"",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        LearningDirection.CONTEXT -> {
                            Text(
                                text = question.contextSentence ?: "",
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        LearningDirection.LISTENING -> {
                            Button(
                                onClick = { onSpeak(question.correctAnswer) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Play Spoken Word Audio")
                            }
                        }
                        LearningDirection.SPELLING, LearningDirection.PRODUCTION, LearningDirection.SYNONYM -> {
                            Text(
                                text = question.entry.primaryMeaning,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }

        // Interactive Response Area
        if (question.direction == LearningDirection.SPELLING) {
            item {
                Column {
                    OutlinedTextField(
                        value = typedAnswer,
                        onValueChange = { typedAnswer = it },
                        label = { Text("Type the headword") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("spelling_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                val match = typedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                                isCorrect = match
                                isSubmitted = true
                            },
                            enabled = typedAnswer.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("spelling_submit_button")
                        ) {
                            Text("Check Spelling")
                        }
                    }
                }
            }
        } else if (question.direction == LearningDirection.PRODUCTION) {
            item {
                Column {
                    OutlinedTextField(
                        value = typedAnswer,
                        onValueChange = { typedAnswer = it },
                        label = { Text("Write your sentence containing '${question.correctAnswer}'") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("production_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                val containsWord = typedAnswer.lowercase().contains(question.correctAnswer.lowercase())
                                val lengthOk = typedAnswer.trim().split("\\s+".toRegex()).size >= 5
                                isCorrect = containsWord && lengthOk
                                isSubmitted = true
                            },
                            enabled = typedAnswer.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("production_submit_button")
                        ) {
                            Text("Evaluate Sentence")
                        }
                    }
                }
            }
        } else {
            // Multiple Choice Options (4 choices)
            items(question.options.size) { idx ->
                val option = question.options[idx]
                val isSelected = selectedOption == option

                val buttonColor = if (isSubmitted) {
                    when {
                        option == question.correctAnswer -> SuccessGreen
                        isSelected -> ErrorRed
                        else -> MaterialTheme.colorScheme.surface
                    }
                } else {
                    if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = buttonColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!isSubmitted && !isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSubmitted) {
                            selectedOption = option
                            isCorrect = option == question.correctAnswer
                            isSubmitted = true
                        }
                        .testTag("quiz_option_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + idx)}.",
                            fontWeight = FontWeight.Bold,
                            color = if (isSubmitted && (option == question.correctAnswer || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSubmitted && (option == question.correctAnswer || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSubmitted) {
                            if (option == question.correctAnswer) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Correct", tint = Color.White)
                            } else if (isSelected) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Incorrect", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Feedback & Next Button
        if (isSubmitted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCorrect) SuccessGreen else ErrorRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCorrect) "Correct!" else "Incorrect",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isCorrect) SuccessGreen else ErrorRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = question.explanation,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Preserved PDF Source: ${question.sourceDefinition}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (question.entry.exampleSentence.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Context Example: “${question.entry.exampleSentence}”",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryBlue
                            )
                        }
                        if (question.entry.dictionarySynonyms.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Synonyms (Dict): ${question.entry.dictionarySynonyms.joinToString(", ")}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7C3AED)
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val rating = if (isCorrect) 2 else 0
                        onAnswer(isCorrect, rating)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quiz_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (currentIndex + 1 < total) "Next Question" else "Finish Session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun SessionCompletedView(
    completed: SessionPhase.Completed,
    onDone: () -> Unit
) {
    val accuracy = if (completed.totalQuestions > 0) {
        (completed.correctAnswers.toFloat() / completed.totalQuestions) * 100f
    } else 100f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(AccentGold.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Session Completed!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your progress has been recorded into the Spaced Repetition system.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${completed.totalLearned}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text("Words Studied", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.0f", accuracy)}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text("Accuracy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("session_done_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}
