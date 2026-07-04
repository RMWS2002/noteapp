package com.rmws2002.noteapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.viewmodel.ScheduleViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    onBack: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableStateOf(System.currentTimeMillis() + 3600000) }
    var hasChanges by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Pre-set to next hour on first composition
    val initialized = remember { mutableStateOf(false) }
    if (!initialized.value) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        startTime = cal.timeInMillis
        endTime = cal.timeInMillis + 3600000
        initialized.value = true
    }

    fun saveAndBack() {
        if (title.isNotBlank()) {
            viewModel.saveSchedule(title, description, startTime, endTime)
        }
        onBack()
    }

    fun formatDate(ts: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        return "${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日" +
               " 周${listOf("日","一","二","三","四","五","六")[cal.get(Calendar.DAY_OF_WEEK) - 1]}"
    }
    fun formatTime(ts: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }
    fun formatDuration(): String {
        val diff = endTime - startTime
        val mins: Long = diff / 60000L
        return when {
            mins < 60L -> "${mins}分钟"
            mins % 60L == 0L -> "${mins / 60L}小时"
            else -> "${mins / 60L}小时${mins % 60L}分钟"
        }
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

    // Date picker
    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startTime)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { d ->
                        val diff = endTime - startTime
                        val cal = Calendar.getInstance().apply { timeInMillis = d }
                        val oldCal = Calendar.getInstance().apply { timeInMillis = startTime }
                        cal.set(Calendar.HOUR_OF_DAY, oldCal.get(Calendar.HOUR_OF_DAY))
                        cal.set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE))
                        startTime = cal.timeInMillis
                        endTime = startTime + diff
                        hasChanges = true
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = state) }
    }

    // Start time picker
    if (showStartTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = startTime }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Column(Modifier.padding(24.dp).clip(RoundedCornerShape(16.dp))) {
                Text("开始时间", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                TimePicker(state = timeState)
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showStartTimePicker = false }) { Text("取消") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        val diff = endTime - startTime
                        val c = Calendar.getInstance().apply { timeInMillis = startTime }
                        c.set(Calendar.HOUR_OF_DAY, timeState.hour)
                        c.set(Calendar.MINUTE, timeState.minute)
                        startTime = c.timeInMillis
                        endTime = startTime + diff.coerceAtLeast(300000) // min 5 min
                        hasChanges = true
                        showStartTimePicker = false
                    }) { Text("确定") }
                }
            }
        }
    }

    // End time picker
    if (showEndTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = endTime }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Column(Modifier.padding(24.dp).clip(RoundedCornerShape(16.dp))) {
                Text("结束时间", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                TimePicker(state = timeState)
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showEndTimePicker = false }) { Text("取消") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = endTime }
                        c.set(Calendar.HOUR_OF_DAY, timeState.hour)
                        c.set(Calendar.MINUTE, timeState.minute)
                        endTime = c.timeInMillis.coerceAtLeast(startTime + 300000)
                        hasChanges = true
                        showEndTimePicker = false
                    }) { Text("确定") }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建日程", style = MaterialTheme.typography.titleLarge) },
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

            // ── Title: borderless, headline style ──
            TextField(
                value = title,
                onValueChange = { title = it; hasChanges = true },
                placeholder = {
                    Text("日程标题",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            // ── Quick date presets ──
            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
                val todayEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
                val tomorrowStart = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0)
                }.timeInMillis
                val tomorrowEnd = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0)
                }.timeInMillis
                val afterTomorrowStart = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 2)
                    set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0)
                }.timeInMillis
                val afterTomorrowEnd = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 2)
                    set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0)
                }.timeInMillis

                val presets = listOf(
                    Triple("今天", todayStart, todayEnd),
                    Triple("明天", tomorrowStart, tomorrowEnd),
                    Triple("后天", afterTomorrowStart, afterTomorrowEnd)
                )
                presets.forEach { (label, s, e) ->
                    val isActive = startTime == s && endTime == e
                    FilterChip(
                        selected = isActive,
                        onClick = {
                            startTime = s; endTime = e
                            hasChanges = true
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                // Custom date chip
                FilterChip(
                    selected = startTime !in presets.flatMap { listOf(it.second) },
                    onClick = { showDatePicker = true },
                    label = { Text("选日期", style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // ── Date ──
            SettingRow(
                icon = { Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary) },
                label = "日期",
                value = formatDate(startTime),
                onClick = { showDatePicker = true }
            )

            Spacer(Modifier.height(2.dp))

            // ── Start time ──
            SettingRow(
                icon = { Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary) },
                label = "开始",
                value = formatTime(startTime),
                onClick = { showStartTimePicker = true }
            )

            Spacer(Modifier.height(2.dp))

            // ── End time (with duration) ──
            SettingRow(
                icon = { Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.error) },
                label = "结束",
                value = "${formatTime(endTime)}  ·  ${formatDuration()}",
                onClick = { showEndTimePicker = true }
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            // ── Description ──
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(20.dp))
                Text("详情", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(72.dp))
            }

            // Description card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .padding(4.dp)
            ) {
                TextField(
                    value = description,
                    onValueChange = { description = it; hasChanges = true },
                    placeholder = {
                        Text("日程详情、地点、备注...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    minLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = "${description.length} 字",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 12.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    onClick: () -> Unit = {},
    valueColor: Color = MaterialTheme.colorScheme.onSurface
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
        Text("›", style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
