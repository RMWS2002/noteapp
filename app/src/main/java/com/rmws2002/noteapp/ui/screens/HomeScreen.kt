package com.rmws2002.noteapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.rmws2002.noteapp.ui.components.DailyQuote
import com.rmws2002.noteapp.ui.components.GreetingHeader
import com.rmws2002.noteapp.ui.components.MiniWeekStrip
import com.rmws2002.noteapp.ui.components.QuickActionChip
import com.rmws2002.noteapp.ui.components.StatsRow
import com.rmws2002.noteapp.ui.components.TodoRow
import com.rmws2002.noteapp.ui.components.buildWeekDays
import com.rmws2002.noteapp.ui.util.formatTime
import com.rmws2002.noteapp.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToTodos: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onTodoClick: (Long) -> Unit,
    onNewTodo: () -> Unit = {},
    onNewSchedule: () -> Unit = {},
    onDayClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val activeTodos by viewModel.activeTodos.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()
    val completedTodoCount by viewModel.completedTodoCount.collectAsState()
    val weekEventDates by viewModel.weekEventDates.collectAsState()
    val weekDays = remember(weekEventDates) { buildWeekDays(weekEventDates) }

    // Staggered reveal: 1=greeting+quote, 2=week+stats, 3=actions, 4=schedule, 5=todos
    var revealStep by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        revealStep = 1; delay(80)
        revealStep = 2; delay(80)
        revealStep = 3; delay(80)
        revealStep = 4; delay(80)
        revealStep = 5
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 1. Greeting + Quote group
        item(key = "greeting") {
            AnimatedVisibility(
                visible = revealStep >= 1,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                Column {
                    GreetingHeader()
                    Spacer(Modifier.height(10.dp))
                    DailyQuote()
                }
            }
        }

        // 2. Mini week strip + Stats row
        item(key = "week_stats") {
            AnimatedVisibility(
                visible = revealStep >= 2,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    MiniWeekStrip(weekDays = weekDays, onDayClick = onDayClick)
                    Spacer(Modifier.height(14.dp))
                    StatsRow(
                        activeTodoCount = activeTodos.size,
                        completedTodoCount = completedTodoCount,
                        scheduleCount = todaySchedules.size,
                        onTodoClick = onNavigateToTodos,
                        onScheduleClick = onNavigateToSchedule
                    )
                }
            }
        }

        // 3. Quick actions
        item(key = "quick_actions") {
            AnimatedVisibility(
                visible = revealStep >= 3,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                QuickActionChip(
                    onNewTodo = onNewTodo,
                    onNewSchedule = onNewSchedule
                )
            }
        }

        // 4. Today's schedule
        if (todaySchedules.isNotEmpty()) {
            item(key = "schedule_header") {
                AnimatedVisibility(
                    visible = revealStep >= 4,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    SectionHeader("今日日程", onViewAll = onNavigateToSchedule)
                }
            }
            item(key = "schedule_list") {
                AnimatedVisibility(
                    visible = revealStep >= 4,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
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
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }

        // 5. Active todos
        item(key = "todo_header") {
            AnimatedVisibility(
                visible = revealStep >= 5,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                SectionHeader("待办事项", onViewAll = onNavigateToTodos)
            }
        }
        if (activeTodos.isEmpty()) {
            item(key = "todo_empty") {
                AnimatedVisibility(
                    visible = revealStep >= 5,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    EmptyHint("没有待办事项")
                }
            }
        } else {
            items(activeTodos.take(5), key = { it.id }) { todo ->
                AnimatedVisibility(
                    visible = revealStep >= 5,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
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

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
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
    title: String, startTime: Long, endTime: Long, onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(200.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatTime(startTime),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(
                modifier = Modifier.width(1.dp).height(24.dp).padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
