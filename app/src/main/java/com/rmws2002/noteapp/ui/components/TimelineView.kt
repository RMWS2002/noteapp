package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import com.rmws2002.noteapp.ui.util.formatTime
import java.util.Calendar

@Composable
fun TimelineView(
    schedules: List<ScheduleEntity>,
    onEventClick: (ScheduleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeLineColor = MaterialTheme.colorScheme.outlineVariant

    // Auto-scroll to current hour
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val startHour = 6
    val initialIndex = (currentHour - startHour).coerceIn(0, 17)

    LaunchedEffect(Unit) {
        if (schedules.isEmpty()) {
            listState.animateScrollToItem(initialIndex.coerceAtMost(17))
        }
    }

    // Group schedules by start hour
    val scheduleByHour: Map<Int, List<ScheduleEntity>> = schedules.groupBy { s ->
        Calendar.getInstance().apply { timeInMillis = s.startTime }
            .get(Calendar.HOUR_OF_DAY)
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        // Hour slots from 6:00 to 23:00
        for (hour in startHour until 24) {
            val events = scheduleByHour[hour].orEmpty()
            val displayHour = if (hour > 12) hour - 12 else hour
            val amPm = if (hour < 12) "上午" else if (hour < 18) "下午" else "晚上"

            item(key = "hour_$hour") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (events.isEmpty()) 28.dp else 8.dp + (events.size * 72).dp)
                        .padding(start = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Time label column
                    Text(
                        text = "${displayHour}:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )

                    // Timeline line
                    Canvas(modifier = Modifier
                        .width(16.dp)
                        .height(if (events.isEmpty()) 28.dp else 8.dp)) {
                        val x = size.width / 2
                        drawCircle(
                            color = timeLineColor,
                            radius = 3.dp.toPx()
                        )
                        drawLine(
                            color = timeLineColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Events or empty space
                    if (events.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            events.forEach { event ->
                                TimelineEventCard(
                                    event = event,
                                    onClick = { onEventClick(event) },
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEventCard(
    event: ScheduleEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatTime(event.startTime)} - ${formatTime(event.endTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
