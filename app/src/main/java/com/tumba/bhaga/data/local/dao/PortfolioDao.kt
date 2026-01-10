package com.tumba.bhaga.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tumba.bhaga.data.local.entity.PortfolioEntity

@Dao
interface PortfolioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long

    @Update
    suspend fun updatePortfolio(portfolio: PortfolioEntity)

    @Query("SELECT * FROM portfolio WHERE userId = :userId")
    suspend fun getUserPortfolio(userId: Long): List<PortfolioEntity>

    @Query("SELECT * FROM portfolio WHERE userId = :userId AND ticker = :ticker LIMIT 1")
    suspend fun getPortfolioItem(userId: Long, ticker: String): PortfolioEntity?

    @Query("DELETE FROM portfolio WHERE userId = :userId AND ticker = :ticker")
    suspend fun deletePortfolioItem(userId: Long, ticker: String)

    @Query("SELECT SUM(quantity * averagePrice) FROM portfolio WHERE userId = :userId")
    suspend fun getTotalPortfolioValue(userId: Long): Double?
}