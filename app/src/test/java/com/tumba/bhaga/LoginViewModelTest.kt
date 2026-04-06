package com.tumba.bhaga

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tumba.bhaga.data.repository.AuthRepository
import com.tumba.bhaga.ui.screens.login.LoginViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun initialstateshouldbeemptyandnotloading() = runTest {
        assertEquals("", viewModel.email.value)
        assertEquals("", viewModel.password.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.loginSuccess.value)
    }

    @Test
    fun onEmailChangeshouldupdateemailstate() = runTest {
        // When
        viewModel.onEmailChange("test@example.com")

        // Then
        assertEquals("test@example.com", viewModel.email.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun onPasswordChangeshouldupdatepasswordstate() = runTest {
        // When
        viewModel.onPasswordChange("password123")

        // Then
        assertEquals("password123", viewModel.password.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun togglePasswordVisibilityshouldtogglevisibilitystate() = runTest {
        // Given
        assertFalse(viewModel.isPasswordVisible.value)

        // When
        viewModel.togglePasswordVisibility()

        // Then
        assertTrue(viewModel.isPasswordVisible.value)

        // When
        viewModel.togglePasswordVisibility()

        // Then
        assertFalse(viewModel.isPasswordVisible.value)
    }

    @Test
    fun loginwithemptyemailshouldshowerror() = runTest {
        // Given
        viewModel.onPasswordChange("password")

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        assertEquals("Please fill in all fields", viewModel.errorMessage.value)
        assertFalse(viewModel.loginSuccess.value)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun loginwithemptypasswordshouldshowerror() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        assertEquals("Please fill in all fields", viewModel.errorMessage.value)
        assertFalse(viewModel.loginSuccess.value)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun loginwithvalidcredentialsshouldsucceed() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        coEvery { authRepository.login("test@example.com", "password123") } returns true

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.loginSuccess.value)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
        coVerify { authRepository.login("test@example.com", "password123") }
    }

    @Test
    fun loginwithinvalidcredentialsshouldshowerror() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("wrongpassword")
        coEvery { authRepository.login(any(), any()) } returns false

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.loginSuccess.value)
        assertEquals("Invalid email or password", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loginwithexceptionshouldshowerrormessage() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        coEvery { authRepository.login(any(), any()) } throws Exception("Network error")

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.loginSuccess.value)
        assertEquals("Network error", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loginshouldsetloadingstatecorrectly() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        coEvery { authRepository.login(any(), any()) } coAnswers {
            delay(100)
            true
        }

        // When
        viewModel.login()

        // Then - loading should be true immediately
        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        // Then - loading should be false after completion
        assertFalse(viewModel.isLoading.value)
    }
}