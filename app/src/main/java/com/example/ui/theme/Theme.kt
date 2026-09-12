package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppAccent,
    onPrimary = Color.Black,
    primaryContainer = WhatsAppTealDark,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppGreenLight,
    onSecondary = Color.Black,
    background = WhatsAppBgDark,
    onBackground = TextPrimaryDark,
    surface = WhatsAppSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = WhatsAppTopBarDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppGreen,
    onPrimary = Color.White,
    primaryContainer = WhatsAppGreenDark,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppGreenLight,
    onSecondary = Color.White,
    background = WhatsAppBgLight,
    onBackground = TextPrimaryLight,
    surface = WhatsAppSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use WhatsApp signature colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
