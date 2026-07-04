package com.rmws2002.noteapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.calendar.SystemCalendarEvent
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import com.rmws2002.noteapp.ui.components.EventDetailSheet
import com.rmws2002.noteapp.ui.util.formatTime
import com.rmws2002.noteapp.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
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

    // Refresh system events on resume
    LaunchedEffect(Unit) { viewModel.refreshSystemEvents() }

    // Merge all event dates for date strip dots
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
            // ── Horizontal date strip ──
            HorizontalDateStrip(
                selectedDate = selectedDate,
                eventDates = allEventDates,
                onDateSelected = { viewModel.selectDate(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            // ── Selected date label ──
            val dateFormat = remember { SimpleDateFormat("M月d日 EEEE", Locale.CHINESE) }
            Text(
                text = dateFormat.format(Date(selectedDate)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            if (!hasAnyEvents) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        "当天没有日程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // ── Timeline: local schedules + system calendar events ──
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp)
                ) {
                    // Local schedules
                    items(daySchedules, key = { "local_${it.id}" }) { schedule ->
                        TimelineItem(
                            title = schedule.title,
                            startTime = schedule.startTime,
                            endTime = schedule.endTime,
                            description = schedule.description,
                            isSystemEvent = false,
                            onClick = { selectedEvent = schedule }
                        )
                    }
                    // System calendar events
                    items(daySystemEvents, key = { "sys_${it.id}" }) { event ->
                        TimelineItem(
                            title = event.title,
                            startTime = event.startTime,
                            endTime = event.endTime,
                            description = event.calendarName,
                            isSystemEvent = true,
                            onClick = {}
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // FAB
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

// ── Horizontal date strip ──
@Composable
private fun HorizontalDateStrip(
    selectedDate: Long,
    eventDates: Set<LocalDate>,
    onDateSelected: (Long) -> Unit
) {
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
    }
    val dayNames = listOf("日", "一", "二", "三", "四", "五", "六")

    val days = remember {
        (0 until 14).map { offset ->
            Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_MONTH, offset - 3)
            }
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(days) { cal ->
            val ts = cal.timeInMillis
            val isSelected = ts == selectedDate
            val isToday = cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            val date = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()
            val hasEvents = date in eventDates

            Column(
                modifier = Modifier
                    .width(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onDateSelected(ts) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${cal.get(Calendar.DAY_OF_MONTH)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                           else if (isToday) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                // Event dot
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasEvents) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                )
            }
        }
    }
}

// ── Timeline item: works for both local schedules and system events ──
@Composable
private fun TimelineItem(
    title: String,
    startTime: Long,
    endTime: Long,
    description: String,
    isSystemEvent: Boolean,
    onClick: () -> Unit
) {
    val dotColor = if (isSystemEvent)
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Time column
        Column(
            modifier = Modifier.width(52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(startTime),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier.size(8.dp).padding(top = 6.dp).clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.width(8.dp))

        // Card
        Card(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSystemEvent) {
                        Text(
                            "📅",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "${formatTime(startTime)} - ${formatTime(endTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
