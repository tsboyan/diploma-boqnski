package com.tumba.bhaga.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tumba.bhaga.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM `transaction` WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getUserTransactions(userId: Long): List<TransactionEntity>

    @Query("SELECT * FROM `transaction` WHERE userId = :userId AND ticker = :ticker ORDER BY timestamp DESC")
    suspend fun getTransactionsForStock(userId: Long, ticker: String): List<TransactionEntity>

    @Query("SELECT * FROM `transaction` ORDER BY timestamp DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>
}