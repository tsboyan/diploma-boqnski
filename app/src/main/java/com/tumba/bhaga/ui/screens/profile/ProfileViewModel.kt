package com.tumba.bhaga.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.local.entity.UserEntity
import com.tumba.bhaga.data.repository.AuthRepository
import com.tumba.bhaga.data.repository.StockRepository
import com.tumba.bhaga.data.repository.TradingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioItemWithPrice(
    val ticker: String,
    val companyName: String,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tradingRepository: TradingRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _portfolioWithPrices = MutableStateFlow<List<PortfolioItemWithPrice>>(emptyList())
    val portfolioWithPrices: StateFlow<List<PortfolioItemWithPrice>> = _portfolioWithPrices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess: StateFlow<Boolean> = _logoutSuccess

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _user.value = authRepository.getCurrentUser()

                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val portfolio = tradingRepository.getUserPortfolio(userId)

                    // Fetch current prices for each stock
                    val portfolioWithPrices = portfolio.map { portfolioItem ->
                        val stockSummary = stockRepository.getStockSummary(portfolioItem.ticker)
                        PortfolioItemWithPrice(
                            ticker = portfolioItem.ticker,
                            companyName = portfolioItem.companyName,
                            quantity = portfolioItem.quantity,
                            averagePrice = portfolioItem.averagePrice,
                            currentPrice = stockSummary.currentPrice
                        )
                    }

                    _portfolioWithPrices.value = portfolioWithPrices
                }
            } catch (e: Exception) {
                _user.value = null
                _portfolioWithPrices.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _logoutSuccess.value = true
    }
}