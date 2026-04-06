package com.tumba.bhaga

import com.tumba.bhaga.data.local.dao.BlockedStockDao
import com.tumba.bhaga.data.local.dao.PortfolioDao
import com.tumba.bhaga.data.local.dao.TransactionDao
import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.PortfolioEntity
import com.tumba.bhaga.data.repository.TradingRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class TradingRepositoryTest {

    private lateinit var repository: TradingRepository
    private lateinit var userDao: UserDao
    private lateinit var portfolioDao: PortfolioDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var blockedStockDao: BlockedStockDao

    @Before
    fun setup() {
        userDao = mockk()
        portfolioDao = mockk()
        transactionDao = mockk()
        blockedStockDao = mockk()
        repository = TradingRepository(userDao, portfolioDao, transactionDao, blockedStockDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun buyStockwithsufficientfundsshouldsucceed() = runTest {
        // Given
        val userId = 1L
        val ticker = "AAPL"
        val quantity = 10
        val pricePerShare = 150.0
        val currentBalance = 2000.0

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns false
        coEvery { userDao.getBalance(userId) } returns currentBalance
        coEvery { userDao.updateBalance(any(), any()) } just Runs
        coEvery { portfolioDao.getPortfolioItem(userId, ticker) } returns null
        coEvery { portfolioDao.insertPortfolio(any()) } returns 1L
        coEvery { transactionDao.insertTransaction(any()) } returns 1L

        // When
        val result = repository.buyStock(userId, ticker, "Apple", quantity, pricePerShare)

        // Then
        assertTrue(result.isSuccess)
        coVerify { userDao.updateBalance(userId, currentBalance - (quantity * pricePerShare)) }
        coVerify { portfolioDao.insertPortfolio(any()) }
        coVerify { transactionDao.insertTransaction(any()) }
    }

    @Test
    fun buyStockwithinsufficientfundsshouldfail() = runTest {
        // Given
        val userId = 1L
        val ticker = "AAPL"
        val quantity = 10
        val pricePerShare = 150.0
        val currentBalance = 100.0

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns false
        coEvery { userDao.getBalance(userId) } returns currentBalance

        // When
        val result = repository.buyStock(userId, ticker, "Apple", quantity, pricePerShare)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Insufficient funds", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { userDao.updateBalance(any(), any()) }
        coVerify(exactly = 0) { portfolioDao.insertPortfolio(any()) }
    }

    @Test
    fun buyStockwithblockedstockshouldfail() = runTest {
        // Given
        val userId = 1L
        val ticker = "TSLA"

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns true

        // When
        val result = repository.buyStock(userId, ticker, "Tesla", 5, 200.0)

        // Then
        assertTrue(result.isFailure)
        assertEquals("This stock is currently blocked from trading", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { userDao.getBalance(any()) }
    }

    @Test
    fun buyStockwhenuserownsstockshouldupdateaverageprice() = runTest {
        // Given
        val userId = 1L
        val ticker = "AAPL"
        val existingQuantity = 5
        val existingAvgPrice = 140.0
        val newQuantity = 5
        val newPrice = 160.0
        val currentBalance = 1000.0

        val existingPortfolio = PortfolioEntity(
            id = 1L,
            userId = userId,
            ticker = ticker,
            companyName = "Apple",
            quantity = existingQuantity,
            averagePrice = existingAvgPrice
        )

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns false
        coEvery { userDao.getBalance(userId) } returns currentBalance
        coEvery { userDao.updateBalance(any(), any()) } just Runs
        coEvery { portfolioDao.getPortfolioItem(userId, ticker) } returns existingPortfolio
        coEvery { portfolioDao.updatePortfolio(any()) } just Runs
        coEvery { transactionDao.insertTransaction(any()) } returns 1L

        // When
        val result = repository.buyStock(userId, ticker, "Apple", newQuantity, newPrice)

        // Then
        assertTrue(result.isSuccess)

        // Verify average price calculation
        val expectedTotal = (existingQuantity * existingAvgPrice) + (newQuantity * newPrice)
        val expectedAvgPrice = expectedTotal / (existingQuantity + newQuantity)

        coVerify {
            portfolioDao.updatePortfolio(
                match {
                    it.quantity == existingQuantity + newQuantity &&
                            it.averagePrice == expectedAvgPrice
                }
            )
        }
    }

    @Test
    fun sellStockwithsufficientsharesshouldsucceed() = runTest {
        // Given
        val userId = 1L
        val ticker = "AAPL"
        val ownedQuantity = 10
        val sellQuantity = 5
        val pricePerShare = 150.0
        val currentBalance = 1000.0

        val portfolio = PortfolioEntity(
            id = 1L,
            userId = userId,
            ticker = ticker,
            companyName = "Apple",
            quantity = ownedQuantity,
            averagePrice = 140.0
        )

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns false
        coEvery { portfolioDao.getPortfolioItem(userId, ticker) } returns portfolio
        coEvery { userDao.getBalance(userId) } returns currentBalance
        coEvery { userDao.updateBalance(any(), any()) } just Runs
        coEvery { portfolioDao.updatePortfolio(any()) } just Runs
        coEvery { transactionDao.insertTransaction(any()) } returns 1L

        // When
        val result = repository.sellStock(userId, ticker, "Apple", sellQuantity, pricePerShare)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            userDao.updateBalance(userId, currentBalance + (sellQuantity * pricePerShare))
        }
        coVerify {
            portfolioDao.updatePortfolio(
                match { it.quantity == ownedQuantity - sellQuantity }
            )
        }
    }

    @Test
    fun sellStockwithinsufficientsharesshouldfail() = runTest {
        // Given
        val userId = 1L
        val ticker = "AAPL"
        val ownedQuantity = 5
        val sellQuantity = 10

        val portfolio = PortfolioEntity(
            id = 1L,
            userId = userId,
            ticker = ticker,
            companyName = "Apple",
            quantity = ownedQuantity,
            averagePrice = 140.0
        )

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns false
        coEvery { portfolioDao.getPortfolioItem(userId, ticker) } returns portfolio

        // When
        val result = repository.sellStock(userId, ticker, "Apple", sellQuantity, 150.0)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Insufficient shares", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { userDao.updateBalance(any(), any()) }
    }

    @Test
    fun sellStockwhensellingallsharesshoulddeleteportfolioitem() = runTest {
        // Given
        val userId = 1L
        val ticker = "AAPL"
        val quantity = 10
        val currentBalance = 1000.0

        val portfolio = PortfolioEntity(
            id = 1L,
            userId = userId,
            ticker = ticker,
            companyName = "Apple",
            quantity = quantity,
            averagePrice = 140.0
        )

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns false
        coEvery { portfolioDao.getPortfolioItem(userId, ticker) } returns portfolio
        coEvery { userDao.getBalance(userId) } returns currentBalance
        coEvery { userDao.updateBalance(any(), any()) } just Runs
        coEvery { portfolioDao.deletePortfolioItem(userId, ticker) } just Runs
        coEvery { transactionDao.insertTransaction(any()) } returns 1L

        // When
        val result = repository.sellStock(userId, ticker, "Apple", quantity, 150.0)

        // Then
        assertTrue(result.isSuccess)
        coVerify { portfolioDao.deletePortfolioItem(userId, ticker) }
        coVerify(exactly = 0) { portfolioDao.updatePortfolio(any()) }
    }

    @Test
    fun sellStockwhenuserdoesntownstockshouldfail() = runTest {
        // Given
        val userId = 1L
        val ticker = "AAPL"

        coEvery { blockedStockDao.isStockBlocked(ticker) } returns false
        coEvery { portfolioDao.getPortfolioItem(userId, ticker) } returns null

        // When
        val result = repository.sellStock(userId, ticker, "Apple", 5, 150.0)

        // Then
        assertTrue(result.isFailure)
        assertEquals("You don't own this stock", result.exceptionOrNull()?.message)
    }
}