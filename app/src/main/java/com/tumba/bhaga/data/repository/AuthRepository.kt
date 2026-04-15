package com.tumba.bhaga.data.repository

import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.UserEntity
import com.tumba.bhaga.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ENHANCED VERSION: Backward compatible with existing plain text passwords
 *
 * This version handles both:
 * - New users (with hashed passwords)
 * - Old users (with plain text passwords) - automatically hashes on first login
 *
 * Use this if you have existing user accounts with plain text passwords
 * that you want to migrate without forcing password resets.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    private var currentUserId: Long? = null

    companion object {
        private val ADMIN_EMAILS = setOf(
            "admin@bhaga.com",
            "superadmin@bhaga.com"
        )

        // Bcrypt hash prefixes - used to detect if password is hashed
        private const val BCRYPT_PREFIX_2A = "$2a$"
        private const val BCRYPT_PREFIX_2B = "$2b$"
        private const val BCRYPT_PREFIX_2X = "$2x$"
        private const val BCRYPT_PREFIX_2Y = "$2y$"
    }

    suspend fun signUp(name: String, email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check if email already exists
                if (userDao.emailExists(email)) {
                    throw Exception("Email already exists")
                }

                // Hash the password before storing
                val hashedPassword = PasswordHasher.hashPassword(password)

                // Create new user with hashed password
                val user = UserEntity(
                    name = name,
                    email = email,
                    password = hashedPassword  // Store the hash, not plain text
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

    /**
     * Enhanced login that handles both plain text and hashed passwords
     *
     * For plain text passwords (old users):
     * - Verifies the plain text directly
     * - Automatically hashes and updates the password in DB
     *
     * For hashed passwords (new users):
     * - Verifies using bcrypt normally
     */
    suspend fun login(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Get user by email
                val user = userDao.getUserByEmail(email)

                if (user != null) {
                    // Check if password is already hashed (starts with bcrypt prefix)
                    if (isPasswordHashed(user.password)) {
                        // Password is already hashed - verify normally
                        if (PasswordHasher.verifyPassword(password, user.password)) {
                            currentUserId = user.id
                            true
                        } else {
                            false
                        }
                    } else {
                        // Password is plain text (old user from before bcrypt migration)
                        if (user.password == password) {
                            // Password matches! But we need to hash it for future logins
                            val hashedPassword = PasswordHasher.hashPassword(password)

                            // Update the password in database
                            userDao.updatePassword(user.id, hashedPassword)

                            // Login successful
                            currentUserId = user.id
                            true
                        } else {
                            // Plain text password doesn't match
                            false
                        }
                    }
                } else {
                    // User not found
                    false
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    /**
     * Detects if a password is already hashed (bcrypt format)
     *
     * Bcrypt hashes start with $2a$, $2b$, $2x$, or $2y$ prefix
     * Plain text passwords don't have this prefix
     */
    private fun isPasswordHashed(password: String): Boolean {
        return password.startsWith(BCRYPT_PREFIX_2A) ||
                password.startsWith(BCRYPT_PREFIX_2B) ||
                password.startsWith(BCRYPT_PREFIX_2X) ||
                password.startsWith(BCRYPT_PREFIX_2Y)
    }

    suspend fun getCurrentUser(): UserEntity? {
        return withContext(Dispatchers.IO) {
            currentUserId?.let { userDao.getUserById(it) }
        }
    }

    suspend fun isCurrentUserAdmin(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = getCurrentUser()
            user?.email in ADMIN_EMAILS
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