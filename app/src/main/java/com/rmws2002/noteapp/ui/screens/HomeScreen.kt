package com.rmws2002.noteapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.ui.components.NoteCard
import com.rmws2002.noteapp.ui.components.TodoRow
import com.rmws2002.noteapp.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToNotes: () -> Unit,
    onNavigateToTodos: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onTodoClick: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val recentNotes by viewModel.recentNotes.collectAsState()
    val activeTodos by viewModel.activeTodos.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()
    val dateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "今天 ${dateFormat.format(Date())}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Today's Schedules
        if (todaySchedules.isNotEmpty()) {
            item {
                SectionHeader("今日日程", onViewAll = onNavigateToSchedule)
            }
            items(todaySchedules) { schedule ->
                ScheduleItem(schedule = schedule, onClick = { onNavigateToSchedule() })
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Active Todos
        item {
            SectionHeader("待办事项", onViewAll = onNavigateToTodos)
        }
        if (activeTodos.isEmpty()) {
            item {
                EmptyHint("没有待办事项")
            }
        } else {
            items(activeTodos.take(5)) { todo ->
                TodoRow(
                    todo = todo,
                    onToggle = { /* viewModel.toggleTodo(todo) */ },
                    onClick = { onTodoClick(todo.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Recent Notes
        item {
            SectionHeader("最近笔记", onViewAll = onNavigateToNotes)
        }
        if (recentNotes.isEmpty()) {
            item {
                EmptyHint("没有笔记")
            }
        } else {
            items(recentNotes) { note ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ScheduleItem(schedule: ScheduleEntity, onClick: () -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${timeFormat.format(Date(schedule.startTime))} ${schedule.title}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
