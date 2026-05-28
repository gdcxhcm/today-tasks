package com.gdc.todaytasks.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFFE84C63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9DE),
    secondary = Color(0xFF167C72),
    secondaryContainer = Color(0xFFA5F2E4),
    tertiary = Color(0xFF8A5A00),
    tertiaryContainer = Color(0xFFFFDEA6),
    surface = Color(0xFFFFF8F6),
    surfaceVariant = Color(0xFFFFE9E7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB1BD),
    secondary = Color(0xFF85D5C8),
    tertiary = Color(0xFFFFBC48)
)

@Composable
fun TodayTasksTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !dark -> LightColors
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
