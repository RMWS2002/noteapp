package com.rmws2002.noteapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.entity.NoteEntity
import com.rmws2002.noteapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long?,
    onBack: () -> Unit,
    viewModel: NoteViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var initialLoaded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load existing note
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0 && !initialLoaded) {
            val note = viewModel.allNotes.value.find { it.id == noteId }
            note?.let {
                title = it.title
                content = it.content
                initialLoaded = true
            }
        }
    }

    fun saveAndBack() {
        if (title.isNotBlank() || content.isNotBlank()) {
            if (noteId == null || noteId == 0L) {
                viewModel.saveNote(title, content, null)
            } else {
                viewModel.updateNote(noteId, title, content, null)
            }
        }
        onBack()
    }

    fun deleteAndBack() {
        if (noteId != null && noteId > 0) {
            viewModel.deleteNote(NoteEntity(id = noteId, title = title, content = content))
            scope.launch {
                snackbarHostState.showSnackbar("已删除")
            }
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
                }) {
                    Text("放弃", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("继续编辑")
                }
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
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null || noteId == 0L) "新建笔记" else "编辑笔记") },
                navigationIcon = {
                    TextButton(onClick = {
                        if (hasChanges) showDiscardDialog = true else onBack()
                    }) {
                        Text("取消", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    // Save button
                    TextButton(onClick = { saveAndBack() }) {
                        Text("保存", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    // Delete button (only for existing notes)
                    if (noteId != null && noteId > 0) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; hasChanges = true },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it; hasChanges = true },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                minLines = 10
            )
        }
    }
}
