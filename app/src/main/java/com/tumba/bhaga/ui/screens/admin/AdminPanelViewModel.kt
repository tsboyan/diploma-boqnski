package com.tumba.bhaga.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.local.entity.BlockedStockEntity
import com.tumba.bhaga.data.local.entity.TransactionEntity
import com.tumba.bhaga.data.local.entity.UserEntity
import com.tumba.bhaga.data.repository.AdminRepository
import com.tumba.bhaga.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _allUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allUsers: StateFlow<List<UserEntity>> = _allUsers

    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<TransactionEntity>> = _allTransactions

    private val _blockedStocks = MutableStateFlow<List<BlockedStockEntity>>(emptyList())
    val blockedStocks: StateFlow<List<BlockedStockEntity>> = _blockedStocks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadAllData()
    }

    private fun loadAllData() {
        loadUsers()
        loadTransactions()
        loadBlockedStocks()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allUsers.value = adminRepository.getAllUsers()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _allTransactions.value = adminRepository.getAllTransactions()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load transactions: ${e.message}"
            }
        }
    }

    fun loadBlockedStocks() {
        viewModelScope.launch {
            try {
                _blockedStocks.value = adminRepository.getAllBlockedStocks()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load blocked stocks: ${e.message}"
            }
        }
    }

    fun adjustUserBalance(userId: Long, amount: Double) {
        viewModelScope.launch {
            try {
                val user = _allUsers.value.find { it.id == userId }
                if (user != null) {
                    val newBalance = user.balance + amount
                    if (newBalance < 0) {
                        _errorMessage.value = "Balance cannot be negative"
                        return@launch
                    }
                    adminRepository.updateUserBalance(userId, newBalance)
                    _successMessage.value = "Balance updated successfully"
                    loadUsers()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update balance: ${e.message}"
            }
        }
    }

    fun blockStock(ticker: String, reason: String) {
        viewModelScope.launch {
            try {
                val admin = authRepository.getCurrentUser()
                if (admin != null) {
                    adminRepository.blockStock(ticker, reason, admin.email)
                    _successMessage.value = "Stock $ticker blocked successfully"
                    loadBlockedStocks()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to block stock: ${e.message}"
            }
        }
    }

    fun unblockStock(ticker: String) {
        viewModelScope.launch {
            try {
                adminRepository.unblockStock(ticker)
                _successMessage.value = "Stock $ticker unblocked successfully"
                loadBlockedStocks()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unblock stock: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }
}