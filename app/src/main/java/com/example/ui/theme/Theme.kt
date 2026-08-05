package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = LightForestGreen,
    secondary = BrassAccent,
    tertiary = StampBlue,
    background = Color(0xFF121B17),
    surface = Color(0xFF1E2823)
)

private val LightColorScheme = lightColorScheme(
    primary = DarkForestGreen,
    secondary = LightForestGreen,
    tertiary = BrassAccent,
    background = CreamPaperBg,
    surface = CreamPaperBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ForestGreenText,
    onSurface = ForestGreenText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
