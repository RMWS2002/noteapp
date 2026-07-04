package com.rmws2002.noteapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rmws2002.noteapp.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TODO_REMINDER -> {
                val todoId = intent.getLongExtra(EXTRA_TODO_ID, 0)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
                val dueTime = if (intent.hasExtra(EXTRA_DUE_TIME)) {
                    intent.getLongExtra(EXTRA_DUE_TIME, 0)
                } else null
                NotificationHelper.showTodoReminder(context, todoId, title, dueTime)
            }
            ACTION_SCHEDULE_REMINDER -> {
                val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, 0)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
                val startTime = intent.getLongExtra(EXTRA_START_TIME, 0)
                NotificationHelper.showScheduleReminder(context, scheduleId, title, startTime)
            }
            ACTION_DAILY_SUMMARY -> {
                val todoCount = intent.getIntExtra(EXTRA_TODO_COUNT, 0)
                val scheduleCount = intent.getIntExtra(EXTRA_SCHEDULE_COUNT, 0)
                NotificationHelper.showDailySummary(context, todoCount, scheduleCount)
            }
        }
    }

    companion object {
        const val ACTION_TODO_REMINDER = "com.rmws2002.noteapp.TODO_REMINDER"
        const val ACTION_SCHEDULE_REMINDER = "com.rmws2002.noteapp.SCHEDULE_REMINDER"
        const val ACTION_DAILY_SUMMARY = "com.rmws2002.noteapp.DAILY_SUMMARY"

        const val EXTRA_TODO_ID = "todo_id"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DUE_TIME = "due_time"
        const val EXTRA_START_TIME = "start_time"
        const val EXTRA_TODO_COUNT = "todo_count"
        const val EXTRA_SCHEDULE_COUNT = "schedule_count"
    }
}
