package com.tumba.bhaga.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tumba.bhaga.data.local.entity.BlockedStockEntity

@Dao
interface BlockedStockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockStock(stock: BlockedStockEntity)

    @Query("DELETE FROM blocked_stocks WHERE ticker = :ticker")
    suspend fun unblockStock(ticker: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_stocks WHERE ticker = :ticker)")
    suspend fun isStockBlocked(ticker: String): Boolean

    @Query("SELECT * FROM blocked_stocks ORDER BY blockedAt DESC")
    suspend fun getAllBlockedStocks(): List<BlockedStockEntity>
}