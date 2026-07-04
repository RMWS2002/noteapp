package com.rmws2002.noteapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.entity.TagEntity
import com.rmws2002.noteapp.ui.components.NoteCard
import com.rmws2002.noteapp.ui.components.TagChip
import com.rmws2002.noteapp.viewmodel.NoteViewModel

@Composable
fun NoteListScreen(
    onAddNote: () -> Unit,
    onNoteClick: (Long) -> Unit,
    viewModel: NoteViewModel = viewModel()
) {
    val notes by viewModel.allNotes.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTagId by viewModel.selectedTagId.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp)
        ) {
            // Tag filter row
            item {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp)
                ) {
                    TagChip(
                        tag = TagEntity(name = "全部", color = "#666666"),
                        isSelected = selectedTagId == null,
                        onClick = { viewModel.filterByTag(null) }
                    )
                    tags.forEach { tag ->
                        TagChip(
                            tag = tag,
                            isSelected = selectedTagId == tag.id,
                            onClick = { viewModel.filterByTag(tag.id) }
                        )
                    }
                }
            }

            if (notes.isEmpty()) {
                item {
                    EmptyHint("还没有笔记，点击 + 创建")
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddNote,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加笔记")
        }
    }
}
