package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizQuestion
import com.example.ui.components.AnimatedCounter
import com.example.ui.components.AnimatedProgressBar
import com.example.ui.components.ComboMeter
import com.example.ui.components.ConfettiBurst
import com.example.ui.components.LiquidBlobs
import com.example.ui.components.answerFeedback
import com.example.ui.components.bouncyClickable
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.FreshMint
import com.example.ui.theme.Lavender
import com.example.ui.theme.Motion
import com.example.ui.theme.SoftRose
import com.example.ui.theme.SunnyAmber
import com.example.ui.theme.WarmCoral
import com.example.ui.viewmodel.VocabViewModel
import kotlinx.coroutines.delay

private enum class ChallengeState { READY, PLAYING, RESULTS }

private const val CHALLENGE_MILLIS = 60_000L

/** Answer as many word → meaning questions as possible in 60 seconds. Combos and XP still apply. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimedChallengeScreen(viewModel: VocabViewModel, onNavigateBack: () -> Unit) {
    val best by viewModel.bestChallenge.collectAsState()
    val combo by viewModel.combo.collectAsState()
    var state by remember { mutableStateOf(ChallengeState.READY) }
    var score by remember { mutableIntStateOf(0) }
    var answered by remember { mutableIntStateOf(0) }
    var remaining by remember { mutableLongStateOf(CHALLENGE_MILLIS) }
    var question by remember { mutableStateOf<QuizQuestion?>(null) }
    var picked by remember { mutableStateOf<String?>(null) }
    var newRecord by remember { mutableStateOf(false) }
    var previousBest by remember { mutableIntStateOf(0) }

    fun start() {
        score = 0; answered = 0; remaining = CHALLENGE_MILLIS; picked = null; newRecord = false
        previousBest = best
        viewModel.startChallenge()
        question = viewModel.nextChallengeQuestion()
        state = ChallengeState.PLAYING
    }

    // Countdown driven by frame time, so it stays accurate even if frames drop
    LaunchedEffect(state) {
        if (state != ChallengeState.PLAYING) return@LaunchedEffect
        val startedAt = withFrameMillis { it }
        while (remaining > 0) {
            val now = withFrameMillis { it }
            remaining = (CHALLENGE_MILLIS - (now - startedAt)).coerceAtLeast(0)
        }
        newRecord = viewModel.finishChallenge(score)
        state = ChallengeState.RESULTS
    }

    // Brief reveal, then the next question
    LaunchedEffect(picked) {
        if (picked != null && state == ChallengeState.PLAYING) {
            delay(380)
            question = viewModel.nextChallengeQuestion()
            picked = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("60-Second Challenge", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ChallengeState.READY -> ReadyView(best = best, onStart = ::start)
                ChallengeState.PLAYING -> {
                    val q = question
                    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val seconds = ((remaining + 999) / 1000).toInt()
                            Text(
                                "⏱ ${seconds}s",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = if (seconds <= 10) SoftRose else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.weight(1f))
                            ComboMeter(combo = combo)
                            Spacer(Modifier.width(10.dp))
                            Text("Score $score", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BrandIndigo)
                        }
                        Spacer(Modifier.height(10.dp))
                        AnimatedProgressBar(
                            progress = remaining / CHALLENGE_MILLIS.toFloat(),
                            color = if (remaining <= 10_000) SoftRose else FreshMint,
                            height = 12
                        )
                        Spacer(Modifier.height(24.dp))
                        if (q == null) {
                            Text("Not enough words in this collection for the challenge.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            AnimatedContent(
                                targetState = q,
                                transitionSpec = {
                                    (slideInHorizontally(Motion.liquid()) { it / 2 } + fadeIn(tween(150))) togetherWith
                                        (slideOutHorizontally(tween(160)) { -it / 2 } + fadeOut(tween(120)))
                                },
                                label = "challengeQuestion"
                            ) { current ->
                                ChallengeQuestionView(
                                    question = current,
                                    picked = if (current == q) picked else null,
                                    onPick = { option ->
                                        if (picked == null) {
                                            val correct = option.equals(current.correctAnswer, ignoreCase = true)
                                            picked = option
                                            answered++
                                            if (correct) score++
                                            viewModel.answerChallenge(current, correct)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                ChallengeState.RESULTS -> ResultsView(
                    score = score,
                    answered = answered,
                    best = best,
                    previousBest = previousBest,
                    newRecord = newRecord,
                    onAgain = ::start,
                    onDone = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun ReadyView(best: Int, onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
            LiquidBlobs(colors = listOf(WarmCoral, SunnyAmber, Lavender), modifier = Modifier.fillMaxSize(), periodMillis = 6000)
            Text("⚡", fontSize = 80.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("How many can you get in 60 seconds?", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Pick the right meaning for each word. Streaks build combos for bonus XP.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        if (best > 0) Text("Personal best: $best", fontWeight = FontWeight.Bold, color = BrandIndigo, fontSize = 16.sp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = WarmCoral),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().height(58.dp)
        ) {
            Text("Start", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun ChallengeQuestionView(question: QuizQuestion, picked: String?, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(BrandIndigo, Lavender))).padding(vertical = 28.dp, horizontal = 20.dp)) {
                Text(
                    question.targetWord,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        question.options.forEach { option ->
            val isCorrect = option.equals(question.correctAnswer, ignoreCase = true)
            val target = when {
                picked == null -> MaterialTheme.colorScheme.surface
                isCorrect -> FreshMint.copy(alpha = 0.18f)
                option == picked -> SoftRose.copy(alpha = 0.18f)
                else -> MaterialTheme.colorScheme.surface
            }
            val bg by animateColorAsState(target, tween(150), label = "challengeOption")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                border = BorderStroke(
                    1.5.dp,
                    when {
                        picked != null && isCorrect -> FreshMint
                        picked == option -> SoftRose
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .answerFeedback(
                        correctTrigger = if (picked == option && isCorrect) 1 else 0,
                        wrongTrigger = if (picked == option && !isCorrect) 1 else 0
                    )
                    .bouncyClickable { onPick(option) }
            ) {
                Text(option, modifier = Modifier.padding(16.dp), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ResultsView(score: Int, answered: Int, best: Int, previousBest: Int, newRecord: Boolean, onAgain: () -> Unit, onDone: () -> Unit) {
    val firstScore = previousBest == 0 && score > 0
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                when {
                    newRecord -> "🏆 New personal best!"
                    firstScore -> "🎉 Your first score!"
                    score >= previousBest - 2 && score > 0 -> "So close to your best!"
                    else -> "Time's up!"
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            var shown by remember { mutableIntStateOf(0) }
            LaunchedEffect(Unit) { shown = score }
            AnimatedCounter(
                value = shown,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 84.sp, fontWeight = FontWeight.Black),
                color = BrandIndigo
            )
            Text("correct in 60 seconds", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            val accuracy = if (answered > 0) score * 100 / answered else 0
            Text("Accuracy $accuracy%  ·  Best $best", fontWeight = FontWeight.SemiBold)
            if (!newRecord && previousBest > 0 && score < previousBest) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "${previousBest - score + 1} more to beat your record",
                    color = WarmCoral,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onAgain,
                colors = ButtonDefaults.buttonColors(containerColor = WarmCoral),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Play again", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp) }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Done", fontWeight = FontWeight.Bold) }
        }
        ConfettiBurst(trigger = if (newRecord || firstScore) score else null, modifier = Modifier.fillMaxSize(), particleCount = 130)
    }
}
