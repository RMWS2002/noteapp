package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuickActionChip(
    onNewNote: () -> Unit,
    onNewTodo: () -> Unit,
    onNewSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = onNewNote,
            label = { Text("新笔记") },
            leadingIcon = {
                Icon(Icons.Default.Description, contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        AssistChip(
            onClick = onNewTodo,
            label = { Text("新待办") },
            leadingIcon = {
                Icon(Icons.Default.Checklist, contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
        AssistChip(
            onClick = onNewSchedule,
            label = { Text("新日程") },
            leadingIcon = {
                Icon(Icons.Default.Event, contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        )
    }
}
