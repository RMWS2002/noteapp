package com.rmws2002.noteapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    // Pre-set to next hour
    val initialCal = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    if (startTime == System.currentTimeMillis()) {
        startTime = initialCal.timeInMillis
        endTime = initialCal.apply { add(Calendar.HOUR_OF_DAY, 1) }.timeInMillis
    }

    fun saveAndBack() {
        if (title.isNotBlank()) {
            viewModel.saveSchedule(title, description, startTime, endTime)
        }
        onBack()
    }

    fun formatDate(ts: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        return "${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"
    }
    fun formatTime(ts: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }
    fun formatDuration(): String {
        val diff = endTime - startTime
        val mins = diff / 60000
        return if (mins >= 60) "${mins / 60}小时${mins % 60}分钟" else "${mins}分钟"
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
            Column(Modifier.padding(24.dp)) {
                Text("选择开始时间", style = MaterialTheme.typography.titleMedium)
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
                        endTime = startTime + diff
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
            Column(Modifier.padding(24.dp)) {
                Text("选择结束时间", style = MaterialTheme.typography.titleMedium)
                TimePicker(state = timeState)
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showEndTimePicker = false }) { Text("取消") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = endTime }
                        c.set(Calendar.HOUR_OF_DAY, timeState.hour)
                        c.set(Calendar.MINUTE, timeState.minute)
                        endTime = c.timeInMillis.coerceAtLeast(startTime + 600000)
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
                title = { Text("新建日程") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) showDiscardDialog = true else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { saveAndBack() }) {
                        Text("保存", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; hasChanges = true },
                label = { Text("日程标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Date row
            Row(
                Modifier.fillMaxWidth().clickable { showDatePicker = true }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                Text(formatDate(startTime), style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp))
            }

            // Start time row
            Row(
                Modifier.fillMaxWidth().clickable { showStartTimePicker = true }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary)
                Text("开始: ${formatTime(startTime)}", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp))
            }

            // End time row
            Row(
                Modifier.fillMaxWidth().clickable { showEndTimePicker = true }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.error)
                Text("结束: ${formatTime(endTime)}", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp))
                Spacer(Modifier.weight(1f))
                Text(formatDuration(), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; hasChanges = true },
                label = { Text("详细说明") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                minLines = 5
            )
        }
    }
}
