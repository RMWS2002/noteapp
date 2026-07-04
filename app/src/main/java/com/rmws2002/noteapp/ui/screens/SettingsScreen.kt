package com.rmws2002.noteapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.calendar.CalendarAccount
import com.rmws2002.noteapp.data.entity.TagEntity
import com.rmws2002.noteapp.data.preferences.ThemeMode
import com.rmws2002.noteapp.ui.components.parseColor
import com.rmws2002.noteapp.util.NotificationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NoteApp
    val scope = rememberCoroutineScope()
    var hasCalendarPerm by remember { mutableStateOf(false) }
    var hasNotifPerm by remember { mutableStateOf(false) }
    val currentTheme by app.appPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val syncEnabled by app.appPreferences.calendarSyncEnabled.collectAsState(initial = false)
    val notifEnabled by app.appPreferences.notificationsEnabled.collectAsState(initial = true)
    val selectedCalendarId by app.appPreferences.selectedCalendarId.collectAsState(initial = null)
    val tags by app.tagRepository.getAllTags().collectAsState(initial = emptyList())
    var availableCalendars by remember { mutableStateOf<List<CalendarAccount>>(emptyList()) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    // Check permissions on compose
    LaunchedEffect(Unit) {
        hasCalendarPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        hasNotifPerm = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        // Load available calendars
        try {
            availableCalendars = app.calendarSync.getAvailableCalendars()
        } catch (_: Exception) {}
    }

    // Request multiple calendar permissions
    val calendarPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val readGranted = grants[Manifest.permission.READ_CALENDAR] == true
        val writeGranted = grants[Manifest.permission.WRITE_CALENDAR] == true
        hasCalendarPerm = readGranted && writeGranted
        if (readGranted && writeGranted) {
            scope.launch { app.appPreferences.setCalendarSyncEnabled(true) }
        }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPerm = granted
        if (granted) {
            NotificationHelper.createChannels(context)
        }
    }

    val selectedCalendarName = availableCalendars
        .firstOrNull { it.id == selectedCalendarId }?.displayName
        ?: "自动选择"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // === THEME ===
            Text("外观", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            ThemeMode.entries.forEach { mode ->
                val label = when (mode) {
                    ThemeMode.SYSTEM -> "跟随系统"
                    ThemeMode.LIGHT -> "浅色模式"
                    ThemeMode.DARK -> "深色模式"
                }
                Row(
                    Modifier.fillMaxWidth().clickable {
                        scope.launch { app.appPreferences.setThemeMode(mode) }
                    }.padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (currentTheme == mode) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            // === NOTIFICATIONS ===
            Text("通知", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(16.dp))
                Text("允许通知", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = notifEnabled && hasNotifPerm,
                    onCheckedChange = { enabled ->
                        if (enabled && !hasNotifPerm) {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            scope.launch {
                                app.appPreferences.setNotificationsEnabled(enabled)
                            }
                        }
                    }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            // === CALENDAR SYNC ===
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("日历", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(16.dp))
                Text("同步日历", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = syncEnabled && hasCalendarPerm,
                    onCheckedChange = { enabled ->
                        if (enabled && !hasCalendarPerm) {
                            calendarPermLauncher.launch(arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            ))
                        } else {
                            scope.launch { app.appPreferences.setCalendarSyncEnabled(enabled) }
                        }
                    }
                )
            }

            // Calendar account selector
            if (syncEnabled && hasCalendarPerm && availableCalendars.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().clickable { showCalendarPicker = !showCalendarPicker }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(40.dp))
                    Text("日历账户", style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f))
                    Text(selectedCalendarName, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (showCalendarPicker) {
                    availableCalendars.forEach { cal ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                scope.launch {
                                    app.appPreferences.setSelectedCalendar(cal.id, cal.displayName)
                                }
                                showCalendarPicker = false
                            }.padding(vertical = 10.dp, horizontal = 40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${cal.displayName} (${cal.accountName})",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f))
                            if (cal.id == selectedCalendarId) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            if (syncEnabled && !hasCalendarPerm) {
                Text("需要日历权限才能同步", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            // === TAG MANAGEMENT ===
            var addingTag by remember { mutableStateOf(false) }
            var newTagName by remember { mutableStateOf("") }
            var newTagColor by remember { mutableStateOf("#2C6FEF") }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("标签", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                TextButton(onClick = { addingTag = !addingTag; if (!addingTag) newTagName = "" }) {
                    Text(if (addingTag) "完成" else "添加")
                }
            }

            if (addingTag) {
                Row(Modifier.padding(vertical = 8.dp)) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        placeholder = { Text("标签名") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        if (newTagName.isNotBlank()) {
                            scope.launch {
                                try {
                                    app.tagRepository.insert(TagEntity(name = newTagName.trim(), color = newTagColor))
                                    newTagName = ""
                                } catch (_: Exception) {}
                            }
                        }
                    }) { Text("确认") }
                }
            }

            tags.forEach { tag ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null,
                        tint = parseColor(tag.color),
                        modifier = Modifier.padding(end = 8.dp))
                    Text(tag.name, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        scope.launch { try { app.tagRepository.delete(tag) } catch (_: Exception) {} }
                    }) {
                        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("NoteApp v2.0", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
        }
    }
}
