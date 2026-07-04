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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import com.rmws2002.noteapp.ui.components.EventDetailSheet
import com.rmws2002.noteapp.ui.components.TimelineView
import com.rmws2002.noteapp.ui.util.formatDate
import com.rmws2002.noteapp.viewmodel.ScheduleViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onAddSchedule: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val daySchedules by viewModel.getSchedulesForDay(selectedDate).collectAsState(initial = emptyList())
    var selectedEvent by remember { mutableStateOf<ScheduleEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日程") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSchedule,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加日程")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CalendarWidget(
                selectedDate = selectedDate,
                schedules = allSchedules,
                onDateSelected = { viewModel.selectDate(it) }
            )

            // Date text
            Text(
                text = formatDate(selectedDate),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (daySchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    EmptyHint("当天没有日程")
                }
            } else {
                TimelineView(
                    schedules = daySchedules,
                    onEventClick = { selectedEvent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Event detail bottom sheet
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
    schedules: List<ScheduleEntity>,
    onDateSelected: (Long) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthFormat = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINESE)
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")

    // Get event dates for current month
    val monthStart = currentMonth.atDay(1)
    val monthEnd = currentMonth.atEndOfMonth()
    val eventDates = remember(schedules, currentMonth) {
        schedules.filter { s ->
            val date = java.time.Instant.ofEpochMilli(s.startTime)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            date >= monthStart && date <= monthEnd
        }.map { s ->
            java.time.Instant.ofEpochMilli(s.startTime)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        }.toSet()
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上月")
            }
            Text(
                text = currentMonth.format(monthFormat),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下月")
            }
        }

        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val dow = firstDayOfMonth.dayOfWeek.value - 1 // Monday=1 -> 0-index
        val daysInMonth = currentMonth.lengthOfMonth()
        val today = LocalDate.now()

        val totalCells = dow + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - dow + 1

                    if (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day)
                        val isSelected = date.atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant().toEpochMilli() == selectedDate
                        val isToday = date == today
                        val hasEvent = date in eventDates

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .clickable {
                                    val millis = date.atStartOfDay(java.time.ZoneId.systemDefault())
                                        .toInstant().toEpochMilli()
                                    onDateSelected(millis)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            // Event dot
                            if (hasEvent) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .padding(bottom = 3.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
