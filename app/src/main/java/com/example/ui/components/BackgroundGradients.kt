package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DarkBgEnd
import com.example.ui.theme.DarkBgMid
import com.example.ui.theme.DarkBgStart
import com.example.ui.theme.LightBgEnd
import com.example.ui.theme.LightBgMid
import com.example.ui.theme.LightBgStart

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(DarkBgStart, DarkBgMid, DarkBgEnd)
    } else {
        listOf(LightBgStart, LightBgMid, LightBgEnd)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Decorative ambient subtle circular glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            val primaryOrbColor = if (isDark) Color(0x188B5CF6) else Color(0x147C3AED)
            val accentOrbColor = if (isDark) Color(0x12F59E0B) else Color(0x10F59E0B)

            drawCircle(
                color = primaryOrbColor,
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.15f, size.height * 0.15f)
            )
            drawCircle(
                color = accentOrbColor,
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.85f, size.height * 0.7f)
            )
        }
        content()
    }
}
