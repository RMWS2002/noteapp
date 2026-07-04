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
    val timestamp: Long,
    val dayLabel: String,
    val dateNumber: Int,
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
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(weekDays) { _, day ->
            MiniDayCell(day = day, onClick = { onDayClick(day.timestamp) })
        }
    }
}

@Composable
private fun MiniDayCell(day: DayInfo, onClick: () -> Unit) {
    val bgColor = if (day.isToday)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    else
        MaterialTheme.colorScheme.surface

    val textColor = if (day.isToday)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .width(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (day.isToday) 1.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.dayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${day.dateNumber}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (day.hasEvents) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

fun buildWeekDays(eventDates: Set<Long>): List<DayInfo> {
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    val result = mutableListOf<DayInfo>()

    for (i in 0 until 7) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, i)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val ts = cal.timeInMillis
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val labelIndex = if (dow == 1) 6 else dow - 2
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
