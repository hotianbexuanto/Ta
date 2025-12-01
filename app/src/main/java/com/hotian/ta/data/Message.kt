package com.hotian.ta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT, IMAGE, FILE
}

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val groupId: Long = 0,
    val type: String = MessageType.TEXT.name,
    val attachmentUri: String? = null,
    val isEdited: Boolean = false,
    val editedTimestamp: Long? = null,
    val senderId: Long = 1L // 发送者用户ID，默认为1（默认用户）
)
