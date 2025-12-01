package com.hotian.ta.repository

import com.hotian.ta.data.Group
import com.hotian.ta.data.GroupDao
import com.hotian.ta.data.Message
import com.hotian.ta.data.MessageDao
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val messageDao: MessageDao,
    private val groupDao: GroupDao
) {
    fun getMessagesForGroup(groupId: Long): Flow<List<Message>> {
        return messageDao.getMessagesForGroup(groupId)
    }

    suspend fun sendMessage(message: Message) {
        messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(message)
    }

    suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteMessage(messageId)
    }

    fun searchMessages(groupId: Long, query: String): Flow<List<Message>> {
        return messageDao.searchMessages(groupId, query)
    }

    fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups()
    }

    suspend fun getAllGroupsList(): List<Group> {
        return groupDao.getAllGroupsList()
    }

    suspend fun createGroup(name: String): Long {
        return groupDao.insertGroup(Group(name = name))
    }

    suspend fun deleteGroup(group: Group) {
        messageDao.deleteMessagesInGroup(group.id)
        groupDao.deleteGroup(group)
    }
}
