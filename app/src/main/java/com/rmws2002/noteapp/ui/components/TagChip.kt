package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rmws2002.noteapp.data.entity.TagEntity

fun parseColor(hex: String, defaultColor: Color = Color(0xFF2C6FEF)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(
    tag: TagEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = parseColor(tag.color)
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = tagColor.copy(alpha = 0.15f),
        selectedContainerColor = tagColor,
        labelColor = tagColor,
        selectedLabelColor = Color.White
    )

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = chipColors,
        modifier = modifier.padding(end = 6.dp)
    )
}
