package com.Rajath.aura.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.Rajath.aura.ui.theme.BrandBlue
import com.Rajath.aura.ui.theme.BrandPale
import com.Rajath.aura.ui.theme.BrandCyan

@Composable
fun BreathingBackground(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    rings: Int = 3,
    isPlaying: Boolean = false
) {
    val baseDurationMs = if (isPlaying) 1200 else 2200
    val ringDelayStep = 160

    // Brighter, more vibrant meditation colors (bluish + purple + pink)
    val basePalette = listOf(
        BrandCyan.copy(alpha = 0.99f),     // very bright outer glow
        BrandBlue.copy(alpha = 0.45f),     // strong blue mid layer
        BrandPale.copy(alpha = 0.95f),     // bright pale center halo
        BrandCyan.copy(alpha = 0.85f),     // extra cyan reinforcement layer
        BrandBlue.copy(alpha = 0.55f)      // deeper but still bright
    )

    val palette = remember(rings) { (0 until rings).map { idx -> basePalette[idx % basePalette.size] } }

    val transition = rememberInfiniteTransition()

    // Use the exact Box overload with modifier + contentAlignment to avoid the error
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        for (i in 0 until rings) {
            val duration = baseDurationMs + i * 220
            val delay = i * ringDelayStep

            // explicit generic types to avoid inference ambiguity
            val scaleSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable<Float>(
                animation = tween<Float>(durationMillis = duration, easing = FastOutSlowInEasing, delayMillis = delay),
                repeatMode = RepeatMode.Reverse
            )

            val scaleStart = 0.82f + (i * 0.02f)
            val scaleEnd = 1.06f - (i * 0.01f)
            val scale by transition.animateFloat(
                initialValue = scaleStart,
                targetValue = scaleEnd,
                animationSpec = scaleSpec
            )

            val alphaSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable<Float>(
                animation = tween<Float>(durationMillis = (duration * 0.9f).toInt(), easing = LinearEasing, delayMillis = delay / 2),
                repeatMode = RepeatMode.Reverse
            )

            val alphaStart = 0.08f + i * 0.03f
            val alphaEnd = 0.30f + i * 0.03f
            val alpha by transition.animateFloat(
                initialValue = alphaStart,
                targetValue = alphaEnd,
                animationSpec = alphaSpec
            )

            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .alpha(alpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(palette[i], Color.Transparent),
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // empty content — the Box itself is the ring
            }
        }

        // center glow
        val centerDuration = if (isPlaying) 900 else 1400
        val centerSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable<Float>(
            animation = tween<Float>(durationMillis = centerDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
        val centerScale by transition.animateFloat(initialValue = 0.12f, targetValue = 0.20f, animationSpec = centerSpec)
        val centerAlpha by transition.animateFloat(initialValue = 0.92f, targetValue = 0.98f, animationSpec = centerSpec)

        Box(
            modifier = Modifier
                .size(size)
                .scale(centerScale)
                .alpha(centerAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandPale.copy(alpha = 0.30f),
                            BrandCyan.copy(alpha = 0.05f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // center glow done
        }
    }
}
