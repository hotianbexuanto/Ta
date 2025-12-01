package com.hotian.ta.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotian.ta.data.AppDatabase
import com.hotian.ta.data.Group
import com.hotian.ta.data.Message
import com.hotian.ta.data.MessageType
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var hasEnsuredDefaultGroup = false

    init {
        ensureDefaultGroup()
        loadGroups()
        observeCurrentGroupMessages()
    }

    private fun ensureDefaultGroup() {
        viewModelScope.launch {
            // 只检查一次，如果没有群组则创建默认群组
            val groups = repository.getAllGroups()
            groups.collect { groupList ->
                if (!hasEnsuredDefaultGroup && groupList.isEmpty()) {
                    hasEnsuredDefaultGroup = true
                    val defaultGroupId = repository.createGroup("默认对话")
                    _currentGroupId.value = defaultGroupId
                } else if (!hasEnsuredDefaultGroup && groupList.isNotEmpty()) {
                    hasEnsuredDefaultGroup = true
                    if (_currentGroupId.value == 0L) {
                        _currentGroupId.value = groupList.first().id
                    }
                }
            }
        }
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

    fun sendMessage(content: String, type: MessageType = MessageType.TEXT, attachmentUri: Uri? = null) {
        // 文本消息必须有内容，图片消息可以没有文字描述
        if (content.isBlank() && type == MessageType.TEXT) return
        // 确保有有效的群组ID
        if (_currentGroupId.value == 0L) return

        viewModelScope.launch {
            val message = Message(
                content = content.ifBlank { if (type == MessageType.IMAGE) "图片" else "" },
                groupId = _currentGroupId.value,
                type = type.name,
                attachmentUri = attachmentUri?.toString()
            )
            repository.sendMessage(message)
        }
    }

    fun editMessage(message: Message, newContent: String) {
        viewModelScope.launch {
            val updatedMessage = message.copy(
                content = newContent,
                isEdited = true,
                editedTimestamp = System.currentTimeMillis()
            )
            repository.updateMessage(updatedMessage)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun searchMessages(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _isSearching.value = false
            return
        }
        _isSearching.value = true
        viewModelScope.launch {
            repository.searchMessages(_currentGroupId.value, query).collect { searchResults ->
                _messages.value = searchResults
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
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
        clearSearch()
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            repository.deleteGroup(group)
        }
    }
}
