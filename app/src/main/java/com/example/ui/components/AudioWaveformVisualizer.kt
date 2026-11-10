package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AudioWaveformVisualizer(
    isSpeaking: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    val phase = remember { Animatable(0f) }

    LaunchedEffect(isSpeaking) {
        if (isSpeaking) {
            phase.animateTo(
                targetValue = 6.283f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            phase.snapTo(0f)
        }
    }

    val idleColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("audio_waveform_visualizer")
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / (barCount * 1.6f)
        val gap = (width - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)
        val centerY = height / 2f

        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / barCount.toFloat()

            val barHeight = if (isSpeaking) {
                // Combine sine wave phase with live amplitude and pseudo-random harmonic frequency
                val harmonic = sin(phase.value + normalizedX * 5.0).toFloat()
                val centerWeight = sin(normalizedX * Math.PI).toFloat().coerceIn(0.2f, 1.0f)
                val dynamicHeight = (height * 0.85f) * (amplitude.coerceIn(0.2f, 1.0f)) * (0.35f + 0.65f * centerWeight * (0.5f + 0.5f * harmonic))
                dynamicHeight.coerceIn(6f, height * 0.95f)
            } else {
                // Subtle resting idle waveform
                val idleWave = sin(normalizedX * Math.PI).toFloat() * 10f
                idleWave.coerceIn(4f, 12f)
            }

            val x = i * (barWidth + gap)
            val topY = centerY - (barHeight / 2f)

            val brush = Brush.verticalGradient(
                colors = listOf(
                    if (isSpeaking) secondaryColor else idleColor,
                    if (isSpeaking) activeColor else idleColor
                ),
                startY = topY,
                endY = topY + barHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
