package com.rmws2002.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.calendar.SystemCalendarEvent
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
    private val app = application as NoteApp
    private val repo = app.scheduleRepository
    private val calSync = app.calendarSync
    private val prefs = app.appPreferences

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    val allSchedules: StateFlow<List<ScheduleEntity>> = repo.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _systemEvents = MutableStateFlow<List<SystemCalendarEvent>>(emptyList())
    val systemEvents: StateFlow<List<SystemCalendarEvent>> = _systemEvents.asStateFlow()

    fun getSchedulesForDay(dayTimestamp: Long): Flow<List<ScheduleEntity>> {
        val cal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis
        return repo.getSchedulesForDay(startOfDay, endOfDay)
    }

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun fetchSystemEvents(calendarId: Long) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 2)
            val end = cal.timeInMillis
            _systemEvents.value = calSync.readEvents(start, end)
        }
    }

    fun saveSchedule(title: String, description: String, startTime: Long, endTime: Long, syncToCalendar: Boolean = false) {
        viewModelScope.launch {
            var eventId: Long? = null
            if (syncToCalendar) {
                val calendars = app.calendarSync.getAvailableCalendars()
                val calId = calendars.firstOrNull()?.id
                if (calId != null) {
                    eventId = calSync.writeEvent(calId, title, description, startTime, endTime)
                }
            }
            repo.insert(
                ScheduleEntity(
                    title = title.trim(),
                    description = description.trim(),
                    startTime = startTime,
                    endTime = endTime,
                    syncedToCalendar = eventId != null
                )
            )
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            if (schedule.syncedToCalendar) {
                // Can't delete from system calendar without eventId
                // For now, just delete locally
            }
            repo.delete(schedule)
        }
    }
}
