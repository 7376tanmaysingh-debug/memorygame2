package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF4081), // Pink
        Color(0xFF7C4DFF), // Purple
        Color(0xFF00E676), // Green
        Color(0xFF00B0FF), // Light Blue
        Color(0xFFFF9100), // Orange
        Color(0xFFF50057)  // Magenta
    )

    val particles = remember {
        List(70) {
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = -Random.nextFloat() * 0.4f,
                speedY = 0.5f + Random.nextFloat() * 0.7f,
                speedX = (Random.nextFloat() - 0.5f) * 0.35f,
                size = 14f + Random.nextFloat() * 16f,
                color = colors.random(),
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        particles.forEach { p ->
            val currentX = (p.startX + p.speedX * t) * size.width
            val currentY = (p.startY + p.speedY * t) * size.height
            val currentRotation = p.rotationSpeed * t

            if (currentY in 0f..size.height) {
                rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.size / 2f,
                            center = Offset(currentX, currentY)
                        )
                    } else {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(currentX - p.size / 2f, currentY - p.size / 3f),
                            size = Size(p.size, p.size * 0.65f)
                        )
                    }
                }
            }
        }
    }
}
