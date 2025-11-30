package com.hotian.ta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getMessagesForGroup(groupId: Long): Flow<List<Message>>

    @Insert
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages WHERE groupId = :groupId")
    suspend fun deleteMessagesInGroup(groupId: Long)
}
