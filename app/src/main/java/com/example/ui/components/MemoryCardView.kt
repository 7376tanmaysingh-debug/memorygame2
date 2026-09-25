package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MemoryCard
import com.example.ui.theme.CardBackBorder
import com.example.ui.theme.CardBackGradEnd
import com.example.ui.theme.CardBackGradStart
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError

@Composable
fun MemoryCardView(
    card: MemoryCard,
    cardCornerRadius: Dp,
    fontSize: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3D rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "cardFlip_${card.id}"
    )

    // Scale animation: slight bounce when face up, subtle shrink when matched
    val scale by animateFloatAsState(
        targetValue = when {
            card.isMatched -> 0.94f
            card.isFaceUp -> 1.04f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale_${card.id}"
    )

    // Shake animation on mismatch
    val infiniteTransition = rememberInfiniteTransition(label = "shakeTransition_${card.id}")
    val shakeOffset by if (card.isMismatched) {
        infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(60, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake_${card.id}"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val shape = RoundedCornerShape(cardCornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .testTag("card_${card.id}")
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
                scaleX = scale
                scaleY = scale
                translationX = shakeOffset
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !card.isFaceUp && !card.isMatched
            ) {
                onClick()
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (card.isMatched) 1.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        if (rotation <= 90f) {
            CardBack(shape = shape, cornerRadius = cardCornerRadius)
        } else {
            CardFront(
                card = card,
                shape = shape,
                fontSize = fontSize,
                modifier = Modifier.graphicsLayer {
                    // Reverse rotation so emoji content is not mirrored
                    rotationY = 180f
                }
            )
        }
    }
}

@Composable
private fun CardBack(
    shape: RoundedCornerShape,
    cornerRadius: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(CardBackGradStart, CardBackGradEnd)
                ),
                shape = shape
            )
            .border(
                BorderStroke(2.dp, CardBackBorder.copy(alpha = 0.65f)),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner decorative circle
        Box(
            modifier = Modifier
                .size(if (cornerRadius > 12.dp) 36.dp else 24.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QuestionMark,
                contentDescription = "Card Back",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(if (cornerRadius > 12.dp) 20.dp else 14.dp)
            )
        }
    }
}

@Composable
private fun CardFront(
    card: MemoryCard,
    shape: RoundedCornerShape,
    fontSize: Float,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        card.isMismatched -> RoseError
        card.isMatched -> EmeraldSuccess
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    }

    val borderWidth = if (card.isMatched || card.isMismatched) 2.5.dp else 1.5.dp

    val surfaceColor = if (card.isMatched) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor, shape = shape)
            .border(BorderStroke(borderWidth, borderColor), shape = shape)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = card.emoji,
            fontSize = fontSize.sp
        )

        if (card.isMatched) {
            // Little matched checkmark badge in upper right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
                    .background(EmeraldSuccess, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Matched",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
