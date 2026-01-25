package com.tumba.bhaga.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.local.ThemeManager
import com.tumba.bhaga.data.local.ThemeMode
import com.tumba.bhaga.data.local.TokenManager
import com.tumba.bhaga.data.local.TokenValidator
import com.tumba.bhaga.data.repository.InvalidationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: InvalidationRepository,
    private val tokenManager: TokenManager,
    private val tokenValidator: TokenValidator,
    private val themeManager: ThemeManager
) : ViewModel() {
    private val _isTokenValid = MutableStateFlow<Boolean?>(null)
    val isTokenValid: StateFlow<Boolean?> = _isTokenValid

    private val _tokenInitial = MutableStateFlow("")
    val tokenInitial: StateFlow<String> = _tokenInitial

    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    init {
        getToken()
    }

    private fun getToken() {
        viewModelScope.launch {
            _tokenInitial.value = tokenManager.getToken()
        }
    }

    fun clearStockCache() {
        viewModelScope.launch {
            repository.invalidateAllStockData()
        }
    }

    fun clearCompanyCache() {
        viewModelScope.launch {
            repository.invalidateAllCompanyData()
        }
    }

    fun clearNewsCache() {
        viewModelScope.launch {
            repository.invalidateAllNewsData()
        }
    }

    fun checkTokenValidity(token: String) {
        viewModelScope.launch {
            _isTokenValid.value = tokenValidator.isValid(token)
        }
    }

    fun setNewToken(token: String) {
        viewModelScope.launch {
            tokenManager.saveToken(token)
        }
    }

    fun resetNewToken() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _tokenInitial.value = tokenManager.getToken()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(mode)
        }
    }
}