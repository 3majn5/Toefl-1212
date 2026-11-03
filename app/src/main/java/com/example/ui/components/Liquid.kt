package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.FreshMint
import com.example.ui.theme.Lavender
import com.example.ui.theme.Motion
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftRose
import com.example.ui.theme.SunnyAmber
import com.example.ui.theme.WarmCoral
import com.example.util.GamificationManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Soft color blobs that drift slowly on looping paths, like ink in water.
 * Drawn behind hero cards; cheap enough to run continuously (one Canvas, a few radial gradients).
 */
@Composable
fun LiquidBlobs(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    periodMillis: Int = 14_000
) {
    val t by rememberInfiniteTransition(label = "blobs").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing)),
        label = "blobPhase"
    )
    Canvas(modifier = modifier) {
        colors.forEachIndexed { i, color ->
            val phase = t + i * 2.1f
            val cx = size.width * (0.5f + 0.38f * sin(phase * (1 + i % 2)))
            val cy = size.height * (0.5f + 0.42f * cos(phase * 0.8f + i))
            val radius = size.maxDimension * (0.42f + 0.08f * sin(phase * 1.3f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0f)),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }
    }
}

private class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var rotation: Float, val spin: Float,
    val color: Color, val w: Float, val h: Float, val circle: Boolean
)

/**
 * Physics confetti: particles launch upward from [origin] (fractions of the canvas), then
 * fall with gravity and air drag while spinning. A new [trigger] value fires a new burst.
 */
@Composable
fun ConfettiBurst(
    trigger: Any?,
    modifier: Modifier = Modifier,
    particleCount: Int = 90,
    origin: Offset = Offset(0.5f, 0.35f)
) {
    val palette = listOf(BrandIndigo, WarmCoral, FreshMint, SunnyAmber, Lavender, SkyBlue, SoftRose)
    val particles = remember(trigger) { mutableListOf<Particle>() }
    val frame = remember { mutableLongStateOf(0L) }
    val canvasSize = remember { floatArrayOf(0f, 0f) }

    LaunchedEffect(trigger) {
        if (trigger == null) return@LaunchedEffect
        // Wait one frame so the canvas has a size
        withFrameNanos { }
        val w = canvasSize[0].takeIf { it > 0f } ?: return@LaunchedEffect
        val h = canvasSize[1]
        val rnd = Random(System.nanoTime())
        repeat(particleCount) {
            val angle = (-PI / 2 + (rnd.nextFloat() - 0.5f) * PI * 0.9).toFloat()
            val speed = w * (0.9f + rnd.nextFloat() * 1.1f)
            particles += Particle(
                x = w * origin.x, y = h * origin.y,
                vx = cos(angle) * speed, vy = sin(angle) * speed,
                rotation = rnd.nextFloat() * 360f, spin = (rnd.nextFloat() - 0.5f) * 720f,
                color = palette[rnd.nextInt(palette.size)],
                w = 10f + rnd.nextFloat() * 10f, h = 6f + rnd.nextFloat() * 6f,
                circle = rnd.nextFloat() < 0.3f
            )
        }
        var last = withFrameNanos { it }
        val gravity = h * 1.6f
        while (particles.isNotEmpty()) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
            last = now
            val drag = 1f - 1.8f * dt
            particles.forEach { p ->
                p.vx *= drag
                p.vy = p.vy * drag + gravity * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.rotation += p.spin * dt
            }
            particles.removeAll { it.y > h + 40f }
            frame.longValue = now
        }
    }

    Canvas(modifier = modifier) {
        canvasSize[0] = size.width
        canvasSize[1] = size.height
        frame.longValue // read so each physics frame redraws
        particles.forEach { p ->
            translate(p.x, p.y) {
                rotate(p.rotation, pivot = Offset.Zero) {
                    if (p.circle) {
                        drawCircle(p.color, radius = p.h * 0.7f, center = Offset.Zero)
                    } else {
                        drawRect(p.color, topLeft = Offset(-p.w / 2, -p.h / 2), size = Size(p.w, p.h))
                    }
                }
            }
        }
    }
}

/**
 * Combo chip for quizzes: hidden below a 2-streak, then grows, warms in color and wobbles
 * on every new correct answer. Shows the active XP multiplier.
 */
@Composable
fun ComboMeter(combo: Int, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(combo) {
        if (combo >= 2) {
            scale.snapTo(if (scale.value < 0.5f) 0.4f else 1.3f)
            scale.animateTo(1f, Motion.jelly())
        } else {
            scale.animateTo(0f, Motion.smooth())
        }
    }
    if (scale.value <= 0.01f) return
    val multiplier = GamificationManager.comboMultiplier(combo)
    val color = when {
        combo >= 10 -> SoftRose
        combo >= 5 -> WarmCoral
        combo >= 3 -> SunnyAmber
        else -> FreshMint
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .background(
                Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.75f))),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(if (combo >= 5) "🔥" else "⚡", fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text("$combo combo", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        if (multiplier > 1f) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                val label = if (multiplier % 1f == 0f) "×${multiplier.toInt()}" else "×$multiplier"
                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
