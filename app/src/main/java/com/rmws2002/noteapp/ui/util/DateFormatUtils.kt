package com.rmws2002.noteapp.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)
val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val dateTimeFormatter = DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINESE)
val shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd")

fun formatTimestamp(timestamp: Long, formatter: DateTimeFormatter = dateTimeFormatter): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

fun formatDate(timestamp: Long): String = formatTimestamp(timestamp, dateFormatter)
fun formatTime(timestamp: Long): String = formatTimestamp(timestamp, timeFormatter)
fun formatShortDate(timestamp: Long): String = formatTimestamp(timestamp, shortDateFormatter)

fun formatDateRange(startTime: Long, endTime: Long): String {
    val start = formatTime(startTime)
    val end = formatTime(endTime)
    return "$start - $end"
}

fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun todayString(): String = LocalDate.now().format(dateFormatter)
