package com.rmws2002.noteapp.ui.components

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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.ui.util.formatShortDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TodoRow(
    todo: TodoEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkScale = remember { Animatable(1f) }
    // These mutable floats drive the animated strikethrough and alpha
    var strikeProgress by remember { mutableFloatStateOf(if (todo.isCompleted) 1f else 0f) }
    var dimAlpha by remember { mutableFloatStateOf(if (todo.isCompleted) 0.55f else 1f) }
    var cardAlpha by remember { mutableFloatStateOf(if (todo.isCompleted) 0.6f else 1f) }

    LaunchedEffect(todo.isCompleted) {
        if (todo.isCompleted) {
            // Sequence: checkbox bounce → strike grows → fade
            // Step 1: checkbox shrink
            checkScale.snapTo(1f)
            checkScale.animateTo(0.2f, tween(100))
            // Step 2: spring bounce
            checkScale.animateTo(1.15f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
            checkScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            // Step 3: strike-through grows left to right
            val strikeAnim = Animatable(0f)
            launch {
                strikeAnim.animateTo(1f, tween(250))
            }
            // Sample the animatable value each frame
            while (strikeAnim.value < 0.99f) {
                strikeProgress = strikeAnim.value
                delay(16) // ~60fps
            }
            strikeProgress = 1f
            // Step 4: text & card dim
            dimAlpha = 0.55f
            cardAlpha = 0.6f
        } else {
            // Reset
            strikeProgress = 0f
            dimAlpha = 1f
            cardAlpha = 1f
            checkScale.snapTo(1f)
        }
    }

    val isOverdue = todo.dueDate != null && todo.dueDate < System.currentTimeMillis() && !todo.isCompleted
    val dateColor = if (isOverdue) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.onSurfaceVariant
    // Pre-capture colors for drawWithContent (not a @Composable context)
    val strikeLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (todo.isCompleted) 0.3f else 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(checkScale.value),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.width(4.dp))

            // Title with animated strikethrough
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (strikeProgress > 0.8f) TextDecoration.LineThrough else TextDecoration.None,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = dimAlpha),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        // Only use drawWithContent during active animation (0→1 growth)
                        if (strikeProgress > 0.01f && strikeProgress < 0.99f) {
                            Modifier.drawWithContent {
                                drawContent()
                                val lineY = size.height / 2
                                val lineEnd = size.width * strikeProgress
                                drawLine(
                                    color = strikeLineColor,
                                    start = Offset(0f, lineY),
                                    end = Offset(lineEnd, lineY),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                        } else Modifier
                    )
            )

            // Due date
            if (todo.dueDate != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (todo.reminderTime != null && !todo.isCompleted) {
                        Text("🔔", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.width(2.dp))
                    }
                    Text(
                        text = if (todo.hasTime) {
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = todo.dueDate }
                            String.format("%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
                        } else {
                            formatShortDate(todo.dueDate)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else dateColor
                    )
                }
            }
        }
    }
}
