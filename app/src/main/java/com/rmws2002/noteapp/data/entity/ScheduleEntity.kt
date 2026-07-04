package com.rmws2002.noteapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val description: String = "",
    @ColumnInfo(name = "start_time") val startTime: Long = 0,
    @ColumnInfo(name = "end_time") val endTime: Long = 0,
    @ColumnInfo(name = "synced_to_calendar") val syncedToCalendar: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
