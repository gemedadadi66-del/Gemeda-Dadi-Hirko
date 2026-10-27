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
    primary = SkyAccent,
    onPrimary = Color.White,
    primaryContainer = Slate800,
    onPrimaryContainer = SkyAccentLight,
    secondary = SealCrimson,
    onSecondary = Color.White,
    secondaryContainer = Slate800,
    onSecondaryContainer = SealCrimsonLight,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200
)

private val LightColorScheme = lightColorScheme(
    primary = SkyAccent,
    onPrimary = Color.White,
    primaryContainer = SkyAccentLight,
    onPrimaryContainer = Slate900,
    secondary = SealCrimson,
    onSecondary = Color.White,
    secondaryContainer = SealCrimsonLight,
    onSecondaryContainer = SealCrimsonDark,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our intentional palette for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
