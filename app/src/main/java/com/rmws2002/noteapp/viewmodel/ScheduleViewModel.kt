package com.rmws2002.noteapp.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.calendar.SystemCalendarEvent
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import com.rmws2002.noteapp.receiver.AlarmReceiver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleViewModel(application: android.app.Application) : AndroidViewModel(application) {
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
        refreshSystemEvents()
    }

    fun refreshSystemEvents() {
        viewModelScope.launch {
            try {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -2)
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

    /** Get system calendar events for the selected day using overlap logic */
    fun getSystemEventsForDay(dayTimestamp: Long): List<SystemCalendarEvent> {
        val cal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis
        // Use overlap: event starts before day-end AND ends after day-start
        return _systemEvents.value.filter { it.startTime < endOfDay && it.endTime > startOfDay }
    }

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun saveSchedule(title: String, description: String, startTime: Long, endTime: Long) {
        viewModelScope.launch {
            val enabled = prefs.calendarSyncEnabled.first()
            var synced = false
            var calendarId = 0L
            if (enabled) {
                try {
                    // Use selected calendar or auto-detect default
                    val selectedId = prefs.selectedCalendarId.first()
                    calendarId = selectedId ?: calSync.getDefaultCalendarId()
                    calSync.writeEvent(calendarId, title, description, startTime, endTime)
                    synced = true
                } catch (e: Exception) {
                    Log.e("ScheduleVM", "Failed to sync to calendar", e)
                }
            }
            val entity = ScheduleEntity(
                title = title.trim(),
                description = description.trim(),
                startTime = startTime,
                endTime = endTime,
                syncedToCalendar = synced
            )
            val id = repo.insert(entity)

            // Schedule notification
            scheduleNotification(id, title, startTime)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            cancelNotification(schedule.id)
            repo.delete(schedule)
        }
    }

    private fun scheduleNotification(scheduleId: Long, title: String, startTime: Long) {
        val reminderMinutes = 15 // default, can be customized later
        val reminderTime = startTime - (reminderMinutes * 60 * 1000L)
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(app, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SCHEDULE_REMINDER
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(AlarmReceiver.EXTRA_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_START_TIME, startTime)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            app,
            (scheduleId + 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

    private fun cancelNotification(scheduleId: Long) {
        val intent = Intent(app, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SCHEDULE_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            app,
            (scheduleId + 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
