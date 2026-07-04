package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar

data class DayInfo(
    val timestamp: Long,      // start-of-day millis
    val dayLabel: String,     // "一", "二", ..., "日"
    val dateNumber: Int,      // 1-31
    val hasEvents: Boolean,
    val isToday: Boolean
)

@Composable
fun MiniWeekStrip(
    weekDays: List<DayInfo>,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(weekDays) { _, day ->
            MiniDayCell(
                day = day,
                onClick = { onDayClick(day.timestamp) }
            )
        }
    }
}

@Composable
private fun MiniDayCell(
    day: DayInfo,
    onClick: () -> Unit
) {
    val bgColor = if (day.isToday)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    val textColor = if (day.isToday)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .width(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.dayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${day.dateNumber}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            // Dot indicator
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        if (day.hasEvents)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

/** Build list of 7 DayInfo entries starting from today. */
fun buildWeekDays(eventDates: Set<Long>): List<DayInfo> {
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    val today = Calendar.getInstance()
    val result = mutableListOf<DayInfo>()

    for (i in 0 until 7) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, i)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val ts = cal.timeInMillis
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun..7=Sat
        val labelIndex = if (dayOfWeek == 1) 6 else dayOfWeek - 2 // map to Mon=0..Sun=6
        result.add(
            DayInfo(
                timestamp = ts,
                dayLabel = dayNames[labelIndex],
                dateNumber = cal.get(Calendar.DAY_OF_MONTH),
                hasEvents = ts in eventDates,
                isToday = i == 0
            )
        )
    }
    return result
}
