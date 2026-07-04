package com.rmws2002.noteapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.entity.NoteEntity
import com.rmws2002.noteapp.ui.components.DailyQuote
import com.rmws2002.noteapp.ui.components.GreetingHeader
import com.rmws2002.noteapp.ui.components.MiniWeekStrip
import com.rmws2002.noteapp.ui.components.NoteCard
import com.rmws2002.noteapp.ui.components.OverviewCard
import com.rmws2002.noteapp.ui.components.QuickActionChip
import com.rmws2002.noteapp.ui.components.TodoRow
import com.rmws2002.noteapp.ui.components.buildWeekDays
import com.rmws2002.noteapp.ui.util.formatTime
import com.rmws2002.noteapp.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

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
    val completedTodoCount by viewModel.completedTodoCount.collectAsState()
    val weekEventDates by viewModel.weekEventDates.collectAsState()

    val weekDays = remember(weekEventDates) { buildWeekDays(weekEventDates) }

    // Staggered reveal: 1=greeting, 2=quote, 3=weekstrip, 4=overview, 5=actions, 6=schedule, 7=todos, 8=notes
    var revealStep by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        revealStep = 1; delay(100)
        revealStep = 2; delay(120)
        revealStep = 3; delay(100)
        revealStep = 4; delay(120)
        revealStep = 5; delay(100)
        revealStep = 6; delay(120)
        revealStep = 7; delay(100)
        revealStep = 8
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 1. Greeting header
        item(key = "greeting") {
            AnimatedVisibility(
                visible = revealStep >= 1,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                GreetingHeader(
                    todoCount = activeTodos.size,
                    scheduleCount = todaySchedules.size
                )
            }
        }

        // 2. Daily quote
        item(key = "quote") {
            AnimatedVisibility(
                visible = revealStep >= 2,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                DailyQuote(modifier = Modifier.padding(top = 4.dp))
            }
        }

        // 3. Mini week strip
        item(key = "week_strip") {
            AnimatedVisibility(
                visible = revealStep >= 3,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                MiniWeekStrip(
                    weekDays = weekDays,
                    onDayClick = { /* scroll to that day in schedule tab */ },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // 4. Overview card
        item(key = "overview") {
            AnimatedVisibility(
                visible = revealStep >= 4,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                OverviewCard(
                    activeTodoCount = activeTodos.size,
                    completedTodoCount = completedTodoCount,
                    scheduleCount = todaySchedules.size,
                    noteCount = recentNotes.size,
                    onTodoClick = onNavigateToTodos,
                    onScheduleClick = onNavigateToSchedule,
                    onNoteClick = onNavigateToNotes,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // 5. Quick actions
        item(key = "quick_actions") {
            AnimatedVisibility(
                visible = revealStep >= 5,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                QuickActionChip(
                    onNewNote = onNewNote,
                    onNewTodo = onNewTodo,
                    onNewSchedule = onNewSchedule
                )
            }
        }

        // 6. Today's schedule (compact horizontal cards)
        if (todaySchedules.isNotEmpty()) {
            item(key = "schedule_header") {
                AnimatedVisibility(
                    visible = revealStep >= 6,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    SectionHeader("今日日程", onViewAll = onNavigateToSchedule)
                }
            }
            item(key = "schedule_list") {
                AnimatedVisibility(
                    visible = revealStep >= 6,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    androidx.compose.foundation.lazy.LazyRow(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(todaySchedules.take(5), key = { it.id }) { schedule ->
                            CompactScheduleCard(
                                title = schedule.title,
                                startTime = schedule.startTime,
                                endTime = schedule.endTime,
                                onClick = { /* navigate to schedule detail */ }
                            )
                        }
                    }
                }
            }
        }

        // 7. Active todos
        item(key = "todo_header") {
            AnimatedVisibility(
                visible = revealStep >= 7,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                SectionHeader("待办事项", onViewAll = onNavigateToTodos)
            }
        }
        if (activeTodos.isEmpty()) {
            item(key = "todo_empty") {
                AnimatedVisibility(
                    visible = revealStep >= 7,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    EmptyHint("没有待办事项 ✨")
                }
            }
        } else {
            items(activeTodos.take(5), key = { it.id }) { todo ->
                AnimatedVisibility(
                    visible = revealStep >= 7,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    TodoRow(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo) },
                        onClick = { onTodoClick(todo.id) },
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        // 8. Recent notes
        item(key = "notes_header") {
            AnimatedVisibility(
                visible = revealStep >= 8,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                SectionHeader("最近笔记", onViewAll = onNavigateToNotes)
            }
        }
        if (recentNotes.isEmpty()) {
            item(key = "notes_empty") {
                AnimatedVisibility(
                    visible = revealStep >= 8,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    EmptyHint("没有笔记 📝")
                }
            }
        } else {
            // 2-column grid using Row pairs
            val notesChunks = recentNotes.take(6).chunked(2)
            items(notesChunks, key = { it.firstOrNull()?.id ?: 0L }) { pair ->
                AnimatedVisibility(
                    visible = revealStep >= 8,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                        } ?: Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = onViewAll) {
            Text("查看全部", style = MaterialTheme.typography.labelLarge)
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

@Composable
private fun CompactScheduleCard(
    title: String,
    startTime: Long,
    endTime: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Text(
                text = formatTime(startTime),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .padding(horizontal = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
