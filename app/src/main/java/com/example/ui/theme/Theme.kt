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
    primary = CyanNeon,
    onPrimary = ObsidianDark,
    primaryContainer = CyanNeonDark,
    onPrimaryContainer = Color.White,
    secondary = EmeraldLocked,
    onSecondary = ObsidianDark,
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = AmberCaution,
    onTertiary = ObsidianDark,
    background = ObsidianDark,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = RubyRecording,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    // Studio tracing environment prefers high-contrast dark theme by default
    primary = CyanNeonDark,
    onPrimary = Color.White,
    secondary = EmeraldLocked,
    onSecondary = Color.White,
    background = ObsidianDark,
    surface = MidnightSurface,
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve high-contrast neon studio theme for AR visibility
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
