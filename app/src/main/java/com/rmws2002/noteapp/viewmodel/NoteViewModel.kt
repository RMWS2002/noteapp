package com.rmws2002.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as NoteApp).noteRepository
    private val tagRepo = (application as NoteApp).tagRepository

    val allNotes: StateFlow<List<NoteEntity>> = repo.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTagId = MutableStateFlow<Long?>(null)
    val selectedTagId: StateFlow<Long?> = _selectedTagId.asStateFlow()

    val tags = tagRepo.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load default tags if empty
        viewModelScope.launch {
            tagRepo.getAllTags().collect { tagList ->
                if (tagList.isEmpty()) {
                    tagRepo.insert(com.rmws2002.noteapp.data.entity.TagEntity(name = "工作", color = "#1A73E8"))
                    tagRepo.insert(com.rmws2002.noteapp.data.entity.TagEntity(name = "个人", color = "#34A853"))
                    tagRepo.insert(com.rmws2002.noteapp.data.entity.TagEntity(name = "学习", color = "#FBBC04"))
                }
            }
        }
    }

    fun filterByTag(tagId: Long?) {
        _selectedTagId.value = tagId
    }

    fun saveNote(title: String, content: String, tagId: Long?) {
        viewModelScope.launch {
            repo.insert(
                NoteEntity(
                    title = title.trim(),
                    content = content.trim(),
                    tagId = tagId
                )
            )
        }
    }

    fun updateNote(id: Long, title: String, content: String, tagId: Long?) {
        viewModelScope.launch {
            repo.update(
                NoteEntity(
                    id = id,
                    title = title.trim(),
                    content = content.trim(),
                    tagId = tagId,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repo.delete(note) }
    }
}
