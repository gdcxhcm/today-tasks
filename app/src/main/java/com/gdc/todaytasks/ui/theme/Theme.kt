package com.gdc.todaytasks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmWhite = Color(0xFFF7F4EF)
private val Ink = Color(0xFF171717)
private val Muted = Color(0xFF666666)
private val Hairline = Color(0xFFE7E2DA)
private val Sage = Color(0xFF7C8B7A)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9E2),
    onPrimaryContainer = Ink,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8DDD2),
    onSecondaryContainer = Ink,
    tertiary = Color(0xFFA28F7A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE9DED1),
    onTertiaryContainer = Ink,
    background = WarmWhite,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1EEE8),
    onSurfaceVariant = Muted,
    outline = Hairline,
    error = Color(0xFFB66A5E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFEDE9E2),
    onPrimary = Ink,
    primaryContainer = Color(0xFF2A2926),
    onPrimaryContainer = Color(0xFFF7F4EF),
    secondary = Color(0xFFBAC5B4),
    onSecondary = Ink,
    background = Color(0xFF11110F),
    onBackground = Color(0xFFF4F1EA),
    surface = Color(0xFF1B1A18),
    onSurface = Color(0xFFF4F1EA),
    surfaceVariant = Color(0xFF24231F),
    onSurfaceVariant = Color(0xFFB8B3AA),
    outline = Color(0xFF38352F)
)

@Composable
fun TodayTasksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
