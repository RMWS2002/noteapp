package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Two side-by-side stat cards replacing the old OverviewCard. */
@Composable
fun StatsRow(
    activeTodoCount: Int,
    completedTodoCount: Int,
    scheduleCount: Int,
    onTodoClick: () -> Unit,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTodos = activeTodoCount + completedTodoCount
    val progress = if (totalTodos > 0) completedTodoCount.toFloat() / totalTodos else 0f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        // Todo stat card
        StatCard(
            modifier = Modifier.weight(1f),
            onClick = onTodoClick,
            label = "待办",
            value = if (totalTodos > 0) "${completedTodoCount}/${totalTodos}" else "0",
            subtitle = if (totalTodos > 0) "${(progress * 100).toInt()}% 完成" else "暂无待办"
        ) {
            if (totalTodos > 0) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                )
            }
        }

        // Schedule stat card
        StatCard(
            modifier = Modifier.weight(1f),
            onClick = onScheduleClick,
            label = "日程",
            value = "$scheduleCount",
            subtitle = "今日"
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    onClick: () -> Unit,
    label: String,
    value: String,
    subtitle: String,
    bottom: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (bottom != null) {
                Spacer(Modifier.height(10.dp))
                bottom()
            }
        }
    }
}
