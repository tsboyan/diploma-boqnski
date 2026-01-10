package com.tumba.bhaga.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "portfolio",
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
data class PortfolioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val ticker: String,
    val companyName: String,
    val quantity: Int,
    val averagePrice: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)