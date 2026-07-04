package com.rmws2002.noteapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.entity.NoteEntity
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.ui.components.GreetingHeader
import com.rmws2002.noteapp.ui.components.NoteCard
import com.rmws2002.noteapp.ui.components.QuickActionChip
import com.rmws2002.noteapp.ui.components.TimelineView
import com.rmws2002.noteapp.ui.components.TodoRow
import com.rmws2002.noteapp.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToNotes: () -> Unit = {},
    onNavigateToTodos: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNoteClick: (Long) -> Unit,
    onTodoClick: (Long) -> Unit,
    onNewNote: () -> Unit = {},
    onNewTodo: () -> Unit = {},
    onNewSchedule: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val recentNotes by viewModel.recentNotes.collectAsState()
    val activeTodos by viewModel.activeTodos.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Greeting header
        item(key = "greeting") {
            GreetingHeader(
                todoCount = activeTodos.size,
                scheduleCount = todaySchedules.size
            )
        }

        // Quick actions
        item(key = "quick_actions") {
            QuickActionChip(
                onNewNote = onNewNote,
                onNewTodo = onNewTodo,
                onNewSchedule = onNewSchedule
            )
        }

        // Today's timeline
        if (todaySchedules.isNotEmpty()) {
            item(key = "today_schedule_header") {
                SectionHeader("今日时间线", onViewAll = onNavigateToSchedule)
            }
            item(key = "today_schedule") {
                TimelineView(
                    schedules = todaySchedules,
                    onEventClick = { /* TODO: navigate to schedule detail */ },
                    modifier = Modifier.height((todaySchedules.size * 80 + 100).dp.coerceAtMost(400.dp))
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Active Todos
        item(key = "todo_header") {
            SectionHeader("待办事项", onViewAll = onNavigateToTodos)
        }
        if (activeTodos.isEmpty()) {
            item(key = "todo_empty") {
                EmptyHint("没有待办事项")
            }
        } else {
            items(activeTodos.take(5), key = { it.id }) { todo ->
                TodoRow(
                    todo = todo,
                    onToggle = { viewModel.toggleTodo(todo) },
                    onClick = { onTodoClick(todo.id) },
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Recent Notes (2-column grid)
        item(key = "notes_header") {
            SectionHeader("最近笔记", onViewAll = onNavigateToNotes)
        }
        if (recentNotes.isEmpty()) {
            item(key = "notes_empty") {
                EmptyHint("没有笔记")
            }
        } else {
            // Show as 2-column grid
            val chunks = recentNotes.chunked(2)
            items(chunks, key = { it.firstOrNull()?.id ?: 0L }) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.getOrNull(0)?.let { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    pair.getOrNull(1)?.let { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        TextButton(onClick = onViewAll) {
            Text("查看全部")
        }
    }
}

@Composable
fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
