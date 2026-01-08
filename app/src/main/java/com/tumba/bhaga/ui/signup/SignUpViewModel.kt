package com.tumba.bhaga.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumba.bhaga.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible

    private val _isConfirmPasswordVisible = MutableStateFlow(false)
    val isConfirmPasswordVisible: StateFlow<Boolean> = _isConfirmPasswordVisible

    fun onNameChange(newName: String) {
        _name.value = newName
        _errorMessage.value = null
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _errorMessage.value = null
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun toggleConfirmPasswordVisibility() {
        _isConfirmPasswordVisible.value = !_isConfirmPasswordVisible.value
    }

    fun signUp() {
        // Validation
        when {
            _name.value.isBlank() -> {
                _errorMessage.value = "Please enter your name"
                return
            }
            _email.value.isBlank() -> {
                _errorMessage.value = "Please enter your email"
                return
            }
            !isValidEmail(_email.value) -> {
                _errorMessage.value = "Please enter a valid email address with @ and .com"
                return
            }
            _password.value.isBlank() -> {
                _errorMessage.value = "Please enter a password"
                return
            }
            _password.value.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                return
            }
            _password.value != _confirmPassword.value -> {
                _errorMessage.value = "Passwords do not match"
                return
            }
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val success = authRepository.signUp(
                    name = _name.value,
                    email = _email.value,
                    password = _password.value
                )
                if (success) {
                    _signUpSuccess.value = true
                } else {
                    _errorMessage.value = "Sign up failed. Please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.endsWith(".com")
    }
}