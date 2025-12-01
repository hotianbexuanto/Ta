package com.hotian.ta.repository

import com.hotian.ta.data.User
import com.hotian.ta.data.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun getAllUsersList(): List<User> {
        return userDao.getAllUsersList()
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getDefaultUser(): User? {
        return userDao.getDefaultUser()
    }

    suspend fun createUser(name: String, avatarColor: String = "#FF6200EE"): Long {
        val user = User(
            name = name,
            avatarColor = avatarColor,
            isDefault = false
        )
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun ensureDefaultUser(): User {
        var defaultUser = getDefaultUser()
        if (defaultUser == null) {
            val userId = userDao.insertUser(
                User(
                    name = "我",
                    avatarColor = "#FF6200EE",
                    isDefault = true
                )
            )
            defaultUser = getUserById(userId)!!
        }
        return defaultUser
    }
}
