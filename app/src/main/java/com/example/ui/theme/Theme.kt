package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = VioletPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = AmberAccentLight,
    onSecondary = Color.Black,
    tertiary = SkyInfo,
    background = DarkBgStart,
    onBackground = DarkOnSurface,
    surface = DarkSurfaceCard,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceCardElevated,
    onSurfaceVariant = DarkOnSurfaceSubtle,
    error = RoseError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletPrimaryLight,
    onPrimaryContainer = Color(0xFF1E0059),
    secondary = AmberAccent,
    onSecondary = Color.White,
    tertiary = SkyInfo,
    background = LightBgStart,
    onBackground = LightOnSurface,
    surface = LightSurfaceCard,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceCardElevated,
    onSurfaceVariant = LightOnSurfaceSubtle,
    error = RoseError,
    onError = Color.White
)

@Composable
fun MemoryMatchTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
