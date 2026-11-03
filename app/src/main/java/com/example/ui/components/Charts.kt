package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Chart mark colors, validated for lightness band, CVD separation and >=3:1 contrast
// against the card surface in each theme (dataviz palette validator).
object ChartColors {
    @Composable
    fun bar(): Color = if (isDark()) Color(0xFF7F80EA) else Color(0xFF5B5BD6)

    @Composable
    fun line(): Color = if (isDark()) Color(0xFF1BA37E) else Color(0xFF0B8F6E)

    @Composable
    private fun isDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

/**
 * Single-series bar chart. Bars have 4dp rounded tops anchored to the baseline and a gap
 * between them; tapping a bar shows its exact value above the plot.
 */
@Composable
fun WeeklyBarChart(
    labels: List<String>,
    values: List<Int>,
    unit: String,
    modifier: Modifier = Modifier
) {
    val barColor = ChartColors.bar()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    var selected by remember(values) { mutableStateOf(values.indices.lastOrNull()) }
    val grow = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { grow.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val niceMax = niceCeiling(maxValue)

    Column(modifier = modifier) {
        // Tooltip line: the tapped bar's exact value, in text ink (never the series color)
        Text(
            text = selected?.let { "${labels[it]}: ${values[it]} $unit" } ?: "Tap a bar for details",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row {
            // Y-axis labels
            Column(
                modifier = Modifier.height(160.dp).padding(end = 6.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                listOf(niceMax, niceMax / 2, 0).forEach {
                    Text("$it", fontSize = 10.sp, color = muted)
                }
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .pointerInput(values) {
                        detectTapGestures { offset ->
                            val slot = size.width / values.size.coerceAtLeast(1)
                            selected = (offset.x / slot).toInt().coerceIn(0, values.lastIndex)
                        }
                    }
            ) {
                val h = size.height
                listOf(0f, 0.5f, 1f).forEach { f ->
                    drawLine(gridColor, Offset(0f, h * f), Offset(size.width, h * f), strokeWidth = 1f)
                }
                val slot = size.width / values.size.coerceAtLeast(1)
                val barWidth = (slot * 0.55f).coerceAtMost(28.dp.toPx())
                val radius = 4.dp.toPx()
                values.forEachIndexed { i, v ->
                    val barHeight = h * (v.toFloat() / niceMax) * grow.value
                    if (barHeight <= 0f) return@forEachIndexed
                    val left = slot * i + (slot - barWidth) / 2
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = left, top = h - barHeight, right = left + barWidth, bottom = h,
                                topLeftCornerRadius = CornerRadius(radius), topRightCornerRadius = CornerRadius(radius),
                                bottomLeftCornerRadius = CornerRadius.Zero, bottomRightCornerRadius = CornerRadius.Zero
                            )
                        )
                    }
                    drawPath(path, if (selected == null || selected == i) barColor else barColor.copy(alpha = 0.45f))
                }
            }
        }
        // X-axis labels: every other week to avoid collisions
        Row(modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 4.dp)) {
            labels.forEachIndexed { i, label ->
                Text(
                    text = if (i % 2 == labels.lastIndex % 2) label else "",
                    fontSize = 10.sp,
                    color = muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * One small-multiple panel: a single skill's weekly accuracy on a shared 0–100% scale.
 * Weeks without attempts leave a gap rather than drawing a misleading zero.
 */
@Composable
fun AccuracySparkPanel(
    title: String,
    values: List<Float?>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val lineColor = ChartColors.line()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val surface = MaterialTheme.colorScheme.surface
    var selected by remember(values) { mutableStateOf<Int?>(null) }
    val latest = values.indexOfLast { it != null }

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            val shownIndex = selected ?: latest
            Text(
                text = if (shownIndex >= 0 && values[shownIndex] != null) {
                    "${(values[shownIndex]!! * 100).toInt()}%" + if (selected != null) " · ${labels[shownIndex]}" else ""
                } else "No practice yet",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 4.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .pointerInput(values) {
                        detectTapGestures { offset ->
                            val step = size.width / (values.size - 1).coerceAtLeast(1)
                            val i = (offset.x / step + 0.5f).toInt().coerceIn(0, values.lastIndex)
                            selected = if (values[i] != null) i else null
                        }
                    }
            ) {
                val h = size.height
                val pad = 5.dp.toPx()
                drawLine(gridColor, Offset(0f, pad), Offset(size.width, pad), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, h - pad), Offset(size.width, h - pad), strokeWidth = 1f)
                val step = size.width / (values.size - 1).coerceAtLeast(1)
                fun point(i: Int, v: Float) = Offset(step * i, pad + (h - 2 * pad) * (1f - v))

                // Line segments, broken across weeks with no data
                var path: Path? = null
                values.forEachIndexed { i, v ->
                    if (v == null) {
                        path?.let { drawPath(it, lineColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                        path = null
                    } else {
                        val p = point(i, v)
                        path = (path ?: Path().apply { moveTo(p.x, p.y) }).apply { lineTo(p.x, p.y) }
                    }
                }
                path?.let { drawPath(it, lineColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) }

                // Markers (8dp) with a 2dp surface ring; the selected/latest point is emphasized
                values.forEachIndexed { i, v ->
                    if (v == null) return@forEachIndexed
                    val emphasized = i == (selected ?: latest)
                    val r = if (emphasized) 5.dp.toPx() else 4.dp.toPx()
                    drawCircle(surface, radius = r + 2.dp.toPx(), center = point(i, v))
                    drawCircle(lineColor, radius = r, center = point(i, v))
                }
            }
        }
    }
}

/** Rounds up to a tidy axis maximum (1, 2, 5 × 10ⁿ). */
private fun niceCeiling(value: Int): Int {
    if (value <= 5) return 5
    var magnitude = 1
    while (magnitude * 10 <= value) magnitude *= 10
    return listOf(1, 2, 5, 10).map { it * magnitude }.first { it >= value }
}
