package com.rmws2002.noteapp.data.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.util.TimeZone

data class CalendarAccount(
    val id: Long,
    val displayName: String,
    val accountName: String
)

data class SystemCalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val calendarName: String
)

class CalendarSyncHelper(private val context: Context) {

    fun getAvailableCalendars(): List<CalendarAccount> {
        val uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        val calendars = mutableListOf<CalendarAccount>()
        cursor?.use {
            while (it.moveToNext()) {
                calendars.add(
                    CalendarAccount(
                        id = it.getLong(0),
                        displayName = it.getString(1) ?: "",
                        accountName = it.getString(2) ?: ""
                    )
                )
            }
        }
        return calendars
    }

    fun readEvents(startTime: Long, endTime: Long): List<SystemCalendarEvent> {
        val events = mutableListOf<SystemCalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.CALENDAR_DISPLAY_NAME
        )
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection, selection, selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                events.add(
                    SystemCalendarEvent(
                        id = it.getLong(0),
                        title = it.getString(1) ?: "",
                        description = it.getString(2) ?: "",
                        startTime = it.getLong(3),
                        endTime = it.getLong(4),
                        calendarName = it.getString(5) ?: ""
                    )
                )
            }
        }
        return events
    }

    fun writeEvent(
        calendarId: Long,
        title: String,
        description: String,
        startTime: Long,
        endTime: Long
    ): Long? {
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return uri?.let { ContentUris.parseId(it) }
    }

    fun updateEvent(eventId: Long, title: String, description: String, startTime: Long, endTime: Long): Int {
        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        return context.contentResolver.update(uri, values, null, null)
    }

    fun deleteEvent(eventId: Long): Int {
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        return context.contentResolver.delete(uri, null, null)
    }
}
