package com.rmws2002.noteapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rmws2002.noteapp.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY is_completed ASC, due_date ASC, created_at DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE is_completed = 0 ORDER BY due_date ASC, created_at DESC")
    fun getActiveTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTodos(): Flow<List<TodoEntity>>

    @Query("DELETE FROM todos WHERE is_completed = 1")
    suspend fun deleteCompleted()

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE tag_id = :tagId ORDER BY created_at DESC")
    fun getTodosByTag(tagId: Long): Flow<List<TodoEntity>>

    @Query(
        "SELECT * FROM todos WHERE title LIKE '%' || :query || '%' " +
        "ORDER BY is_completed ASC, due_date ASC"
    )
    fun searchTodos(query: String): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
