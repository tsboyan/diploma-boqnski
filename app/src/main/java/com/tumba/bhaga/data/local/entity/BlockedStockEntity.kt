package com.tumba.bhaga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_stocks")
data class BlockedStockEntity(
    @PrimaryKey val ticker: String,
    val reason: String,
    val blockedAt: Long = System.currentTimeMillis(),
    val blockedBy: String // Admin email who blocked it
)