package com.rmws2002.noteapp

import android.app.Application
import com.rmws2002.noteapp.data.calendar.CalendarSyncHelper
import com.rmws2002.noteapp.data.database.AppDatabase
import com.rmws2002.noteapp.data.preferences.AppPreferences
import com.rmws2002.noteapp.repository.NoteRepository
import com.rmws2002.noteapp.repository.ScheduleRepository
import com.rmws2002.noteapp.repository.TagRepository
import com.rmws2002.noteapp.repository.TodoRepository

class NoteApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }
    val todoRepository: TodoRepository by lazy { TodoRepository(database.todoDao()) }
    val tagRepository: TagRepository by lazy { TagRepository(database.tagDao()) }
    val scheduleRepository: ScheduleRepository by lazy { ScheduleRepository(database.scheduleDao()) }
    val appPreferences: AppPreferences by lazy { AppPreferences(this) }
    val calendarSync: CalendarSyncHelper by lazy { CalendarSyncHelper(this) }
}
