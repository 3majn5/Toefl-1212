package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset

/**
 * Shared spring presets. Springs keep their velocity when interrupted, so motion never
 * snaps or restarts: that continuity is what makes the UI feel fluid rather than mechanical.
 */
object Motion {
    /** Soft and slightly bouncy, used for entrances, chips and celebrations. */
    fun <T> liquid(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = 0.68f, stiffness = 260f, visibilityThreshold = visibilityThreshold)

    /** Wobbly jelly release for pressed elements. */
    fun <T> jelly(): SpringSpec<T> = spring(dampingRatio = 0.38f, stiffness = 520f)

    /** Smooth without overshoot, used for fills, counters and colors. */
    fun <T> smooth(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 140f, visibilityThreshold = visibilityThreshold)

    /** Screen-to-screen slides. */
    val screen: SpringSpec<IntOffset> = spring(
        dampingRatio = 0.86f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset(1, 1)
    )
}
