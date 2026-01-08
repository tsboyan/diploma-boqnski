package com.tumba.bhaga.data.repository

import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    private var currentUserId: Long? = null

    suspend fun signUp(name: String, email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check if email already exists
                if (userDao.emailExists(email)) {
                    throw Exception("Email already exists")
                }

                // Create new user
                val user = UserEntity(
                    name = name,
                    email = email,
                    password = password
                )

                // Insert user and get the generated ID
                val userId = userDao.insertUser(user)
                currentUserId = userId
                true
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.login(email, password)
                if (user != null) {
                    currentUserId = user.id
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun getCurrentUser(): UserEntity? {
        return withContext(Dispatchers.IO) {
            currentUserId?.let { userDao.getUserById(it) }
        }
    }

    fun logout() {
        currentUserId = null
    }

    fun isLoggedIn(): Boolean {
        return currentUserId != null
    }

    fun getCurrentUserId(): Long? {
        return currentUserId
    }
}