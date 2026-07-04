package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rmws2002.noteapp.data.entity.TagEntity

@Composable
fun TagChip(
    tag: TagEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = modifier.padding(end = 4.dp)
    )
}
