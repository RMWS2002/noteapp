package com.rmws2002.noteapp.repository

import com.rmws2002.noteapp.data.dao.TagDao
import com.rmws2002.noteapp.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {
    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()
    suspend fun getTagById(id: Long): TagEntity? = tagDao.getTagById(id)
    suspend fun insert(tag: TagEntity): Long = tagDao.insert(tag)
    suspend fun update(tag: TagEntity) = tagDao.update(tag)
    suspend fun delete(tag: TagEntity) = tagDao.delete(tag)
}
