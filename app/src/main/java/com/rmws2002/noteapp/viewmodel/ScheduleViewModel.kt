package com.rmws2002.noteapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.calendar.SystemCalendarEvent
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.*
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

    init {
        // Attempt to load system events when ViewModel is created
        refreshSystemEvents()
    }

    fun refreshSystemEvents() {
        viewModelScope.launch {
            try {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -2)  // Wider window for overlap detection
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 4)
                val end = cal.timeInMillis
                _systemEvents.value = calSync.readEvents(start, end)
            } catch (e: SecurityException) {
                Log.d("ScheduleVM", "Calendar permission required")
            } catch (e: Exception) {
                Log.e("ScheduleVM", "Failed to read calendar events", e)
            }
        }
    }

    fun getSchedulesForDay(dayTimestamp: Long): Flow<List<ScheduleEntity>> {
        val cal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis
        return repo.getSchedulesForDay(startOfDay, endOfDay)
    }

    /** Get system calendar events for the selected day */
    fun getSystemEventsForDay(dayTimestamp: Long): List<SystemCalendarEvent> {
        val cal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis
        return _systemEvents.value.filter { it.startTime in startOfDay until endOfDay }
    }

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun saveSchedule(title: String, description: String, startTime: Long, endTime: Long) {
        viewModelScope.launch {
            val enabled = prefs.calendarSyncEnabled.first()
            var synced = false
            if (enabled) {
                try {
                    val calendars = calSync.getAvailableCalendars()
                    calSync.writeEvent(calendars.firstOrNull()?.id ?: 1L, title, description, startTime, endTime)
                    synced = true
                } catch (_: Exception) {}
            }
            repo.insert(
                ScheduleEntity(
                    title = title.trim(),
                    description = description.trim(),
                    startTime = startTime,
                    endTime = endTime,
                    syncedToCalendar = synced
                )
            )
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch { repo.delete(schedule) }
    }
}
