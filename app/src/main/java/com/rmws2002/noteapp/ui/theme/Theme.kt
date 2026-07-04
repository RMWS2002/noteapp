package com.rmws2002.noteapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Color(0xFF86868B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E8ED),
    onSecondaryContainer = Color(0xFF1D1D1F),
    tertiary = Color(0xFF5F5FFF),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0E0FF),
    onTertiaryContainer = Color(0xFF1D1D1F),
    background = BgLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = Color(0xFF86868B),
    outline = Color(0xFFD1D1D6),
    error = Danger,
    onError = Color.White,
    outlineVariant = Color(0xFFE5E5EA)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BB8FF),
    onPrimary = PrimaryDark,
    primaryContainer = Primary,
    onPrimaryContainer = PrimaryLight,
    secondary = Color(0xFF98989D),
    onSecondary = Color(0xFF1D1D1F),
    secondaryContainer = Color(0xFF3C3C3E),
    onSecondaryContainer = Color(0xFFE5E5EA),
    tertiary = Color(0xFF9E9EFF),
    onTertiary = Color(0xFF1D1D1F),
    tertiaryContainer = Color(0xFF3C3C5E),
    onTertiaryContainer = Color(0xFFE0E0FF),
    background = BgDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = Color(0xFF98989D),
    outline = Color(0xFF38383A),
    error = Danger,
    onError = Color.White,
    outlineVariant = Color(0xFF2C2C2E)
)

@Composable
fun NoteAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) {
                Color.Transparent.toArgb()
            } else {
                colorScheme.background.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
