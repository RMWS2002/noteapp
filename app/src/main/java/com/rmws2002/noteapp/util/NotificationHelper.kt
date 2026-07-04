package com.rmws2002.noteapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rmws2002.noteapp.MainActivity
import com.rmws2002.noteapp.R

object NotificationHelper {
    const val CHANNEL_TODO = "todo_reminders"
    const val CHANNEL_SCHEDULE = "schedule_reminders"
    const val CHANNEL_DAILY = "daily_summary"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val todoChannel = NotificationChannel(
            CHANNEL_TODO,
            "待办提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "待办事项到期提醒"
            enableVibration(true)
        }
        manager.createNotificationChannel(todoChannel)

        val scheduleChannel = NotificationChannel(
            CHANNEL_SCHEDULE,
            "日程提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "日程即将开始提醒"
            enableVibration(true)
        }
        manager.createNotificationChannel(scheduleChannel)

        val dailyChannel = NotificationChannel(
            CHANNEL_DAILY,
            "每日汇总",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "每日待办和日程汇总"
        }
        manager.createNotificationChannel(dailyChannel)
    }

    fun showTodoReminder(context: Context, todoId: Long, title: String, dueTime: Long?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "todo")
            putExtra("todo_id", todoId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, todoId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeStr = if (dueTime != null) {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = dueTime }
            String.format("%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
        } else null

        val contentText = buildString {
            append(title)
            if (timeStr != null) append("  ·  $timeStr")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TODO)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ 待办提醒")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify("todo_$todoId", todoId.toInt(), notification)
    }

    fun showScheduleReminder(context: Context, scheduleId: Long, title: String, startTime: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "schedule")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, (scheduleId + 10000).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = java.util.Calendar.getInstance().apply { timeInMillis = startTime }
        val timeStr = String.format(
            "%d月%d日 %02d:%02d",
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SCHEDULE)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("📅 日程提醒")
            .setContentText("$title  ·  $timeStr")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify("schedule_$scheduleId", (scheduleId + 10000).toInt(), notification)
    }

    fun showDailySummary(context: Context, todoCount: Int, scheduleCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 99999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val content = buildString {
            if (todoCount > 0) append("$todoCount 项待办")
            if (scheduleCount > 0) {
                if (todoCount > 0) append("  ·  ")
                append("$scheduleCount 个日程")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("🌞 今日事项")
            .setContentText(content.ifEmpty { "今天没什么安排" })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify("daily_summary", 99999, notification)
    }
}
