package com.tumba.bhaga.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    BUY, SELL
}

@Entity(
    tableName = "transaction",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("ticker")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val ticker: String,
    val companyName: String,
    val type: TransactionType,
    val quantity: Int,
    val pricePerShare: Double,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)