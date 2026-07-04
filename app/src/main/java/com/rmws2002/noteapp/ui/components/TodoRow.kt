package com.rmws2002.noteapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
    // Checkbox spring scale animation
    val checkScale = remember { Animatable(if (todo.isCompleted) 1f else 1f) }
    LaunchedEffect(todo.isCompleted) {
        if (todo.isCompleted) {
            checkScale.animateTo(0.3f, tween(100))
            checkScale.animateTo(1.25f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            checkScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    // Text color animation: onSurface → onSurfaceVariant (muted)
    val textColor by animateColorAsState(
        targetValue = if (todo.isCompleted)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(350),
        label = "textColor"
    )

    // Card alpha: completed cards are dimmer
    val cardAlpha by animateColorAsState(
        targetValue = if (todo.isCompleted)
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(350),
        label = "cardBg"
    )

    // Due date color: red if overdue and not completed
    val isOverdue = todo.dueDate != null && todo.dueDate < System.currentTimeMillis() && !todo.isCompleted
    val dateColor = if (isOverdue)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (todo.isCompleted) 0.65f else 1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardAlpha),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(
                alpha = if (todo.isCompleted) 0.4f else 1f
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(checkScale.value),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            if (todo.dueDate != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (todo.reminderTime != null && !todo.isCompleted) {
                        Text(
                            text = "🔔",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = if (todo.hasTime) {
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = todo.dueDate }
                            String.format(
                                "%02d:%02d",
                                cal.get(java.util.Calendar.HOUR_OF_DAY),
                                cal.get(java.util.Calendar.MINUTE)
                            )
                        } else {
                            formatShortDate(todo.dueDate)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else dateColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
    }
}
