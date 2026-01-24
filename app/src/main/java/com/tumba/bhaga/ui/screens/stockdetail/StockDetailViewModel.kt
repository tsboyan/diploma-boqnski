package com.tumba.bhaga.ui.screens.stockdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.repository.AuthRepository
import com.tumba.bhaga.data.repository.FavouritesRepository
import com.tumba.bhaga.data.repository.StockRepository
import com.tumba.bhaga.data.repository.TradingRepository
import com.tumba.bhaga.domain.models.StockDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val repository: StockRepository,
    private val favouritesRepository: FavouritesRepository,
    private val tradingRepository: TradingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _stock = MutableStateFlow<StockDetail?>(null)
    val stock: StateFlow<StockDetail?> = _stock

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite

    private val _userBalance = MutableStateFlow(0.0)
    val userBalance: StateFlow<Double> = _userBalance

    private val _ownedShares = MutableStateFlow(0)
    val ownedShares: StateFlow<Int> = _ownedShares

    private val _averagePrice = MutableStateFlow(0.0)
    val averagePrice: StateFlow<Double> = _averagePrice

    private val _transactionSuccess = MutableStateFlow<String?>(null)
    val transactionSuccess: StateFlow<String?> = _transactionSuccess

    private val _transactionError = MutableStateFlow<String?>(null)
    val transactionError: StateFlow<String?> = _transactionError

    fun loadStock(ticker: String) {
        viewModelScope.launch {
            _stock.value = repository.getStockDetail(ticker)
            _isFavourite.value = favouritesRepository.checkFavourite(ticker)

            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val user = authRepository.getCurrentUser()
                _userBalance.value = user?.balance ?: 0.0

                val portfolio = tradingRepository.getPortfolioItem(userId, ticker)
                _ownedShares.value = portfolio?.quantity ?: 0
                _averagePrice.value = portfolio?.averagePrice ?: 0.0
            }
        }
    }

    fun addToFavourites() {
        viewModelScope.launch {
            _stock.value?.ticker?.let {
                favouritesRepository.addFavourite(it)
                _isFavourite.value = true
            }
        }
    }

    fun removeFromFavourites() {
        viewModelScope.launch {
            _stock.value?.ticker?.let {
                favouritesRepository.removeFavourite(it)
                _isFavourite.value = false
            }
        }
    }

    fun buyStock(quantity: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val stockDetail = _stock.value

            if (userId != null && stockDetail != null) {
                val result = tradingRepository.buyStock(
                    userId = userId,
                    ticker = stockDetail.ticker,
                    companyName = stockDetail.companyName,
                    quantity = quantity,
                    pricePerShare = stockDetail.currentPrice
                )

                result.fold(
                    onSuccess = {
                        _transactionSuccess.value = "Successfully bought $quantity shares"
                        loadStock(stockDetail.ticker)
                    },
                    onFailure = { error ->
                        _transactionError.value = error.message ?: "Transaction failed"
                    }
                )
            }
        }
    }

    fun sellStock(quantity: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val stockDetail = _stock.value

            if (userId != null && stockDetail != null) {
                val result = tradingRepository.sellStock(
                    userId = userId,
                    ticker = stockDetail.ticker,
                    companyName = stockDetail.companyName,
                    quantity = quantity,
                    pricePerShare = stockDetail.currentPrice
                )

                result.fold(
                    onSuccess = {
                        _transactionSuccess.value = "Successfully sold $quantity shares"
                        loadStock(stockDetail.ticker)
                    },
                    onFailure = { error ->
                        _transactionError.value = error.message ?: "Transaction failed"
                    }
                )
            }
        }
    }

    fun clearTransactionMessages() {
        _transactionSuccess.value = null
        _transactionError.value = null
    }
}