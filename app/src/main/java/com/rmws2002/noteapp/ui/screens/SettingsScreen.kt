package com.rmws2002.noteapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import com.rmws2002.noteapp.data.entity.TagEntity
import com.rmws2002.noteapp.data.preferences.ThemeMode
import com.rmws2002.noteapp.ui.components.parseColor
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
    val currentTheme by app.appPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val syncEnabled by app.appPreferences.calendarSyncEnabled.collectAsState(initial = false)
    val tags by app.tagRepository.getAllTags().collectAsState(initial = emptyList())

    // Runtime calendar permission launcher
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCalendarPerm = granted
        if (granted) {
            scope.launch { app.appPreferences.setCalendarSyncEnabled(true) }
        }
    }

    // Check permission on compose
    LaunchedEffect(Unit) {
        hasCalendarPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // === THEME ===
            Text("主题", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            ThemeMode.entries.forEach { mode ->
                val label = when (mode) {
                    ThemeMode.SYSTEM -> "跟随系统"
                    ThemeMode.LIGHT -> "浅色"
                    ThemeMode.DARK -> "深色"
                }
                Row(
                    Modifier.fillMaxWidth().clickable {
                        scope.launch { app.appPreferences.setThemeMode(mode) }
                    }.padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (currentTheme == mode) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // === CALENDAR SYNC ===
            Text("日历同步", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("同步到系统日历", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = syncEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !hasCalendarPerm) {
                            permLauncher.launch(Manifest.permission.READ_CALENDAR)
                        } else {
                            scope.launch { app.appPreferences.setCalendarSyncEnabled(enabled) }
                        }
                    }
                )
            }

            if (syncEnabled && !hasCalendarPerm) {
                Text("需要日历权限才能同步", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // === TAG MANAGEMENT ===
            var addingTag by remember { mutableStateOf(false) }
            var newTagName by remember { mutableStateOf("") }
            var newTagColor by remember { mutableStateOf("#2C6FEF") }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("标签管理", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { addingTag = !addingTag; if (!addingTag) newTagName = "" }) {
                    Text(if (addingTag) "完成" else "添加")
                }
            }
            Spacer(Modifier.height(4.dp))

            if (addingTag) {
                Row(Modifier.padding(vertical = 4.dp)) {
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
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null,
                        tint = parseColor(tag.color),
                        modifier = Modifier.padding(end = 4.dp))
                    Text(tag.name, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        scope.launch { try { app.tagRepository.delete(tag) } catch (_: Exception) {} }
                    }) {
                        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("NoteApp v1.0", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
