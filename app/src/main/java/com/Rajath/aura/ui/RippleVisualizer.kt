// RippleVisualizer.kt
package com.Rajath.aura.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * amplitude: 0..1 (audio-driven)
 * rings: number of concentric ripples
 */
@Composable
fun RippleVisualizer(
    amplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    rings: Int = 4
) {
    // subtle smoothing for UI (avoid jumpy)
    val target = amplitude.coerceIn(0f, 1f)
    val smoothed by animateFloatAsState(target, animationSpec = androidx.compose.animation.core.spring( stiffness = 300f, dampingRatio = 0.7f))

    val baseColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.toPx()
            val h = size.toPx()
            val cx = w / 2f
            val cy = h / 2f
            val maxRadius = min(cx, cy) * 0.92f

            // overall amplitude scale (boost small amplitudes a bit)
            val ampScale = (0.35f + smoothed * 1.2f).coerceIn(0.2f, 1.6f)

            // draw background soft fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(baseColor.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = Offset(cx, cy)
            )

            // draw rings from outer to inner (so inner overlays outer)
            for (i in (rings - 1) downTo 0) {
                val t = i.toFloat() / rings.toFloat() // 0..(rings-1)/rings
                // radius base for this ring
                val baseRadius = maxRadius * (0.35f + t * 0.6f)
                // audio-driven offset
                val dynamic = baseRadius * (0.06f + ampScale * 0.08f * (1f - t))
                val radius = (baseRadius + dynamic).coerceAtMost(maxRadius)

                val alpha = (0.22f * (1f - t) * (0.6f + smoothed * 0.8f)).coerceIn(0f, 1f)
                val stroke = (2f + (1.8f * (1f - t))).coerceAtLeast(1f)

                // gradient stroke color slightly shifted per ring
                val ringColor = when (i % 3) {
                    0 -> baseColor.copy(alpha = alpha)
                    1 -> accentColor.copy(alpha = alpha * 0.95f)
                    else -> baseColor.copy(alpha = alpha * 0.9f)
                }

                drawCircle(
                    color = ringColor,
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = stroke)
                )
            }

            // Center dot
            drawCircle(
                color = baseColor,
                radius = maxRadius * (0.08f + 0.02f * smoothed),
                center = Offset(cx, cy)
            )
        }
    }
}