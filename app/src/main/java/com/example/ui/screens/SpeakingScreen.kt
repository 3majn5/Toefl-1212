package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.StudyStatus
import com.example.data.model.VocabEntry
import com.example.ui.components.AnimatedEntrance
import com.example.ui.components.AnimatedProgressBar
import com.example.ui.components.LiquidBlobs
import com.example.ui.components.bouncyClickable
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.FreshMint
import com.example.ui.theme.Lavender
import com.example.ui.theme.Motion
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftRose
import com.example.ui.theme.SunnyAmber
import com.example.ui.theme.WarmCoral
import com.example.ui.viewmodel.VocabViewModel
import com.example.util.SpeakingCoach
import com.example.util.SpeakingFeedback
import com.example.util.SpeechListener
import com.example.util.rememberSpeechListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingScreen(viewModel: VocabViewModel, onNavigateBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val listener = rememberSpeechListener()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Speaking Studio", fontWeight = FontWeight.Bold)
                        Text("TOEFL speaking tasks & pronunciation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { listener.stop(); tab = 0 }, text = { Text("Speaking task", fontWeight = FontWeight.SemiBold) })
                Tab(selected = tab == 1, onClick = { listener.stop(); tab = 1 }, text = { Text("Pronunciation", fontWeight = FontWeight.SemiBold) })
            }
            MicPermissionGate {
                when (tab) {
                    0 -> SpeakingTask(viewModel, listener)
                    else -> PronunciationDrill(viewModel, listener)
                }
            }
        }
    }
}

/** Asks for the microphone once; content shows either way (typing fallback if denied). */
@Composable
private fun MicPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { asked = true }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!granted && !asked) launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
    // Re-create the content once the permission answer arrives so it re-checks access
    androidx.compose.runtime.key(asked) { content() }
}

