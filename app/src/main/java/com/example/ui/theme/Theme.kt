package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SwbYellow,
    onPrimary = SwbNavy,
    primaryContainer = SwbDeepBlue,
    onPrimaryContainer = SwbYellowLight,
    secondary = SwbCyan,
    onSecondary = SwbNavy,
    secondaryContainer = SwbSurfaceBlue,
    onSecondaryContainer = SwbCyanLight,
    tertiary = SwbValidGreen,
    onTertiary = Color.White,
    background = SwbNavy,
    onBackground = TextWhite,
    surface = SwbDeepBlue,
    onSurface = TextWhite,
    surfaceVariant = SwbSurfaceBlue,
    onSurfaceVariant = TextSecondary,
    error = SwbErrorRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = SwbYellow,
    onPrimary = SwbNavy,
    primaryContainer = SwbDeepBlue,
    onPrimaryContainer = SwbYellowLight,
    secondary = SwbCyan,
    onSecondary = SwbNavy,
    secondaryContainer = SwbSurfaceBlue,
    onSecondaryContainer = SwbCyanLight,
    tertiary = SwbValidGreen,
    onTertiary = Color.White,
    background = SwbNavy,
    onBackground = TextWhite,
    surface = SwbDeepBlue,
    onSurface = TextWhite,
    surfaceVariant = SwbSurfaceBlue,
    onSurfaceVariant = TextSecondary,
    error = SwbErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Saigon Waterbus signature brand colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
