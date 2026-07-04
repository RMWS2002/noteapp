package com.rmws2002.noteapp.ui.util

import androidx.compose.ui.graphics.Color

/** Parse hex color string like "#1A73E8" or "#FF1A73E8" into Compose Color. */
fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color.Gray
    }
}
