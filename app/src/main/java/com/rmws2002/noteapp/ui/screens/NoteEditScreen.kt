package com.rmws2002.noteapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.entity.NoteEntity
import com.rmws2002.noteapp.data.entity.TagEntity
import com.rmws2002.noteapp.ui.components.parseColor
import com.rmws2002.noteapp.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long?,
    onBack: () -> Unit,
    viewModel: NoteViewModel = viewModel()
) {
    val tags by viewModel.tags.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf<Long?>(null) }
    var hasChanges by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var initialLoaded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val createdAt = remember { mutableStateOf(System.currentTimeMillis()) }

    val dateTimeFormat = remember { SimpleDateFormat("M月d日 HH:mm", Locale.CHINESE) }

    // Load existing note
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0 && !initialLoaded) {
            val note = viewModel.allNotes.value.find { it.id == noteId }
            note?.let {
                title = it.title
                content = it.content
                selectedTagId = it.tagId
                createdAt.value = it.createdAt
                initialLoaded = true
            }
        }
    }

    fun saveAndBack() {
        if (title.isNotBlank() || content.isNotBlank()) {
            if (noteId == null || noteId == 0L) {
                viewModel.saveNote(title, content, selectedTagId)
            } else {
                viewModel.updateNote(noteId, title, content, selectedTagId)
            }
        }
        onBack()
    }

    fun deleteAndBack() {
        if (noteId != null && noteId > 0) {
            viewModel.deleteNote(NoteEntity(id = noteId, title = title, content = content))
        }
        onBack()
    }

    // Handle system back press
    BackHandler(enabled = hasChanges) {
        showDiscardDialog = true
    }

    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("放弃更改？") },
            text = { Text("当前编辑的内容还没有保存") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBack()
                }) { Text("放弃", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("继续编辑") }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除这条笔记？") },
            text = { Text("此操作无法撤销") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteAndBack()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    val isEditing = noteId != null && noteId > 0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑笔记" else "新建笔记", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = {
                        if (hasChanges) showDiscardDialog = true else onBack()
                    }) { Text("取消", color = MaterialTheme.colorScheme.primary) }
                },
                actions = {
                    TextButton(onClick = { saveAndBack() }) {
                        Text("保存", color = MaterialTheme.colorScheme.primary)
                    }
                    if (isEditing) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            // Title - borderless, large text
            TextField(
                value = title,
                onValueChange = { title = it; hasChanges = true },
                placeholder = {
                    Text("笔记标题",
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

            Spacer(Modifier.height(16.dp))

            // Tag row
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Label, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(20.dp))
                Text("标签", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(56.dp))
                LazyRow(modifier = Modifier.weight(1f)) {
                    items(tags) { tag ->
                        val isSelected = selectedTagId == tag.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTagId = if (isSelected) null else tag.id
                                hasChanges = true
                            },
                            label = { Text(tag.name, style = MaterialTheme.typography.labelMedium) },
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
            Spacer(Modifier.height(16.dp))

            // Content area with subtle background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(4.dp)
            ) {
                TextField(
                    value = content,
                    onValueChange = { content = it; hasChanges = true },
                    placeholder = {
                        Text("写下你的想法...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    minLines = 10,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                // Character count
                if (content.isNotEmpty()) {
                    Text(
                        text = "${content.length} 字",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 12.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            // Footer with clock icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isEditing) "创建于 ${dateTimeFormat.format(Date(createdAt.value))}"
                           else dateTimeFormat.format(Date(System.currentTimeMillis())),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isEditing) {
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("删除笔记", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
