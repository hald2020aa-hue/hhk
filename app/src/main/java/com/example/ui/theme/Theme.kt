package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan400,
    onPrimary = SlateNavy950,
    primaryContainer = CyberCyan900,
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = ElectricViolet400,
    onSecondary = SlateNavy950,
    secondaryContainer = ElectricViolet900,
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = EmeraldAccent400,
    onTertiary = SlateNavy950,
    tertiaryContainer = EmeraldAccent900,
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = SlateNavy950,
    onBackground = Color(0xFFF1F5F9),
    surface = SlateNavy900,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SlateNavy800,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = SlateNavy700,
    error = CoralRose400
)

private val LightColorScheme = lightColorScheme(
    primary = CyberCyan500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = CyberCyan900,
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = ElectricViolet900,
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = EmeraldAccent900,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFE11D48)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
