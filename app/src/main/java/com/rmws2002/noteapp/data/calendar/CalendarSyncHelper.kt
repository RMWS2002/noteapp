package com.rmws2002.noteapp.data.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import java.util.TimeZone

data class CalendarAccount(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val accountType: String = ""
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
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        val calendars = mutableListOf<CalendarAccount>()
        cursor?.use {
            while (it.moveToNext()) {
                val accessLevel = it.getInt(4)
                // Only include calendars the user can read
                if (accessLevel >= CalendarContract.Calendars.CAL_ACCESS_READ) {
                    calendars.add(
                        CalendarAccount(
                            id = it.getLong(0),
                            displayName = it.getString(1) ?: "",
                            accountName = it.getString(2) ?: "",
                            accountType = it.getString(3) ?: ""
                        )
                    )
                }
            }
        }
        return calendars
    }

    /**
     * Get the best default calendar to write to.
     * Prefers Xiaomi/MIUI local calendar, then any LOCAL calendar,
     * then falls back to the first available writable calendar.
     */
    fun getDefaultCalendarId(): Long {
        val calendars = getAvailableCalendars()
        if (calendars.isEmpty()) return 1L

        // Prefer Xiaomi/MIUI calendar (account_type = com.xiaomi or LOCAL)
        calendars.firstOrNull { cal ->
            cal.accountType.contains("xiaomi", ignoreCase = true) ||
            cal.accountType.contains("miui", ignoreCase = true)
        }?.let { return it.id }

        // Then prefer any LOCAL account (most OEM calendars use this)
        calendars.firstOrNull { cal ->
            cal.accountType.equals("LOCAL", ignoreCase = true)
        }?.let { return it.id }

        // Fallback to first calendar
        return calendars.first().id
    }

    fun getCalendarName(calendarId: Long): String {
        val calendars = getAvailableCalendars()
        return calendars.firstOrNull { it.id == calendarId }?.displayName ?: "日历"
    }

    fun readEvents(startTime: Long, endTime: Long): List<SystemCalendarEvent> {
        val events = mutableListOf<SystemCalendarEvent>()

        // Use CalendarContract.Instances.query() which:
        // - Properly expands recurring events
        // - Auto-joins with Calendars table for visibility/access filtering
        // - Handles all-day event timestamps correctly
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME
        )

        try {
            val cursor = CalendarContract.Instances.query(
                context.contentResolver,
                projection,
                startTime,
                endTime
            )
            cursor?.use {
                // Instances columns are 0-indexed: EVENT_ID=0, TITLE=1, DESCRIPTION=2, BEGIN=3, END=4
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
        } catch (e: SecurityException) {
            Log.e("CalendarSync", "Permission denied reading calendar events", e)
        } catch (e: Exception) {
            Log.e("CalendarSync", "Failed to read calendar events", e)
        }

        Log.d("CalendarSync", "Read ${events.size} events from calendar in range")
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
