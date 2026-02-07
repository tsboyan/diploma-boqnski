package com.tumba.bhaga.data.repository

import com.tumba.bhaga.data.local.dao.BlockedStockDao
import com.tumba.bhaga.data.local.dao.PortfolioDao
import com.tumba.bhaga.data.local.dao.TransactionDao
import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.PortfolioEntity
import com.tumba.bhaga.data.local.entity.TransactionEntity
import com.tumba.bhaga.data.local.entity.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradingRepository @Inject constructor(
    private val userDao: UserDao,
    private val portfolioDao: PortfolioDao,
    private val transactionDao: TransactionDao,
    private val blockedStockDao: BlockedStockDao
) {
    suspend fun buyStock(
        userId: Long,
        ticker: String,
        companyName: String,
        quantity: Int,
        pricePerShare: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if stock is blocked
            if (blockedStockDao.isStockBlocked(ticker)) {
                return@withContext Result.failure(Exception("This stock is currently blocked from trading"))
            }

            val totalCost = quantity * pricePerShare
            val currentBalance = userDao.getBalance(userId) ?: 0.0

            if (currentBalance < totalCost) {
                return@withContext Result.failure(Exception("Insufficient funds"))
            }

            userDao.updateBalance(userId, currentBalance - totalCost)

            val existingPortfolio = portfolioDao.getPortfolioItem(userId, ticker)
            if (existingPortfolio != null) {
                val totalQuantity = existingPortfolio.quantity + quantity
                val totalValue = (existingPortfolio.quantity * existingPortfolio.averagePrice) + totalCost
                val newAveragePrice = totalValue / totalQuantity

                portfolioDao.updatePortfolio(
                    existingPortfolio.copy(
                        quantity = totalQuantity,
                        averagePrice = newAveragePrice,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } else {
                portfolioDao.insertPortfolio(
                    PortfolioEntity(
                        userId = userId,
                        ticker = ticker,
                        companyName = companyName,
                        quantity = quantity,
                        averagePrice = pricePerShare
                    )
                )
            }

            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    ticker = ticker,
                    companyName = companyName,
                    type = TransactionType.BUY,
                    quantity = quantity,
                    pricePerShare = pricePerShare,
                    totalAmount = totalCost
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sellStock(
        userId: Long,
        ticker: String,
        companyName: String,
        quantity: Int,
        pricePerShare: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if stock is blocked
            if (blockedStockDao.isStockBlocked(ticker)) {
                return@withContext Result.failure(Exception("This stock is currently blocked from trading"))
            }

            val portfolio = portfolioDao.getPortfolioItem(userId, ticker)
                ?: return@withContext Result.failure(Exception("You don't own this stock"))

            if (portfolio.quantity < quantity) {
                return@withContext Result.failure(Exception("Insufficient shares"))
            }

            val totalProceeds = quantity * pricePerShare
            val currentBalance = userDao.getBalance(userId) ?: 0.0

            userDao.updateBalance(userId, currentBalance + totalProceeds)

            val newQuantity = portfolio.quantity - quantity
            if (newQuantity == 0) {
                portfolioDao.deletePortfolioItem(userId, ticker)
            } else {
                portfolioDao.updatePortfolio(
                    portfolio.copy(
                        quantity = newQuantity,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    ticker = ticker,
                    companyName = companyName,
                    type = TransactionType.SELL,
                    quantity = quantity,
                    pricePerShare = pricePerShare,
                    totalAmount = totalProceeds
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPortfolio(userId: Long): List<PortfolioEntity> = withContext(Dispatchers.IO) {
        portfolioDao.getUserPortfolio(userId)
    }

    suspend fun getPortfolioItem(userId: Long, ticker: String): PortfolioEntity? = withContext(Dispatchers.IO) {
        portfolioDao.getPortfolioItem(userId, ticker)
    }

    suspend fun getUserTransactions(userId: Long): List<TransactionEntity> = withContext(Dispatchers.IO) {
        transactionDao.getUserTransactions(userId)
    }
}