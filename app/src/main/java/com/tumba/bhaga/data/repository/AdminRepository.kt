package com.tumba.bhaga.data.repository

import com.tumba.bhaga.data.local.dao.BlockedStockDao
import com.tumba.bhaga.data.local.dao.PortfolioDao
import com.tumba.bhaga.data.local.dao.TransactionDao
import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.BlockedStockEntity
import com.tumba.bhaga.data.local.entity.PortfolioEntity
import com.tumba.bhaga.data.local.entity.TransactionEntity
import com.tumba.bhaga.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val userDao: UserDao,
    private val portfolioDao: PortfolioDao,
    private val transactionDao: TransactionDao,
    private val blockedStockDao: BlockedStockDao
) {
    companion object {
        private val ADMIN_EMAILS = setOf(
            "admin@bhaga.com",
            "superadmin@bhaga.com"
        )
    }

    fun isAdmin(email: String): Boolean {
        return email in ADMIN_EMAILS
    }

    suspend fun getAllUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        userDao.getAllUsers()
    }

    suspend fun getUserWithPortfolio(userId: Long): Pair<UserEntity?, List<PortfolioEntity>> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        val portfolio = portfolioDao.getUserPortfolio(userId)
        user to portfolio
    }

    suspend fun updateUserBalance(userId: Long, newBalance: Double) = withContext(Dispatchers.IO) {
        userDao.updateBalance(userId, newBalance)
    }

    suspend fun getAllTransactions(): List<TransactionEntity> = withContext(Dispatchers.IO) {
        transactionDao.getAllTransactions()
    }

    suspend fun blockStock(ticker: String, reason: String, adminEmail: String) = withContext(Dispatchers.IO) {
        val blockedStock = BlockedStockEntity(
            ticker = ticker,
            reason = reason,
            blockedBy = adminEmail
        )
        blockedStockDao.blockStock(blockedStock)
    }

    suspend fun unblockStock(ticker: String) = withContext(Dispatchers.IO) {
        blockedStockDao.unblockStock(ticker)
    }

    suspend fun isStockBlocked(ticker: String): Boolean = withContext(Dispatchers.IO) {
        blockedStockDao.isStockBlocked(ticker)
    }

    suspend fun getAllBlockedStocks(): List<BlockedStockEntity> = withContext(Dispatchers.IO) {
        blockedStockDao.getAllBlockedStocks()
    }
}