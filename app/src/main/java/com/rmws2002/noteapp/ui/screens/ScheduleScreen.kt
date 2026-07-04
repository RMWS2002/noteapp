package com.rmws2002.noteapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.calendar.SystemCalendarEvent
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import com.rmws2002.noteapp.ui.components.EventDetailSheet
import com.rmws2002.noteapp.ui.components.TimelineView
import com.rmws2002.noteapp.ui.util.formatDate
import com.rmws2002.noteapp.ui.util.formatTime
import com.rmws2002.noteapp.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScheduleScreen(
    onAddSchedule: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val daySchedules by viewModel.getSchedulesForDay(selectedDate).collectAsState(initial = emptyList())
    val systemEvents by viewModel.systemEvents.collectAsState()
    val daySystemEvents = viewModel.getSystemEventsForDay(selectedDate)
    var selectedEvent by remember { mutableStateOf<ScheduleEntity?>(null) }

    // Refresh system events on screen resume
    LaunchedEffect(Unit) {
        viewModel.refreshSystemEvents()
    }

    // Merge system event dates into calendar dots (handle multi-day events)
    val allEventDates = remember(allSchedules, systemEvents) {
        val local = allSchedules.flatMap { s ->
            val start = Instant.ofEpochMilli(s.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(s.endTime).atZone(ZoneId.systemDefault()).toLocalDate()
            generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
        }.toSet()
        val sys = systemEvents.flatMap { e ->
            val start = Instant.ofEpochMilli(e.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(e.endTime).atZone(ZoneId.systemDefault()).toLocalDate()
            generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
        }.toSet()
        local + sys
    }

    val hasAnyEvents = daySchedules.isNotEmpty() || daySystemEvents.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalendarWidget(
                selectedDate = selectedDate,
                eventDates = allEventDates,
                onDateSelected = { viewModel.selectDate(it) }
            )

            Text(
                text = formatDate(selectedDate),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (!hasAnyEvents) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) { EmptyHint("当天没有日程") }
            } else {
                TimelineView(
                    schedules = daySchedules,
                    systemEvents = daySystemEvents,
                    onEventClick = { selectedEvent = it },
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)
                )
            }
        }

        FloatingActionButton(
            onClick = onAddSchedule,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加日程")
        }
    }

    selectedEvent?.let { event ->
        EventDetailSheet(
            event = event,
            onDismiss = { selectedEvent = null },
            onDelete = { viewModel.deleteSchedule(it) }
        )
    }
}

@Composable
fun CalendarWidget(
    selectedDate: Long,
    eventDates: Set<LocalDate>,
    onDateSelected: (Long) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthFormat = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINESE)
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    val today = LocalDate.now()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上月")
            }
            Text(currentMonth.format(monthFormat), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下月")
            }
        }

        Row(Modifier.fillMaxWidth()) {
            dayNames.forEach { day ->
                Text(day, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(4.dp))

        val firstDayOfMonth = currentMonth.atDay(1)
        val dow = firstDayOfMonth.dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()
        val totalCells = dow + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - dow + 1
                    if (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day)
                        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val isSelected = millis == selectedDate
                        val isToday = date == today
                        val hasEvent = date in eventDates

                        Column(Modifier.weight(1f).padding(3.dp).clip(CircleShape)
                            .background(when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            })
                            .clickable { onDateSelected(millis) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(day.toString(), style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            if (hasEvent) Box(Modifier.size(5.dp).padding(bottom = 6.dp)
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary, CircleShape))
                            else Spacer(Modifier.size(5.dp).padding(bottom = 6.dp))
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
