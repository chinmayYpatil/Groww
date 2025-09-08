package com.example.groww.ui.theme

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
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
    // Memoize color scheme selection to prevent recreation
    val colorScheme = remember(darkTheme, dynamicColor) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Dynamic colors are more expensive, cache them
                null // Will be handled below
            }
            darkTheme -> GrowwDarkColorScheme
            else -> GrowwLightColorScheme
        }
    }

    // Handle dynamic colors separately to avoid blocking the main theme
    val finalColorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        remember(darkTheme, context) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
    } else {
        colorScheme ?: GrowwLightColorScheme
    }

    val view = LocalView.current

    // Optimize window decorations - only update when necessary
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = finalColorScheme.primary.toArgb()

            // Only update if color actually changed
            if (window.statusBarColor != statusBarColor) {
                window.statusBarColor = statusBarColor
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

                // Add performance optimizations
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                )
            }
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = GrowwTypography,
        shapes = GrowwShapes,
        content = content
    )
}