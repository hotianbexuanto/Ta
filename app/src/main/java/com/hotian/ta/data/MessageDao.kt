package com.hotian.ta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getMessagesForGroup(groupId: Long): Flow<List<Message>>

    @Insert
    suspend fun insertMessage(message: Message)

    @Update
    suspend fun updateMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM messages WHERE groupId = :groupId")
    suspend fun deleteMessagesInGroup(groupId: Long)

    @Query("SELECT * FROM messages WHERE groupId = :groupId AND content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(groupId: Long, query: String): Flow<List<Message>>
}
