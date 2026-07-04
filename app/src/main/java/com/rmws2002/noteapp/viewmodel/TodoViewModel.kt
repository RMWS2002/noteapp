package com.rmws2002.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.entity.TodoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as NoteApp).todoRepository
    private val tagRepo = (application as NoteApp).tagRepository

    val allTodos: StateFlow<List<TodoEntity>> = repo.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags = tagRepo.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repo.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun saveTodo(title: String, dueDate: Long?, tagId: Long?) {
        viewModelScope.launch {
            repo.insert(
                TodoEntity(
                    title = title.trim(),
                    dueDate = dueDate,
                    tagId = tagId
                )
            )
        }
    }

    fun updateTodo(id: Long, title: String, dueDate: Long?, tagId: Long?) {
        viewModelScope.launch {
            repo.update(
                TodoEntity(
                    id = id,
                    title = title.trim(),
                    dueDate = dueDate,
                    tagId = tagId
                )
            )
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch { repo.delete(todo) }
    }
}
