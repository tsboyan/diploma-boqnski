package com.tumba.bhaga.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.local.entity.PortfolioEntity
import com.tumba.bhaga.data.local.entity.UserEntity
import com.tumba.bhaga.data.repository.AuthRepository
import com.tumba.bhaga.data.repository.TradingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tradingRepository: TradingRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _portfolio = MutableStateFlow<List<PortfolioEntity>>(emptyList())
    val portfolio: StateFlow<List<PortfolioEntity>> = _portfolio

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
                    _portfolio.value = tradingRepository.getUserPortfolio(userId)
                }
            } catch (e: Exception) {
                _user.value = null
                _portfolio.value = emptyList()
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