package com.tumba.bhaga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val balance: Double = 10000.0, // Starting balance of $10,000
    val createdAt: Long = System.currentTimeMillis()
)