package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.example.data.model.CollectionScope
import com.example.ui.theme.AwlColor
import com.example.ui.theme.ToeflColor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@Composable
fun StatusBadge(status: StudyStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        StudyStatus.NEW -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "NEW")
        StudyStatus.LEARNING -> Triple(AccentGold.copy(alpha = 0.15f), AccentGold, "LEARNING")
        StudyStatus.REVIEWING -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "REVIEWING")
        StudyStatus.MASTERED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "MASTERED")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VerificationBadge(status: UserVerificationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        UserVerificationStatus.VERIFIED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Verified")
        UserVerificationStatus.UNVERIFIED -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, "Unverified")
        UserVerificationStatus.QUESTIONABLE -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "Questionable")
        UserVerificationStatus.CORRECTED -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "Corrected")
        UserVerificationStatus.NEEDS_EXPLANATION -> Triple(ErrorRed.copy(alpha = 0.15f), ErrorRed, "Needs Review")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AnomalyBadge(notes: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(WarningOrange.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "PDF Source Anomaly",
            tint = WarningOrange,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Flag: $notes",
            color = WarningOrange,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TtsButton(
    text: String,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 20
) {
    IconButton(
        onClick = { onSpeak(text) },
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Listen to pronunciation",
            tint = PrimaryBlue,
            modifier = Modifier.size(size.dp)
        )
    }
}


// ── Animation building blocks ───────────────────────────────────────

/** Gently shrinks while pressed and springs back — makes tappable cards feel alive. */
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Clickable with a bouncy press animation. */
fun Modifier.bouncyClickable(onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    this
        .pressScale(source)
        .clickable(interactionSource = source, indication = androidx.compose.material3.ripple(), onClick = onClick)
}

/**
 * Fades and slides content up the first time it appears. [index] staggers
 * consecutive items so lists and dashboards cascade in.
 */
@Composable
fun AnimatedEntrance(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(420, delayMillis = (index * 60).coerceAtMost(480))) +
            slideInVertically(
                animationSpec = tween(420, delayMillis = (index * 60).coerceAtMost(480), easing = FastOutSlowInEasing),
                initialOffsetY = { it / 4 }
            )
    ) {
        content()
    }
}

/** A rounded progress bar that animates smoothly to its new value. */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Int = 10
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "progress"
    )
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/** Number that counts up/down to its target value. */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = Color.Unspecified,
    suffix: String = ""
) {
    val animated by animateIntAsState(targetValue = value, animationSpec = tween(800), label = "counter")
    Text(text = "$animated$suffix", modifier = modifier, style = style, color = color)
}

/** Softly pulsing dot/badge used to draw the eye to due reviews. */
@Composable
fun PulsingDot(color: Color, modifier: Modifier = Modifier, size: Int = 10) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseScale"
    )
    Box(
        modifier = modifier
            .size(size.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = 1.4f - scale * 0.5f }
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Feedback animation for quiz answers: shakes horizontally when [wrongTrigger]
 * changes to a new value and pops when [correctTrigger] does.
 */
fun Modifier.answerFeedback(correctTrigger: Int, wrongTrigger: Int): Modifier = composed {
    val shake = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(wrongTrigger) {
        if (wrongTrigger > 0) {
            for (offset in listOf(14f, -12f, 9f, -6f, 3f, 0f)) {
                shake.animateTo(offset, tween(45))
            }
        }
    }
    LaunchedEffect(correctTrigger) {
        if (correctTrigger > 0) {
            pop.animateTo(1.05f, tween(110))
            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    graphicsLayer {
        translationX = shake.value
        scaleX = pop.value
        scaleY = pop.value
    }
}

/**
 * Segmented switcher between TOEFL 1212, AWL 570 and both lists.
 * The highlight slides between segments and recolors to the collection's identity color.
 */
@Composable
fun CollectionSwitcher(
    selected: CollectionScope,
    counts: Map<CollectionScope, Int>,
    onSelect: (CollectionScope) -> Unit,
    modifier: Modifier = Modifier
) {
    val scopes = CollectionScope.values()
    val selectedIndex = scopes.indexOf(selected)
    val indicatorColor by animateColorAsState(
        targetValue = when (selected) {
            CollectionScope.TOEFL -> ToeflColor
            CollectionScope.AWL -> AwlColor
            CollectionScope.ALL -> com.example.ui.theme.WarmCoral
        },
        animationSpec = tween(350),
        label = "switcherColor"
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / scopes.size
        val segmentHeight = maxHeight
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            label = "switcherOffset"
        )
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .size(width = segmentWidth, height = segmentHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(indicatorColor)
        )
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            scopes.forEach { scope ->
                val isSelected = scope == selected
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(250),
                    label = "switcherText"
                )
                Box(
                    modifier = Modifier
                        .size(width = segmentWidth, height = segmentHeight)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(scope) },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = scope.shortName,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        counts[scope]?.takeIf { it > 0 }?.let {
                            Text(
                                text = "$it words",
                                color = textColor.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Small colored tag showing which list a word comes from. */
@Composable
fun CollectionTag(entry: VocabEntry, modifier: Modifier = Modifier) {
    val color = if (entry.isAwl) AwlColor else ToeflColor
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (entry.isAwl) "AWL" else "TOEFL",
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
