package com.example.wifisignalmapper.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Teal40,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE1E1E1),
    onSurface = Color(0xFFE1E1E1),
    primaryContainer = Blue40.copy(alpha = 0.85f),
    onPrimaryContainer = Color.White,
    secondaryContainer = BlueGrey40.copy(alpha = 0.85f),
    onSecondaryContainer = Color.White,
    tertiaryContainer = Teal40.copy(alpha = 0.85f),
    onTertiaryContainer = Color.White,
    surfaceVariant = SurfaceDark.copy(alpha = 0.7f),
    onSurfaceVariant = Color(0xFFCFCFCF)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Teal80,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    primaryContainer = Blue80.copy(alpha = 0.12f),
    onPrimaryContainer = Blue80,
    secondaryContainer = BlueGrey80.copy(alpha = 0.12f),
    onSecondaryContainer = BlueGrey80,
    tertiaryContainer = Teal80.copy(alpha = 0.12f),
    onTertiaryContainer = Teal80,
    surfaceVariant = Color(0xFFE7E9EB),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFFABAFB7),
    outlineVariant = Color(0xFFCDD0D5)
)

@Composable
fun WiFiSignalMapperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use our custom colors instead of system colors
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}