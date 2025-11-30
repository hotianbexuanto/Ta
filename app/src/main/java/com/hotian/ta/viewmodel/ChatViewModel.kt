package com.hotian.ta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotian.ta.data.AppDatabase
import com.hotian.ta.data.Group
import com.hotian.ta.data.Message
import com.hotian.ta.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database.messageDao(), database.groupDao())

    private val _currentGroupId = MutableStateFlow(0L)
    val currentGroupId: StateFlow<Long> = _currentGroupId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    init {
        loadGroups()
        observeCurrentGroupMessages()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            repository.getAllGroups().collect { groupList ->
                _groups.value = groupList
                if (_currentGroupId.value == 0L && groupList.isNotEmpty()) {
                    _currentGroupId.value = groupList.first().id
                }
            }
        }
    }

    private fun observeCurrentGroupMessages() {
        viewModelScope.launch {
            _currentGroupId.collect { groupId ->
                repository.getMessagesForGroup(groupId).collect { messageList ->
                    _messages.value = messageList
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val message = Message(
                content = content,
                groupId = _currentGroupId.value
            )
            repository.sendMessage(message)
        }
    }

    fun createGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newGroupId = repository.createGroup(name)
            _currentGroupId.value = newGroupId
        }
    }

    fun switchGroup(groupId: Long) {
        _currentGroupId.value = groupId
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            repository.deleteGroup(group)
        }
    }
}
