package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FreshMint
import com.example.ui.theme.InkText
import com.example.ui.theme.SunnyAmber
import com.example.util.Badge
import com.example.util.RewardEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Global reward layer drawn above every screen: a "+XP" chip that floats up and fades,
 * and a banner that slides down when a badge is unlocked.
 */
@Composable
fun RewardOverlay(event: RewardEvent?, onConsumed: () -> Unit) {
    var xpEvent by remember { mutableStateOf<RewardEvent?>(null) }
    var badge by remember { mutableStateOf<Badge?>(null) }
    val rise = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(event?.id) {
        val e = event ?: return@LaunchedEffect
        e.newBadges.firstOrNull()?.let { badge = it }
        if (e.xp > 0) {
            xpEvent = e
            rise.snapTo(0f)
            alpha.snapTo(1f)
            launch { rise.animateTo(-60f, tween(900)) }
            delay(500)
            alpha.animateTo(0f, tween(400))
        }
        // Clear only after the animation, since clearing restarts this effect
        onConsumed()
    }
    LaunchedEffect(badge) {
        if (badge != null) {
            delay(2800)
            badge = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        xpEvent?.let { e ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .graphicsLayer { translationY = rise.value; this.alpha = alpha.value }
                    .shadow(6.dp, RoundedCornerShape(50))
                    .background(FreshMint, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("+${e.xp} XP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }

        AnimatedVisibility(
            visible = badge != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp)
        ) {
            val b = badge
            if (b != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .background(SunnyAmber, RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(b.emoji, fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Badge unlocked!", color = InkText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(b.title, color = InkText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
