package com.hotian.ta.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotian.ta.data.AppDatabase
import com.hotian.ta.data.Group
import com.hotian.ta.data.Message
import com.hotian.ta.data.MessageType
import com.hotian.ta.data.User
import com.hotian.ta.repository.ChatRepository
import com.hotian.ta.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database.messageDao(), database.groupDao())
    private val userRepository = UserRepository(database.userDao())

    private val _currentGroupId = MutableStateFlow(0L)
    val currentGroupId: StateFlow<Long> = _currentGroupId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // 原始消息列表(未过滤)，用于搜索后恢复
    private val _allMessages = MutableStateFlow<List<Message>>(emptyList())

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var hasEnsuredDefaultGroup = false

    init {
        println("ChatViewModel: init started")
        ensureDefaultUser()
        ensureDefaultGroup()
        loadGroups()
        loadUsers()
        observeCurrentGroupMessages()
        println("ChatViewModel: init completed")
    }

    private fun ensureDefaultUser() {
        viewModelScope.launch {
            val defaultUser = userRepository.ensureDefaultUser()
            _currentUser.value = defaultUser
            println("ChatViewModel: Current user set to: ${defaultUser.name}")
        }
    }

    private fun ensureDefaultGroup() {
        viewModelScope.launch {
            // 只检查一次，如果没有群组则创建默认群组
            val groupList = repository.getAllGroupsList()
            if (groupList.isEmpty()) {
                val defaultGroupId = repository.createGroup("默认对话")
                _currentGroupId.value = defaultGroupId
                println("ChatViewModel: Created default group with ID: $defaultGroupId")
            } else {
                if (_currentGroupId.value == 0L) {
                    _currentGroupId.value = groupList.first().id
                    println("ChatViewModel: Set current group to first group ID: ${groupList.first().id}")
                }
            }
            hasEnsuredDefaultGroup = true
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

    private fun loadUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { userList ->
                _users.value = userList
            }
        }
    }

    private fun observeCurrentGroupMessages() {
        println("ChatViewModel: observeCurrentGroupMessages called")
        viewModelScope.launch {
            println("ChatViewModel: observeCurrentGroupMessages - starting to collect")
            _currentGroupId
                .flatMapLatest { groupId ->
                    println("ChatViewModel: observeCurrentGroupMessages - groupId changed to $groupId")
                    if (groupId != 0L) {
                        repository.getMessagesForGroup(groupId)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { messageList ->
                    println("ChatViewModel: observeCurrentGroupMessages - received ${messageList.size} messages")
                    _allMessages.value = messageList
                    // 如果正在搜索，过滤消息；否则显示全部
                    if (_isSearching.value && _searchQuery.value.isNotBlank()) {
                        _messages.value = messageList.filter { message ->
                            message.content.contains(_searchQuery.value, ignoreCase = true)
                        }
                    } else {
                        _messages.value = messageList
                    }
                }
        }
    }

    fun sendMessage(content: String, type: MessageType = MessageType.TEXT, attachmentUri: Uri? = null) {
        println("ChatViewModel.sendMessage called - content: '$content', type: $type, currentGroupId: ${_currentGroupId.value}")

        // 文本消息必须有内容，图片消息可以没有文字描述
        if (content.isBlank() && type == MessageType.TEXT) {
            println("ChatViewModel.sendMessage - Rejected: blank text message")
            return
        }

        // 确保有有效的群组ID
        if (_currentGroupId.value == 0L) {
            println("ChatViewModel.sendMessage - Rejected: currentGroupId is 0")
            return
        }

        // 确保有当前用户
        val currentUserId = _currentUser.value?.id ?: 1L

        viewModelScope.launch {
            val message = Message(
                content = content.ifBlank { if (type == MessageType.IMAGE) "图片" else "" },
                groupId = _currentGroupId.value,
                type = type.name,
                attachmentUri = attachmentUri?.toString(),
                senderId = currentUserId
            )
            println("ChatViewModel.sendMessage - Sending message: $message")
            repository.sendMessage(message)
            println("ChatViewModel.sendMessage - Message sent successfully")
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
            _messages.value = _allMessages.value
            return
        }
        _isSearching.value = true
        // 从原始消息列表中过滤，保持原有顺序
        _messages.value = _allMessages.value.filter { message ->
            message.content.contains(query, ignoreCase = true)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
        _messages.value = _allMessages.value
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

    // 用户管理功能
    fun switchUser(user: User) {
        _currentUser.value = user
        println("ChatViewModel: Switched to user: ${user.name}")
    }

    fun createUser(name: String, avatarColor: String = "#FF6200EE") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val userId = userRepository.createUser(name, avatarColor)
            val newUser = userRepository.getUserById(userId)
            newUser?.let {
                _currentUser.value = it
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = user
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            // 不允许删除默认用户
            if (user.isDefault) return@launch

            userRepository.deleteUser(user)

            // 如果删除的是当前用户，切换到默认用户
            if (_currentUser.value?.id == user.id) {
                val defaultUser = userRepository.getDefaultUser()
                _currentUser.value = defaultUser
            }
        }
    }

    suspend fun getUserById(userId: Long): User? {
        return userRepository.getUserById(userId)
    }
}
