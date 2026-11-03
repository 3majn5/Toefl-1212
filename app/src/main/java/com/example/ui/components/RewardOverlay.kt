package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.FreshMint
import com.example.ui.theme.InkText
import com.example.ui.theme.Lavender
import com.example.ui.theme.Motion
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyAmber
import com.example.ui.theme.WarmCoral
import com.example.util.Badge
import com.example.util.FeedbackFx
import com.example.util.QuestType
import com.example.util.RewardEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Global reward layer drawn above every screen:
 * - a "+XP" chip that springs up (gold with a 🍀 when a lucky bonus hits, with the combo multiplier),
 * - quest-complete and streak-freeze toasts,
 * - badge banners, queued so several unlocks each get their moment,
 * - a full-screen level-up celebration with confetti.
 */
@Composable
fun RewardOverlay(event: RewardEvent?, onConsumed: () -> Unit) {
    var xpEvent by remember { mutableStateOf<RewardEvent?>(null) }
    val badges = remember { mutableStateListOf<Badge>() }
    var toast by remember { mutableStateOf<String?>(null) }
    var levelUp by remember { mutableStateOf<Int?>(null) }
    var confetti by remember { mutableStateOf<Long?>(null) }
    val rise = remember { Animatable(0f) }
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(event?.id) {
        val e = event ?: return@LaunchedEffect
        badges += e.newBadges
        when {
            e.levelUp != null -> {
                levelUp = e.levelUp
                confetti = e.id
                FeedbackFx.fanfare()
            }
            e.newBadges.isNotEmpty() || e.completedQuests.isNotEmpty() -> FeedbackFx.sparkle()
        }
        if (e.newBadges.isNotEmpty() && e.levelUp == null) confetti = e.id
        toast = when {
            e.completedQuests.isNotEmpty() -> questToast(e.completedQuests)
            e.freezeUsed -> "🧊 A streak freeze saved your streak!"
            e.freezeEarned -> "🧊 Streak freeze earned — it protects you on a missed day"
            else -> toast
        }
        if (e.xp > 0) {
            xpEvent = e
            rise.snapTo(0f)
            alpha.snapTo(1f)
            scale.snapTo(0.3f)
            launch { scale.animateTo(1f, Motion.jelly()) }
            launch { rise.animateTo(-110f, Motion.smooth()) }
            delay(if (e.lucky || e.multiplier > 1f) 900 else 650)
            alpha.animateTo(0f, tween(350))
        }
        // Clear only after the animation, since clearing restarts this effect
        onConsumed()
    }
    LaunchedEffect(badges.firstOrNull()) {
        if (badges.isNotEmpty()) {
            delay(2600)
            badges.removeAt(0)
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2600)
            toast = null
        }
    }
    LaunchedEffect(levelUp) {
        if (levelUp != null) {
            delay(3800)
            levelUp = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        xpEvent?.let { e ->
            val background = when {
                e.lucky -> Brush.horizontalGradient(listOf(SunnyAmber, WarmCoral))
                e.multiplier > 1f -> Brush.horizontalGradient(listOf(WarmCoral, Lavender))
                else -> Brush.horizontalGradient(listOf(FreshMint, SkyBlue))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .graphicsLayer {
                        translationY = rise.value
                        this.alpha = alpha.value
                        scaleX = scale.value
                        scaleY = scale.value
                    }
                    .shadow(10.dp, RoundedCornerShape(50))
                    .background(background, RoundedCornerShape(50))
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (e.lucky) {
                        Text("🍀 LUCKY ×2", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        Spacer(Modifier.width(8.dp))
                    } else if (e.multiplier > 1f) {
                        Text("🔥 ${e.combo} combo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("+${e.xp} XP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                }
            }
        }

        // Badge banner (top)
        AnimatedVisibility(
            visible = badges.isNotEmpty(),
            enter = slideInVertically(Motion.liquid()) { -it * 2 } + fadeIn(),
            exit = slideOutVertically { -it * 2 } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp)
        ) {
            val b = badges.firstOrNull()
            if (b != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(SunnyAmber, Color(0xFFFFD36E))))
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(b.emoji, fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Badge unlocked!", color = InkText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(b.title, color = InkText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Quest / freeze toast (above the XP chip)
        AnimatedVisibility(
            visible = toast != null,
            enter = scaleIn(Motion.liquid(), initialScale = 0.6f) + fadeIn(),
            exit = scaleOut(targetScale = 0.8f) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 190.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = toast.orEmpty(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .shadow(10.dp, RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(BrandIndigo, Lavender)), RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            )
        }

        // Level-up celebration
        AnimatedVisibility(visible = levelUp != null, enter = fadeIn(tween(250)), exit = fadeOut(tween(300))) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { levelUp = null }
            ) {
                LevelUpCard(level = levelUp ?: 1)
            }
        }

        ConfettiBurst(trigger = confetti, modifier = Modifier.fillMaxSize(), origin = Offset(0.5f, 0.4f))
    }
}

@Composable
private fun LevelUpCard(level: Int) {
    val cardScale = remember { Animatable(0.4f) }
    val numberScale = remember { Animatable(0f) }
    LaunchedEffect(level) {
        launch { cardScale.animateTo(1f, Motion.liquid()) }
        delay(180)
        numberScale.animateTo(1f, Motion.jelly())
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .graphicsLayer { scaleX = cardScale.value; scaleY = cardScale.value }
            .shadow(24.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.linearGradient(listOf(BrandIndigo, Lavender)))
    ) {
        LiquidBlobs(
            colors = listOf(WarmCoral, SunnyAmber, FreshMint),
            modifier = Modifier.matchParentSize(),
            periodMillis = 6000
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 44.dp, vertical = 32.dp)
        ) {
            Text("LEVEL UP!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "$level",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 84.sp,
                modifier = Modifier.graphicsLayer { scaleX = numberScale.value; scaleY = numberScale.value }
            )
            Text(levelTitle(level), color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(14.dp))
            Text("Tap to continue", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

private fun levelTitle(level: Int): String = when {
    level >= 30 -> "Vocabulary Virtuoso"
    level >= 20 -> "Academic Ace"
    level >= 12 -> "Word Wizard"
    level >= 7 -> "Rising Scholar"
    level >= 4 -> "Keen Learner"
    else -> "Getting Started"
}

private fun questToast(quests: List<QuestType>): String =
    if (quests.size == 1) "${quests[0].emoji} Quest complete: ${quests[0].title}  +25 XP"
    else "🗺️ ${quests.size} quests complete!  +${quests.size * 25} XP"
