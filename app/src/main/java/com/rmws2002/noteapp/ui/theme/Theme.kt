package com.rmws2002.noteapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.rmws2002.noteapp.data.preferences.ThemeMode
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
    secondary = Color(0xFF7D7D78),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E8E2),
    onSecondaryContainer = Color(0xFF1D1B18),
    tertiary = Color(0xFF7D7148),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5ECD7),
    onTertiaryContainer = Color(0xFF1D1B18),
    background = BgLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = Color(0xFF6B6B65),
    outline = OutlineLight,
    error = Danger,
    onError = Color.White,
    outlineVariant = Color(0xFFE2DFD8)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AE52),
    onPrimary = Color(0xFF2C2416),
    primaryContainer = Color(0xFF5C4218),
    onPrimaryContainer = PrimaryLight,
    secondary = Color(0xFF989892),
    onSecondary = Color(0xFF1D1B18),
    secondaryContainer = Color(0xFF3C3C38),
    onSecondaryContainer = Color(0xFFE0E0DA),
    tertiary = Color(0xFFC4B888),
    onTertiary = Color(0xFF1D1B18),
    tertiaryContainer = Color(0xFF3C3828),
    onTertiaryContainer = Color(0xFFF5ECD7),
    background = BgDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = Color(0xFF989892),
    outline = OutlineDark,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF1D1B18),
    outlineVariant = Color(0xFF2C2C2E)
)

@Composable
fun NoteAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (isDark) {
                Color.Transparent.toArgb()
            } else {
                colorScheme.background.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
