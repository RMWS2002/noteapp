package com.rmws2002.noteapp.repository

import com.rmws2002.noteapp.data.dao.ScheduleDao
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleDao: ScheduleDao) {
    fun getAllSchedules(): Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    fun getSchedulesForDay(startOfDay: Long, endOfDay: Long): Flow<List<ScheduleEntity>> =
        scheduleDao.getSchedulesForDay(startOfDay, endOfDay)
    fun getSchedulesInRange(startTime: Long, endTime: Long): Flow<List<ScheduleEntity>> =
        scheduleDao.getSchedulesInRange(startTime, endTime)
    suspend fun getScheduleById(id: Long): ScheduleEntity? = scheduleDao.getScheduleById(id)
    suspend fun insert(schedule: ScheduleEntity): Long = scheduleDao.insert(schedule)
    suspend fun update(schedule: ScheduleEntity) = scheduleDao.update(schedule)
    suspend fun delete(schedule: ScheduleEntity) = scheduleDao.delete(schedule)
    suspend fun deleteById(id: Long) = scheduleDao.deleteById(id)
}
