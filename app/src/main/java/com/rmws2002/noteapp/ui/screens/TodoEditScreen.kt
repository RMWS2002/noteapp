package com.rmws2002.noteapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.ui.components.parseColor
import com.rmws2002.noteapp.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINESE)
private val fullDateFormat = SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINESE)

data class ReminderOption(val label: String, val minutesBefore: Int?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(
    todoId: Long?,
    onBack: () -> Unit,
    viewModel: TodoViewModel = viewModel()
) {
    val tags by viewModel.tags.collectAsState()
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var hasTime by remember { mutableStateOf(false) }
    var reminderMinutes by remember { mutableStateOf<Int?>(null) }
    var selectedTagId by remember { mutableStateOf<Long?>(null) }
    var hasChanges by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }
    var initialLoaded by remember { mutableStateOf(false) }

    val reminderOptions = listOf(
        ReminderOption("不提醒", null),
        ReminderOption("到点提醒", 0),
        ReminderOption("5分钟前", 5),
        ReminderOption("15分钟前", 15),
        ReminderOption("30分钟前", 30),
        ReminderOption("1小时前", 60),
        ReminderOption("1天前", 1440)
    )

    LaunchedEffect(todoId) {
        if (todoId != null && todoId > 0 && !initialLoaded) {
            val todo = viewModel.allTodos.value.find { it.id == todoId }
            todo?.let {
                title = it.title
                dueDate = it.dueDate
                hasTime = it.hasTime
                selectedTagId = it.tagId
                // Derive reminder minutes from reminderTime if present
                if (it.reminderTime != null && it.dueDate != null) {
                    reminderMinutes = ((it.dueDate - it.reminderTime) / 60000).toInt()
                }
                initialLoaded = true
            }
        }
    }

    fun saveAndBack() {
        if (title.isNotBlank()) {
            val reminderTime = if (reminderMinutes != null && dueDate != null) {
                dueDate!! - (reminderMinutes!! * 60 * 1000L)
            } else null
            if (todoId == null || todoId == 0L) {
                viewModel.saveTodo(title, dueDate, hasTime, reminderTime, selectedTagId)
            } else {
                viewModel.updateTodo(todoId, title, dueDate, hasTime, reminderTime, selectedTagId)
            }
        }
        onBack()
    }

    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("放弃更改？") },
            text = { Text("当前编辑的内容还没有保存") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                    Text("放弃", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("继续编辑") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除这条待办？") },
            text = { Text("此操作无法撤销") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTodo(TodoEntity(id = todoId ?: 0, title = title))
                    onBack()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { d ->
                        // Preserve time of day if already set
                        val cal = Calendar.getInstance().apply { timeInMillis = d }
                        if (dueDate != null) {
                            val oldCal = Calendar.getInstance().apply { timeInMillis = dueDate!! }
                            cal.set(Calendar.HOUR_OF_DAY, oldCal.get(Calendar.HOUR_OF_DAY))
                            cal.set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE))
                        } else {
                            cal.set(Calendar.HOUR_OF_DAY, 23)
                            cal.set(Calendar.MINUTE, 59)
                        }
                        dueDate = cal.timeInMillis
                        hasChanges = true
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = state) }
    }

    // Time picker dialog
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dueDate ?: System.currentTimeMillis()
        }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(Modifier.padding(24.dp)) {
                Text("选择具体时间", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                TimePicker(state = timeState)
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showTimePicker = false }) { Text("取消") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        val c = Calendar.getInstance().apply {
                            timeInMillis = dueDate ?: System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        dueDate = c.timeInMillis
                        hasChanges = true
                        showTimePicker = false
                    }) { Text("确定") }
                }
            }
        }
    }

    val isEditing = todoId != null && todoId > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑待办" else "新建待办", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = {
                        if (hasChanges) showDiscardDialog = true else onBack()
                    }) { Text("取消", color = MaterialTheme.colorScheme.primary) }
                },
                actions = {
                    TextButton(onClick = { saveAndBack() }) {
                        Text("保存", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            // Title - large, borderless input
            androidx.compose.material3.TextField(
                value = title,
                onValueChange = { title = it; hasChanges = true },
                placeholder = { Text("待办内容", style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )) },
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            // ---- QUICK DATE PRESETS ----
            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val now = System.currentTimeMillis()
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val tomorrowStart = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val nextWeekStart = Calendar.getInstance().apply {
                    add(Calendar.WEEK_OF_YEAR, 1)
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val presets = listOf(
                    "今天" to todayStart,
                    "明天" to tomorrowStart,
                    "下周" to nextWeekStart
                )
                presets.forEach { (label, time) ->
                    val isActive = dueDate != null && dueDate == time
                    FilterChip(
                        selected = isActive,
                        onClick = {
                            dueDate = time
                            hasChanges = true
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                // Custom date picker chip
                FilterChip(
                    selected = dueDate != null && dueDate !in presets.map { it.second },
                    onClick = { showDatePicker = true },
                    label = { Text("选日期", style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // ---- DUE DATE ROW ----
            SettingRow(
                icon = { Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary) },
                label = "截止日期",
                value = if (dueDate != null) {
                    if (hasTime) fullDateFormat.format(Date(dueDate!!))
                    else dateFormat.format(Date(dueDate!!))
                } else "不设截止",
                valueColor = if (dueDate != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { showDatePicker = true }
            )

            if (dueDate != null) {
                Spacer(Modifier.height(4.dp))

                // All-day toggle
                Row(
                    Modifier.fillMaxWidth().padding(start = 52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("全天", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = !hasTime,
                        onCheckedChange = { isAllDay ->
                            hasTime = !isAllDay; hasChanges = true
                        }
                    )
                }

                if (hasTime) {
                    // Time picker
                    SettingRow(
                        icon = { Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary) },
                        label = "具体时间",
                        value = timeFormat.format(Date(dueDate!!)),
                        onClick = { showTimePicker = true }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ---- REMINDER ROW ----
            if (dueDate != null) {
                SettingRow(
                    icon = { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary) },
                    label = "提醒",
                    value = reminderOptions.firstOrNull { it.minutesBefore == reminderMinutes }?.label ?: "不提醒",
                    valueColor = if (reminderMinutes != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { showReminderMenu = true }
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            // ---- TAG ROW ----
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Label, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(20.dp))
                Text("标签", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(72.dp))
                LazyRow {
                    items(tags) { tag ->
                        val isSelected = selectedTagId == tag.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTagId = if (isSelected) null else tag.id
                                hasChanges = true
                            },
                            label = { Text(tag.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = parseColor(tag.color).copy(alpha = 0.15f),
                                selectedContainerColor = parseColor(tag.color),
                                labelColor = parseColor(tag.color),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ---- DELETE BUTTON (edit mode only) ----
            if (isEditing) {
                Spacer(Modifier.height(24.dp))
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("删除待办", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Reminder dropdown
    androidx.compose.material3.DropdownMenu(
        expanded = showReminderMenu,
        onDismissRequest = { showReminderMenu = false }
    ) {
        reminderOptions.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                    reminderMinutes = option.minutesBefore
                    hasChanges = true
                    showReminderMenu = false
                },
                leadingIcon = if (option.minutesBefore == reminderMinutes) {
                    { Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary) }
                } else null
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    onClick: () -> Unit = {},
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    showArrow: Boolean = true
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(20.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(72.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            modifier = Modifier.weight(1f))
        if (showArrow) {
            Text("›", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
