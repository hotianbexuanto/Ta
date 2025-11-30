package com.hotian.ta.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY createdTime DESC")
    fun getAllGroups(): Flow<List<Group>>

    @Insert
    suspend fun insertGroup(group: Group): Long

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): Group?
}
