package com.tumba.bhaga.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tumba.bhaga.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM user WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM user WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("UPDATE user SET balance = :newBalance WHERE id = :userId")
    suspend fun updateBalance(userId: Long, newBalance: Double)

    @Query("SELECT balance FROM user WHERE id = :userId")
    suspend fun getBalance(userId: Long): Double?
}