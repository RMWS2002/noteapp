package com.rmws2002.noteapp.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.entity.TodoEntity
import com.rmws2002.noteapp.receiver.AlarmReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NoteApp
    private val repo = app.todoRepository
    private val tagRepo = app.tagRepository

    val allTodos: StateFlow<List<TodoEntity>> = repo.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags = tagRepo.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repo.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun saveTodo(
        title: String,
        dueDate: Long?,
        hasTime: Boolean,
        reminderTime: Long?,
        tagId: Long?
    ) {
        viewModelScope.launch {
            val entity = TodoEntity(
                title = title.trim(),
                dueDate = dueDate,
                hasTime = hasTime,
                reminderTime = reminderTime,
                tagId = tagId
            )
            val id = repo.insert(entity)
            // Schedule reminder notification if reminderTime is set
            if (reminderTime != null && !title.isBlank()) {
                scheduleNotification(id, title.trim(), dueDate)
            }
        }
    }

    fun updateTodo(
        id: Long,
        title: String,
        dueDate: Long?,
        hasTime: Boolean,
        reminderTime: Long?,
        tagId: Long?
    ) {
        viewModelScope.launch {
            // Cancel old alarm first
            cancelNotification(id)
            repo.update(
                TodoEntity(
                    id = id,
                    title = title.trim(),
                    dueDate = dueDate,
                    hasTime = hasTime,
                    reminderTime = reminderTime,
                    tagId = tagId
                )
            )
            // Reschedule if needed
            if (reminderTime != null && !title.isBlank()) {
                scheduleNotification(id, title.trim(), dueDate)
            }
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            cancelNotification(todo.id)
            repo.delete(todo)
        }
    }

    private fun scheduleNotification(todoId: Long, title: String, dueTime: Long?) {
        val notifyTime = dueTime ?: return
        if (notifyTime <= System.currentTimeMillis()) return

        val intent = Intent(app, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TODO_REMINDER
            putExtra(AlarmReceiver.EXTRA_TODO_ID, todoId)
            putExtra(AlarmReceiver.EXTRA_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_DUE_TIME, notifyTime)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            app,
            todoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent)
    }

    private fun cancelNotification(todoId: Long) {
        val intent = Intent(app, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TODO_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            app,
            todoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
