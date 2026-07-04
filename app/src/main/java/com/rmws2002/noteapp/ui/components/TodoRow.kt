package com.rmws2002.noteapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.ui.util.formatShortDate

@Composable
fun TodoRow(
    todo: TodoEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetAlpha = if (todo.isCompleted) 1f else 0f
    val animProgress by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 400),
        label = "strike"
    )

    val completedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.onSurface
    val targetColor = if (todo.isCompleted) completedColor else activeColor
    val textColor = if (animProgress > 0f) {
        lerpColor(activeColor, targetColor, animProgress)
    } else {
        targetColor
    }

    val strikeColor = completedColor
    val strikeAlpha = animProgress

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.padding(4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (animProgress > 0.5f) TextDecoration.LineThrough else TextDecoration.None,
                color = textColor,
                modifier = Modifier
                    .weight(1f)
                    .drawWithContent {
                        drawContent()
                        if (strikeAlpha > 0f) {
                            val lineY = size.height / 2
                            val lineEnd = size.width * strikeAlpha
                            drawLine(
                                color = strikeColor.copy(alpha = strikeAlpha),
                                start = Offset(0f, lineY),
                                end = Offset(lineEnd, lineY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
            )
            if (todo.dueDate != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (todo.reminderTime != null) {
                        Text(
                            text = "🔔",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = if (todo.hasTime) {
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = todo.dueDate }
                            String.format("%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
                        } else {
                            formatShortDate(todo.dueDate)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (todo.dueDate != null && todo.dueDate < System.currentTimeMillis() && !todo.isCompleted)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}
