package com.rmws2002.noteapp.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rmws2002.noteapp.ui.components.TodoRow
import com.rmws2002.noteapp.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onTodoClick: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val todoResults by viewModel.todoResults.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 80.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("搜索待办...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        if (query.isNotBlank()) {
            if (todoResults.isNotEmpty()) {
                item {
                    Text(
                        text = "待办",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(todoResults) { todo ->
                    TodoRow(todo = todo, onToggle = {}, onClick = { onTodoClick(todo.id) },
                        modifier = Modifier.padding(vertical = 3.dp))
                }
            }
            if (todoResults.isEmpty()) {
                item { EmptyHint("没有找到结果") }
            }
        }
    }
}