private fun micGranted(context: android.content.Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private enum class TaskPhase { IDLE, PREP, SPEAKING, REVIEW }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeakingTask(viewModel: VocabViewModel, listener: SpeechListener) {
    val context = LocalContext.current
    val entries by viewModel.allEntries.collectAsState()
    var promptIndex by rememberSaveable { mutableIntStateOf((System.currentTimeMillis() / 86_400_000L % SpeakingCoach.PROMPTS.size).toInt()) }
    val prompt = SpeakingCoach.PROMPTS[promptIndex % SpeakingCoach.PROMPTS.size]
    val targets = remember(promptIndex, entries.size) { SpeakingCoach.pickTargetWords(entries, seed = promptIndex * 31 + entries.size) }
    var phase by remember(promptIndex) { mutableStateOf(TaskPhase.IDLE) }
    var secondsLeft by remember(promptIndex) { mutableIntStateOf(0) }
    var spokenSeconds by remember(promptIndex) { mutableIntStateOf(0) }
    var feedback by remember(promptIndex) { mutableStateOf<SpeakingFeedback?>(null) }
    var typed by remember(promptIndex) { mutableStateOf("") }
    var finishing by remember(promptIndex) { mutableStateOf(false) }
    var typeInstead by rememberSaveable { mutableStateOf(false) }
    val canSpeak = listener.isAvailable && micGranted(context) && !typeInstead
    val scope = rememberCoroutineScope()

    fun finish() {
        if (phase != TaskPhase.SPEAKING || finishing) return
        finishing = true
        listener.stop()
        scope.launch {
            delay(900) // the recognizer delivers its last segment shortly after stopping
            val transcript = if (canSpeak) listener.transcript else typed
            val result = SpeakingCoach.analyze(transcript, targets, if (canSpeak) spokenSeconds.coerceAtLeast(1) else 0)
            feedback = result
            viewModel.rewardSpeaking(result)
            phase = TaskPhase.REVIEW
            finishing = false
        }
    }

    LaunchedEffect(phase) {
        when (phase) {
            TaskPhase.PREP -> {
                secondsLeft = prompt.prepSeconds
                while (secondsLeft > 0) { delay(1000); secondsLeft-- }
                phase = TaskPhase.SPEAKING
            }
            TaskPhase.SPEAKING -> {
                listener.reset()
                if (canSpeak) listener.startContinuous()
                secondsLeft = prompt.speakSeconds
                spokenSeconds = 0
                while (secondsLeft > 0 && !finishing) { delay(1000); if (!finishing) { secondsLeft--; spokenSeconds++ } }
                finish()
            }
            else -> {}
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AnimatedEntrance(index = 0) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.background(Brush.linearGradient(listOf(BrandIndigo, Lavender)))) {
                    LiquidBlobs(colors = listOf(WarmCoral, SkyBlue), modifier = Modifier.matchParentSize())
                    Column(Modifier.padding(20.dp)) {
                        Text("TASK ${promptIndex % SpeakingCoach.PROMPTS.size + 1} · ${prompt.prepSeconds}s prep · ${prompt.speakSeconds}s answer", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(prompt.question, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 25.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Try to use:", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            targets.forEach { t ->
                                val used = feedback?.targetWordsUsed?.contains(t.answerForm) == true
                                Text(
                                    (if (used) "✓ " else "") + t.answerForm,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .background(if (used) FreshMint else Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        when (phase) {
            TaskPhase.IDLE -> {
                if (typeInstead && listener.isAvailable && micGranted(context)) {
                    OutlinedButton(onClick = { typeInstead = false }, modifier = Modifier.fillMaxWidth()) { Text("🎙️ Switch back to speaking") }
                } else if (!canSpeak) {
                    Text(
                        if (!listener.isAvailable) "Speech recognition isn't available on this device, so type your answer instead — you'll still get feedback."
                        else "Microphone access is off, so type your answer instead. Allow the microphone in Android Settings to speak.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { feedback = null; phase = TaskPhase.PREP },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmCoral),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Start (${prompt.prepSeconds}s to prepare)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                OutlinedButton(onClick = { promptIndex++ }, modifier = Modifier.fillMaxWidth()) { Text("Different question") }
            }
            TaskPhase.PREP -> {
                Text("Prepare your answer", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Opinion → two reasons → an example. Use the target words.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                CountdownRing(secondsLeft, prompt.prepSeconds, SunnyAmber)
                OutlinedButton(onClick = { phase = TaskPhase.SPEAKING }, modifier = Modifier.fillMaxWidth()) { Text("I'm ready — start speaking") }
            }
            TaskPhase.SPEAKING -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (canSpeak) "Speak now" else "Type your answer", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    Text("${secondsLeft}s", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = if (secondsLeft <= 10) SoftRose else BrandIndigo)
                }
                AnimatedProgressBar(progress = secondsLeft / prompt.speakSeconds.toFloat(), color = if (secondsLeft <= 10) SoftRose else FreshMint, height = 12)
                if (canSpeak) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { ListeningOrb(level = listener.level, active = listener.isListening) }
                    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp)) {
                        Text(
                            listener.transcript.ifBlank { "Your words will appear here as you speak…" },
                            modifier = Modifier.padding(16.dp),
                            color = if (listener.transcript.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    listener.error?.let { Text(it, color = SoftRose, fontSize = 13.sp) }
                    OutlinedButton(
                        onClick = { listener.stop(); typeInstead = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Type instead") }
                } else {
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                        placeholder = { Text("Write what you would say…") }
                    )
                }
                Button(
                    onClick = ::finish,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Finish answer", fontWeight = FontWeight.Bold)
                }
            }
            TaskPhase.REVIEW -> feedback?.let { fb ->
                if (fb.wordCount == 0 && canSpeak) {
                    Text(
                        listener.error ?: "We didn't hear anything. Check the microphone, or type your answer instead.",
                        color = SoftRose,
                        fontSize = 14.sp
                    )
                    OutlinedButton(onClick = { typeInstead = true; feedback = null; phase = TaskPhase.SPEAKING }, modifier = Modifier.fillMaxWidth()) {
                        Text("Type my answer instead")
                    }
                }
                SpeakingFeedbackCard(fb, transcript = if (canSpeak) listener.transcript else typed)
                Button(
                    onClick = { feedback = null; phase = TaskPhase.PREP },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmCoral),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Try this question again", fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = { promptIndex++ }, modifier = Modifier.fillMaxWidth()) { Text("Next question") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeakingFeedbackCard(fb: SpeakingFeedback, transcript: String) {
    AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically(Motion.smooth())) {
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Score", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${fb.score} / 4", fontWeight = FontWeight.Black, fontSize = 28.sp, color = if (fb.score >= 3) FreshMint else if (fb.score >= 2) SunnyAmber else SoftRose)
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatChip("${fb.wordCount} words")
                    if (fb.wordsPerMinute > 0) StatChip("${fb.wordsPerMinute} wpm")
                    StatChip("${fb.fillerCount} fillers")
                    StatChip("${fb.targetWordsUsed.size}/${fb.targetWordsUsed.size + fb.targetWordsMissed.size} target words")
                }
                if (fb.strengths.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    fb.strengths.forEach { Text("✓ $it", color = FreshMint, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                }
                if (fb.tips.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    fb.tips.forEach { Text("→ $it", fontSize = 14.sp) }
                }
                if (transcript.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("What we heard", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(transcript, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun CountdownRing(secondsLeft: Int, total: Int, color: Color) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
            val progress by animateFloatAsState(secondsLeft / total.toFloat(), Motion.smooth(), label = "prepRing")
            val track = MaterialTheme.colorScheme.surfaceVariant
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawArc(track, -90f, 360f, false, style = stroke)
                drawArc(color, -90f, 360f * progress, false, style = stroke)
            }
            Text("$secondsLeft", fontSize = 44.sp, fontWeight = FontWeight.Black)
        }
    }
}

/** Mic orb that swells with voice loudness. */
@Composable
private fun ListeningOrb(level: Float, active: Boolean) {
    val scale by animateFloatAsState(if (active) 1f + level * 0.35f else 1f, Motion.liquid(), label = "orb")
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        Box(
            Modifier.size(110.dp).graphicsLayer { scaleX = scale * 1.1f; scaleY = scale * 1.1f; alpha = 0.25f }
                .clip(CircleShape).background(WarmCoral)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(84.dp).graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape).background(Brush.linearGradient(listOf(WarmCoral, SunnyAmber)))
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Listening", tint = Color.White, modifier = Modifier.size(38.dp))
        }
    }
}

// ── Pronunciation drill ──────────────────────────────────────────

@Composable
private fun PronunciationDrill(viewModel: VocabViewModel, listener: SpeechListener) {
    val entries by viewModel.allEntries.collectAsState()
    var round by rememberSaveable { mutableIntStateOf(0) }
    val words = remember(round, entries.size) {
        val single = entries.filter { !it.answerForm.contains(' ') }
        val learning = single.filter { it.studyStatus != StudyStatus.NEW }
        (if (learning.size >= 10) learning else single).shuffled().take(10)
    }
    var index by remember(round) { mutableIntStateOf(0) }
    var perfect by remember(round) { mutableIntStateOf(0) }
    val entry = words.getOrNull(index)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (entry == null) {
            Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎙️", fontSize = 64.sp)
                Text("Round complete!", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text("$perfect of ${words.size} recognised first time", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { round++ }, colors = ButtonDefaults.buttonColors(containerColor = WarmCoral), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("New round", fontWeight = FontWeight.Bold)
                }
            }
            return@Column
        }
        Text("Word ${index + 1} of ${words.size}", fontWeight = FontWeight.SemiBold, color = BrandIndigo)
        AnimatedProgressBar(progress = index / words.size.toFloat(), color = BrandIndigo, height = 8)
        PronunciationCheckCard(
            entry = entry,
            viewModel = viewModel,
            listener = listener,
            onScored = { score -> if (score == 2) perfect++ },
            onNext = { index++ }
        )
    }
}

/**
 * Say-the-word check: the recognizer's ranked guesses are compared with the target.
 * Reused on the word detail page.
 */
@Composable
fun PronunciationCheckCard(
    entry: VocabEntry,
    viewModel: VocabViewModel,
    listener: SpeechListener,
    onScored: (Int) -> Unit = {},
    onNext: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var result by remember(entry.entryNumber) { mutableStateOf<Pair<Int, String?>?>(null) }
    val canSpeak = listener.isAvailable && micGranted(context)

    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.answerForm, fontWeight = FontWeight.Black, fontSize = 30.sp)
                IconButton(onClick = { viewModel.speak(entry.answerForm) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear it", tint = BrandIndigo)
                }
            }
            Text(entry.primaryMeaning, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, fontSize = 13.sp, maxLines = 2)
            Spacer(Modifier.height(16.dp))
            if (!canSpeak) {
                Text(
                    if (!listener.isAvailable) "Speech recognition isn't available on this device." else "Allow microphone access to check your pronunciation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.bouncyClickable {
                        if (!listener.isListening) {
                            result = null
                            listener.startSingle { guesses ->
                                val score = SpeakingCoach.pronunciationScore(entry.answerForm, guesses)
                                result = score to guesses.firstOrNull()
                                if (guesses.isNotEmpty()) {
                                    viewModel.rewardPronunciation(entry, score)
                                    onScored(score)
                                }
                            }
                        }
                    }
                ) {
                    ListeningOrb(level = listener.level, active = listener.isListening)
                }
                Text(if (listener.isListening) "Listening… say the word" else "Tap and say the word", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listener.error?.takeIf { result?.second == null && !listener.isListening }?.let {
                    Spacer(Modifier.height(4.dp)); Text(it, color = SoftRose, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
            result?.let { (score, heard) ->
                if (heard == null) return@let
                Spacer(Modifier.height(12.dp))
                Text(
                    when (score) {
                        2 -> "🎯 Perfect — clearly recognised!"
                        1 -> "👍 Close — recognised on a second guess"
                        else -> "We heard \"$heard\". Listen and try again."
                    },
                    fontWeight = FontWeight.Bold,
                    color = when (score) { 2 -> FreshMint; 1 -> SunnyAmber; else -> SoftRose },
                    textAlign = TextAlign.Center
                )
            }
            if (onNext != null) {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text(if (result == null) "Skip" else "Next word") }
            }
        }
    }
}
