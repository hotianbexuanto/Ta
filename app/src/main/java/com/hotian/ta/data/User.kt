package com.hotian.ta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarColor: String = "#FF6200EE", // 默认主题色
    val createdTime: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false // 标记是否为默认用户
)
