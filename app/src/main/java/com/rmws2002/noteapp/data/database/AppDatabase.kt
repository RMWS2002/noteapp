package com.rmws2002.noteapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rmws2002.noteapp.data.dao.NoteDao
import com.rmws2002.noteapp.data.dao.ScheduleDao
import com.rmws2002.noteapp.data.dao.TagDao
import com.rmws2002.noteapp.data.dao.TodoDao
import com.rmws2002.noteapp.data.entity.NoteEntity
import com.rmws2002.noteapp.data.entity.ScheduleEntity
import com.rmws2002.noteapp.data.entity.TagEntity
import com.rmws2002.noteapp.data.entity.TodoEntity

@Database(
    entities = [
        NoteEntity::class,
        TodoEntity::class,
        TagEntity::class,
        ScheduleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun todoDao(): TodoDao
    abstract fun tagDao(): TagDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noteapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
