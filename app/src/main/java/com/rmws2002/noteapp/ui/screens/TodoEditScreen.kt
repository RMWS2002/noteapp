package com.rmws2002.noteapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(
    todoId: Long?,
    onBack: () -> Unit,
    viewModel: TodoViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(todoId) {
        if (todoId != null && todoId > 0) {
            val todo = viewModel.allTodos.value.find { it.id == todoId }
            todo?.let {
                title = it.title
                dueDate = it.dueDate
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (todoId == null || todoId == 0L) "新建待办" else "编辑待办") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            if (todoId == null || todoId == 0L) {
                                viewModel.saveTodo(title, dueDate, null)
                            } else {
                                viewModel.updateTodo(todoId, title, dueDate, null)
                            }
                        }
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (todoId != null && todoId > 0) {
                        IconButton(onClick = {
                            viewModel.deleteTodo(TodoEntity(id = todoId, title = title))
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("待办内容") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
