package com.rmws2002.noteapp.repository

import com.rmws2002.noteapp.data.dao.TodoDao
import com.rmws2002.noteapp.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()
    fun getActiveTodos(): Flow<List<TodoEntity>> = todoDao.getActiveTodos()
    fun getCompletedTodos(): Flow<List<TodoEntity>> = todoDao.getCompletedTodos()
    fun getTodosByTag(tagId: Long): Flow<List<TodoEntity>> = todoDao.getTodosByTag(tagId)
    fun searchTodos(query: String): Flow<List<TodoEntity>> = todoDao.searchTodos(query)
    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)
    suspend fun insert(todo: TodoEntity): Long = todoDao.insert(todo)
    suspend fun update(todo: TodoEntity) = todoDao.update(todo)
    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)
    suspend fun deleteById(id: Long) = todoDao.deleteById(id)
    suspend fun deleteCompleted() = todoDao.deleteCompleted()
}
