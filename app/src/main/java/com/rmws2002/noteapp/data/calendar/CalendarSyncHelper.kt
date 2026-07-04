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
