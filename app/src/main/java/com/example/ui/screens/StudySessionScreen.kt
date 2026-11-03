package com.example.ui.screens

import com.example.ui.components.AnimatedCounter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.key
import com.example.ui.components.AnimatedEntrance
import com.example.ui.components.AnimatedProgressBar
import com.example.ui.components.answerFeedback
import com.example.ui.components.ComboMeter
import com.example.ui.components.ConfettiBurst
import com.example.ui.components.LiquidBlobs
import com.example.ui.theme.Motion
import com.example.ui.components.TranslationLine
import com.example.data.model.WritingEvaluationResult
import com.example.util.SpellingChecker
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FsrsRating
import com.example.data.model.LearningDirection
import com.example.data.model.QuizQuestion
import com.example.data.model.VocabEntry
import com.example.ui.components.TtsButton
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryIndigo
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
                        text = when (phase) {
                            is SessionPhase.FsrsFlashcards -> "FSRS Spaced Review"
                            is SessionPhase.IntroNew -> "Study Card"
                            is SessionPhase.Quiz -> "Active Practice"
                            is SessionPhase.MistakeReview -> "Mistake Reinforcement"
                            is SessionPhase.Completed -> "Session Summary"
                            SessionPhase.Idle -> "Session"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.finishSession()
                            onFinish()
                        },
                        modifier = Modifier.testTag("session_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Session"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val currentPhase = phase) {
                is SessionPhase.Idle -> {
                    IdleView(
                        onStartDaily = { viewModel.startDailySession() },
                        onBack = onFinish
                    )
                }
                is SessionPhase.FsrsFlashcards -> {
                    FsrsFlashcardView(
                        phase = currentPhase,
                        viewModel = viewModel,
                        onSpeak = { viewModel.speak(it) },
                        onRate = { entry, rating ->
                            viewModel.recordFsrsRating(entry, rating)
                        }
                    )
                }
                is SessionPhase.IntroNew -> {
                    IntroCardView(
                        phase = currentPhase,
                        onSpeak = { viewModel.speak(it) },
                        onNext = { viewModel.advanceIntroCard() }
                    )
                }
                is SessionPhase.Quiz -> {
                    val combo by viewModel.combo.collectAsState()
                    QuizView(
                        phase = currentPhase,
                        combo = combo,
                        onGraded = { isCorrect, q -> viewModel.onQuizAnswerGraded(isCorrect, q) },
                        onSpeak = { viewModel.speak(it) },
                        onAnswer = { isCorrect, q ->
                            viewModel.recordQuizAnswer(isCorrect, q)
                        },
                        onEvaluateProduction = { sentence, q -> viewModel.evaluateProduction(sentence, q) }
                    )
                }
                is SessionPhase.MistakeReview -> {
                    MistakeReviewView(
                        phase = currentPhase,
                        onSpeak = { viewModel.speak(it) },
                        onNext = { q ->
                            viewModel.recordQuizAnswer(true, q)
                        }
                    )
                }
                is SessionPhase.Completed -> {
                    val stats by viewModel.sessionStats.collectAsState()
                    CompletedView(
                        phase = currentPhase,
                        stats = stats,
                        onFinish = {
                            viewModel.finishSession()
                            onFinish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleView(
    onStartDaily: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Active Session",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ready to boost your TOEFL score? Start today's recommended FSRS spaced review.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStartDaily,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("idle_start_button")
        ) {
            Text("Start FSRS Daily Session", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FsrsFlashcardView(
    phase: SessionPhase.FsrsFlashcards,
    viewModel: VocabViewModel,
    onSpeak: (String) -> Unit,
    onRate: (VocabEntry, FsrsRating) -> Unit
) {
    val entry = phase.entries.getOrNull(phase.currentIndex) ?: return
    var isFlipped by remember(phase.currentIndex) { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 180f),
        label = "card_rotation"
    )

    val previews = remember(entry) {
        viewModel.previewFsrsIntervals(entry)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress
        LinearProgressIndicator(
            progress = { (phase.currentIndex + 1) / phase.entries.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Review ${phase.currentIndex + 1} of ${phase.entries.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Page ${entry.sourcePage} • Word ${entry.listLabel}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3D Flip Card
        Card(
            onClick = { isFlipped = !isFlipped },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .testTag("fsrs_flashcard")
        ) {
            if (rotation <= 90f) {
                // Front
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = entry.headword,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        textAlign = TextAlign.Center
                    )

                    entry.phonetic?.let { p ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = p,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    IconButton(
                        onClick = { onSpeak(entry.answerForm) },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f))
                            .testTag("card_speak_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (entry.grammaticalLabels.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            entry.grammaticalLabels.forEach { label ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SecondaryIndigo.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = SecondaryIndigo,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tap to reveal definition, collocations & synonyms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Back
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.headword,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        TtsButton(text = entry.headword, onSpeak = onSpeak)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Definition:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = entry.primaryMeaning,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TranslationLine(entry)

                    // Dictionary Synonyms
                    if (entry.dictionarySynonyms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dictionary Synonyms:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            entry.dictionarySynonyms.forEach { syn ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = syn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Academic Collocations
                    if (entry.collocations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Academic Collocations:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        entry.collocations.take(3).forEach { coll ->
                            Text(
                                text = "• $coll",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Example in context
                    val ex = entry.exampleSentence.ifBlank { entry.dictionaryExamples.firstOrNull() ?: "" }
                    if (ex.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Context Example:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "\"$ex\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    // Learner Trap Note
                    if (entry.commonMistakes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.commonMistakes,
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningOrange,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FSRS 4 Rating Buttons
        if (!isFlipped) {
            Button(
                onClick = { isFlipped = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("show_answer_button")
            ) {
                Icon(imageVector = Icons.Default.Flip, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show Answer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                previews.forEach { preview ->
                    val (btnColor, btnText) = when (preview.rating) {
                        FsrsRating.AGAIN -> Pair(ErrorRed, "Again")
                        FsrsRating.HARD -> Pair(WarningOrange, "Hard")
                        FsrsRating.GOOD -> Pair(PrimaryBlue, "Good")
                        FsrsRating.EASY -> Pair(SuccessGreen, "Easy")
                    }

                    Button(
                        onClick = {
                            isFlipped = false
                            onRate(entry, preview.rating)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("rating_button_${preview.rating.name.lowercase()}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = btnText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = preview.intervalText,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntroCardView(
    phase: SessionPhase.IntroNew,
    onSpeak: (String) -> Unit,
    onNext: () -> Unit
) {
    val entry = phase.entries.getOrNull(phase.currentIndex) ?: return
    var isFlipped by remember(phase.currentIndex) { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 180f),
        label = "card_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { (phase.currentIndex + 1) / phase.entries.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Flashcard ${phase.currentIndex + 1} of ${phase.entries.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Page ${entry.sourcePage} • ${entry.listLabel}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            onClick = { isFlipped = !isFlipped },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .testTag("flashcard_flip_card")
        ) {
            if (rotation <= 90f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = entry.headword,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        textAlign = TextAlign.Center
                    )

                    entry.phonetic?.let { phonetic ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = phonetic,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    IconButton(
                        onClick = { onSpeak(entry.answerForm) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f))
                            .testTag("card_speak_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (entry.grammaticalLabels.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            entry.grammaticalLabels.forEach { label ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SecondaryIndigo.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = SecondaryIndigo,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Flip",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tap card to reveal definition & dictionary synonyms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.headword,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        TtsButton(text = entry.headword, onSpeak = onSpeak)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Primary Meaning:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = entry.primaryMeaning,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TranslationLine(entry)

                    if (entry.dictionarySynonyms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dictionary Synonyms:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            entry.dictionarySynonyms.forEach { syn ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = syn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    val example = entry.exampleSentence.ifBlank {
                        entry.dictionaryExamples.firstOrNull() ?: ""
                    }
                    if (example.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Example in Context:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$example\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Tap to flip back",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { isFlipped = !isFlipped },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("flip_button")
            ) {
                Icon(imageVector = Icons.Default.Flip, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Flip")
            }

            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .weight(1.5f)
                    .height(50.dp)
                    .testTag("next_card_button")
            ) {
                Text(
                    text = if (phase.currentIndex + 1 < phase.entries.size) "Next Word" else "Begin Quiz",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun QuizView(
    phase: SessionPhase.Quiz,
    combo: Int = 0,
    onGraded: (Boolean, QuizQuestion) -> Unit = { _, _ -> },
    onSpeak: (String) -> Unit,
    onAnswer: (Boolean, QuizQuestion) -> Unit,
    onEvaluateProduction: (String, QuizQuestion) -> WritingEvaluationResult? = { _, _ -> null }
) {
    val question = phase.questions.getOrNull(phase.currentIndex) ?: return
    var spellingResult by remember(phase.currentIndex) { mutableStateOf<SpellingChecker.Result?>(null) }
    var writingResult by remember(phase.currentIndex) { mutableStateOf<WritingEvaluationResult?>(null) }
    var selectedOption by remember(phase.currentIndex) { mutableStateOf<String?>(null) }
    var spellingInput by remember(phase.currentIndex) { mutableStateOf("") }
    var productionInput by remember(phase.currentIndex) { mutableStateOf("") }
    var hasAnswered by remember(phase.currentIndex) { mutableStateOf(false) }
    var isCorrectAnswer by remember(phase.currentIndex) { mutableStateOf(false) }

    // Reward the instant an answer is checked: immediate feedback is what makes it feel good
    LaunchedEffect(phase.currentIndex, hasAnswered) {
        if (hasAnswered) onGraded(isCorrectAnswer, question)
    }

    LaunchedEffect(phase.currentIndex) {
        if (question.direction == LearningDirection.LISTENING) {
            onSpeak(question.targetWord)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AnimatedProgressBar(
            progress = (phase.currentIndex + 1) / phase.questions.size.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PrimaryBlue.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = question.direction.displayName,
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ComboMeter(combo = combo)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${phase.currentIndex + 1} / ${phase.questions.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        key(phase.currentIndex) {
        AnimatedEntrance {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Prompt",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = {
                            if (question.direction == LearningDirection.LISTENING) {
                                onSpeak(question.targetWord)
                            } else {
                                onSpeak(question.prompt)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            question.direction == LearningDirection.SPELLING -> {
                OutlinedTextField(
                    value = spellingInput,
                    onValueChange = { if (!hasAnswered) spellingInput = it },
                    label = { Text("Type the correct spelling") },
                    singleLine = true,
                    enabled = !hasAnswered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .answerFeedback(
                            correctTrigger = if (hasAnswered && isCorrectAnswer) 1 else 0,
                            wrongTrigger = if (hasAnswered && !isCorrectAnswer) 1 else 0
                        )
                        .testTag("spelling_input")
                )

                if (!hasAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val result = SpellingChecker.check(spellingInput, question.correctAnswer)
                            spellingResult = result
                            hasAnswered = true
                            isCorrectAnswer = result.isAccepted
                        },
                        enabled = spellingInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_spelling_button")
                    ) {
                        Text("Check Spelling")
                    }
                }
            }

            question.direction == LearningDirection.PRODUCTION -> {
                OutlinedTextField(
                    value = productionInput,
                    onValueChange = { if (!hasAnswered) productionInput = it },
                    label = { Text("Write your sentence using '${question.correctAnswer}'") },
                    minLines = 3,
                    enabled = !hasAnswered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .answerFeedback(
                            correctTrigger = if (hasAnswered && isCorrectAnswer) 1 else 0,
                            wrongTrigger = if (hasAnswered && !isCorrectAnswer) 1 else 0
                        )
                        .testTag("production_input")
                )

                if (!hasAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val evaluation = onEvaluateProduction(productionInput, question)
                            writingResult = evaluation
                            hasAnswered = true
                            // Strong sentences count: the word (or a family form) used well enough to score 3+/5
                            isCorrectAnswer = evaluation?.let { it.isTargetWordPresent && it.estimatedAcademicScore >= 3 }
                                ?: productionInput.lowercase().contains(question.correctAnswer.lowercase())
                        },
                        enabled = productionInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_production_button")
                    ) {
                        Text("Review Sentence")
                    }
                }
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.options.forEachIndexed { index, option ->
                        val isSelected = selectedOption == option
                        val isTarget = option.equals(question.correctAnswer, ignoreCase = true)

                        val (targetCardColor, borderColor) = when {
                            !hasAnswered -> if (isSelected) {
                                Pair(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue)
                            } else {
                                Pair(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), Color.Transparent)
                            }
                            isTarget -> Pair(SuccessGreen.copy(alpha = 0.2f), SuccessGreen)
                            isSelected && !isCorrectAnswer -> Pair(ErrorRed.copy(alpha = 0.2f), ErrorRed)
                            else -> Pair(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), Color.Transparent)
                        }
                        val cardColor by animateColorAsState(targetCardColor, tween(300), label = "optionColor")

                        Card(
                            onClick = {
                                if (!hasAnswered) {
                                    selectedOption = option
                                    hasAnswered = true
                                    isCorrectAnswer = option.equals(question.correctAnswer, ignoreCase = true)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = if (borderColor != Color.Transparent) BorderStroke(2.dp, borderColor) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .answerFeedback(
                                    correctTrigger = if (hasAnswered && isTarget) 1 else 0,
                                    wrongTrigger = if (hasAnswered && isSelected && !isCorrectAnswer) 1 else 0
                                )
                                .testTag("quiz_option_$index")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected || (hasAnswered && isTarget)) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )

                                if (hasAnswered) {
                                    if (isTarget) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = SuccessGreen
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Incorrect",
                                            tint = ErrorRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = hasAnswered,
            enter = fadeIn(tween(220)) + expandVertically(Motion.smooth()) + scaleIn(initialScale = 0.85f, animationSpec = Motion.liquid())
        ) {
            Column(modifier = Modifier.padding(top = 20.dp)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrectAnswer) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCorrectAnswer) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isCorrectAnswer) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCorrectAnswer) praiseFor(combo, phase.currentIndex) else listOf(
                                    "Almost! Mistakes make memories stick",
                                    "Not quite — this one will stick now",
                                    "Good try! Check the answer below"
                                )[phase.currentIndex % 3],
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrectAnswer) SuccessGreen else ErrorRed
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        spellingResult?.let { result ->
                            Text(result.feedback, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            if (result.verdict != SpellingChecker.Verdict.EXACT) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        append("Correct spelling: ")
                                        result.highlightedAnswer.forEach { (segment, wrong) ->
                                            if (wrong) {
                                                withStyle(SpanStyle(color = ErrorRed, fontWeight = FontWeight.ExtraBold, textDecoration = TextDecoration.Underline)) { append(segment) }
                                            } else {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(segment) }
                                            }
                                        }
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        writingResult?.let { eval ->
                            Text(
                                text = "Writing score: " + "★".repeat(eval.estimatedAcademicScore) + "☆".repeat(5 - eval.estimatedAcademicScore),
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentGold
                            )
                            eval.strengths.take(2).forEach {
                                Text("✓ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            eval.suggestions.take(3).forEach {
                                Text("→ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onAnswer(isCorrectAnswer, question)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quiz_continue_button")
                ) {
                    Text(
                        text = if (phase.currentIndex + 1 < phase.questions.size) "Continue" else "See Results",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun MistakeReviewView(
    phase: SessionPhase.MistakeReview,
    onSpeak: (String) -> Unit,
    onNext: (QuizQuestion) -> Unit
) {
    val question = phase.questions.getOrNull(phase.currentIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(WarningOrange.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Targeted Reinforcement (${phase.currentIndex + 1}/${phase.questions.size})",
                color = WarningOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Prompt:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Correct Target:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = question.correctAnswer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    TtsButton(text = question.correctAnswer, onSpeak = onSpeak)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Explanation:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onNext(question) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("mistake_got_it_button")
        ) {
            Text("Got It! Next", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompletedView(
    phase: SessionPhase.Completed,
    stats: com.example.ui.viewmodel.SessionStats,
    onFinish: () -> Unit
) {
    val accuracy = if (phase.totalQuestions > 0) phase.correctAnswers.toFloat() / phase.totalQuestions else 1f
    val scorePct = (accuracy * 100).toInt()

    // Staged reveal: trophy springs in, then the ring fills and the XP counts up
    val trophyScale = remember { Animatable(0f) }
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        trophyScale.animateTo(1f, Motion.jelly())
        revealed = true
    }
    val ring by animateFloatAsState(
        targetValue = if (revealed) accuracy else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 40f),
        label = "accuracyRing"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(170.dp)) {
                LiquidBlobs(
                    colors = listOf(PrimaryBlue, SuccessGreen, AccentGold),
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = trophyScale.value.coerceIn(0f, 1f) }.clip(CircleShape),
                    periodMillis = 7000
                )
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                Canvas(modifier = Modifier.size(150.dp)) {
                    val stroke = 12.dp.toPx()
                    drawArc(trackColor, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(
                        brush = Brush.sweepGradient(listOf(SuccessGreen, PrimaryBlue, SuccessGreen)),
                        startAngle = -90f,
                        sweepAngle = 360f * ring,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = if (scorePct >= 100) "🏆" else if (scorePct >= 80) "🎉" else "💪",
                    fontSize = 56.sp,
                    modifier = Modifier.graphicsLayer { scaleX = trophyScale.value; scaleY = trophyScale.value }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    scorePct >= 100 -> "Flawless session!"
                    scorePct >= 80 -> "Session complete!"
                    else -> "Session complete — every rep counts!"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (phase.srsReviewed > 0) {
                    "${phase.srsReviewed} words reviewed using FSRS memory intervals. Your retention profile has been updated."
                } else {
                    "Your practice scores and spaced repetition intervals have been updated."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryTile("XP earned", if (revealed) stats.xp else 0, "", AccentGold, Modifier.weight(1f), index = 0)
                SummaryTile("Accuracy", if (revealed) scorePct else 0, "%", SuccessGreen, Modifier.weight(1f), index = 1)
                SummaryTile("Best combo", if (revealed) stats.bestCombo else 0, "", WarningOrange, Modifier.weight(1f), index = 2)
            }
            if (stats.luckyHits > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "🍀 ${stats.luckyHits} lucky bonus${if (stats.luckyHits > 1) "es" else ""} this session",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentGold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("finish_session_button")
            ) {
                Text("Back to Dashboard", fontWeight = FontWeight.Bold)
            }
        }

        ConfettiBurst(trigger = if (revealed) phase else null, modifier = Modifier.fillMaxSize(), particleCount = 120)
    }
}

@Composable
private fun SummaryTile(label: String, value: Int, suffix: String, color: Color, modifier: Modifier, index: Int) {
    AnimatedEntrance(index = index + 2, modifier = modifier) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                AnimatedCounter(
                    value = value,
                    suffix = suffix,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = color
                )
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Varied, combo-aware praise: the bigger the streak, the bigger the cheer. */
private fun praiseFor(combo: Int, index: Int): String = when {
    combo >= 10 -> "Unstoppable! $combo in a row 🔥"
    combo >= 5 -> "On fire! $combo in a row 🔥"
    combo >= 3 -> "Hat trick! ⚡"
    else -> listOf("Correct!", "Great job!", "Nailed it!", "Well done!", "Brilliant!", "Spot on!")[index % 6]
}
