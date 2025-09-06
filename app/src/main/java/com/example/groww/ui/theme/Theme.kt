package com.example.groww.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GrowwLightColorScheme = lightColorScheme(
    primary = GrowwGreen,
    onPrimary = Color.White,
    primaryContainer = GrowwLightGreen,
    onPrimaryContainer = GrowwDarkGreen,

    secondary = GrowwBlue,
    onSecondary = Color.White,
    secondaryContainer = GrowwBlue.copy(alpha = 0.1f),
    onSecondaryContainer = GrowwDarkBlue,

    tertiary = GrowwRed,
    onTertiary = Color.White,
    tertiaryContainer = GrowwRed.copy(alpha = 0.1f),
    onTertiaryContainer = GrowwDarkRed,

    background = GrowwBackground,
    onBackground = GrowwTextPrimary,
    surface = GrowwSurface,
    onSurface = GrowwTextPrimary,
    surfaceVariant = GrowwCardBackground,
    onSurfaceVariant = GrowwTextSecondary,

    outline = GrowwBorder,
    outlineVariant = GrowwBorder.copy(alpha = 0.5f),

    error = NegativeRed,
    onError = Color.White,
    errorContainer = NegativeRed.copy(alpha = 0.1f),
    onErrorContainer = NegativeRed
)

private val GrowwDarkColorScheme = darkColorScheme(
    primary = GrowwGreen,
    onPrimary = Color.Black,
    primaryContainer = GrowwDarkGreen,
    onPrimaryContainer = GrowwLightGreen,

    secondary = GrowwBlue,
    onSecondary = Color.White,
    secondaryContainer = GrowwDarkBlue,
    onSecondaryContainer = GrowwBlue.copy(alpha = 0.8f),

    tertiary = GrowwRed,
    onTertiary = Color.White,
    tertiaryContainer = GrowwDarkRed,
    onTertiaryContainer = GrowwRed.copy(alpha = 0.8f),

    background = GrowwDarkBackground,
    onBackground = GrowwDarkTextPrimary,
    surface = GrowwDarkSurface,
    onSurface = GrowwDarkTextPrimary,
    surfaceVariant = GrowwDarkCardBackground,
    onSurfaceVariant = GrowwDarkTextSecondary,

    outline = GrowwDarkBorder,
    outlineVariant = GrowwDarkBorder.copy(alpha = 0.5f),

    error = NegativeRed,
    onError = Color.White,
    errorContainer = NegativeRed.copy(alpha = 0.2f),
    onErrorContainer = NegativeRed.copy(alpha = 0.8f)
)

@Composable
fun GrowwTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent Groww branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GrowwDarkColorScheme
        else -> GrowwLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GrowwTypography,
        shapes = GrowwShapes,
        content = content
    )
}