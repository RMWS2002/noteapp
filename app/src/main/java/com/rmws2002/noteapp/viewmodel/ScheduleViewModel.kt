package com.rmws2002.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as NoteApp).scheduleRepository

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    val allSchedules: StateFlow<List<ScheduleEntity>> = repo.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSchedulesForDay(dayTimestamp: Long): Flow<List<ScheduleEntity>> {
        val cal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis
        return repo.getSchedulesForDay(startOfDay, endOfDay)
    }

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun saveSchedule(title: String, description: String, startTime: Long, endTime: Long) {
        viewModelScope.launch {
            repo.insert(
                ScheduleEntity(
                    title = title.trim(),
                    description = description.trim(),
                    startTime = startTime,
                    endTime = endTime
                )
            )
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch { repo.delete(schedule) }
    }
}
