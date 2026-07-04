package com.rmws2002.noteapp.repository

import com.rmws2002.noteapp.data.dao.NoteDao
import com.rmws2002.noteapp.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    fun getRecentNotes(): Flow<List<NoteEntity>> = noteDao.getRecentNotes()
    fun getNotesByTag(tagId: Long): Flow<List<NoteEntity>> = noteDao.getNotesByTag(tagId)
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)
    suspend fun insert(note: NoteEntity): Long = noteDao.insert(note)
    suspend fun update(note: NoteEntity) = noteDao.update(note)
    suspend fun delete(note: NoteEntity) = noteDao.delete(note)
    suspend fun deleteById(id: Long) = noteDao.deleteById(id)
}
