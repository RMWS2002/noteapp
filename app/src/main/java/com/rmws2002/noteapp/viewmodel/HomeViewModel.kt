package com.rmws2002.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.entity.NoteEntity
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val noteRepo = (application as NoteApp).noteRepository
    private val todoRepo = (application as NoteApp).todoRepository
    private val scheduleRepo = (application as NoteApp).scheduleRepository

    val recentNotes: StateFlow<List<NoteEntity>> = noteRepo.getRecentNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTodos: StateFlow<List<TodoEntity>> = todoRepo.getActiveTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySchedules: StateFlow<List<ScheduleEntity>> = scheduleRepo.getSchedulesInRange(
        getStartOfDay(),
        getEndOfDay()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepo.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
