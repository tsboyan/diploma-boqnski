package com.tumba.bhaga


import com.tumba.bhaga.data.local.dao.UserDao
import com.tumba.bhaga.data.local.entity.UserEntity
import com.tumba.bhaga.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AuthRepositoryTest {

    private lateinit var repository: AuthRepository
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        userDao = mockk()
        repository = AuthRepository(userDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun signUpwithnewemailshouldcreateuserandreturntrue() = runTest {
        // Given
        val name = "Test User"
        val email = "test@example.com"
        val password = "password123"
        val userId = 1L

        coEvery { userDao.emailExists(email) } returns false
        coEvery { userDao.insertUser(any()) } returns userId

        // When
        val result = repository.signUp(name, email, password)

        // Then
        assertTrue(result)
        coVerify { userDao.emailExists(email) }
        coVerify { userDao.insertUser(any()) }
        assertEquals(userId, repository.getCurrentUserId())
    }

    @Test
    fun signUpwithexistingemailshouldthrowexception() = runTest {
        // Given
        val email = "existing@example.com"
        coEvery { userDao.emailExists(email) } returns true

        // When/Then
        try {
            repository.signUp("Test", email, "password")
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals("Email already exists", e.message)
        }

        coVerify { userDao.emailExists(email) }
        coVerify(exactly = 0) { userDao.insertUser(any()) }
    }

    @Test
    fun loginwithvalidcredentialsshouldreturntrue() = runTest {
        // Given
        val email = "user@example.com"
        val password = "password123"
        val user = UserEntity(
            id = 1L,
            name = "Test User",
            email = email,
            password = password
        )

        coEvery { userDao.login(email, password) } returns user

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result)
        assertEquals(user.id, repository.getCurrentUserId())
    }

    @Test
    fun loginwithinvalidcredentialsshouldreturnfalse() = runTest {
        // Given
        val email = "user@example.com"
        val password = "wrongpassword"

        coEvery { userDao.login(email, password) } returns null

        // When
        val result = repository.login(email, password)

        // Then
        assertFalse(result)
        assertNull(repository.getCurrentUserId())
    }

    @Test
    fun isCurrentUserAdminshouldreturntrueforadminemail() = runTest {
        // Given
        val adminUser = UserEntity(
            id = 1L,
            name = "Admin",
            email = "admin@bhaga.com",
            password = "admin123"
        )

        coEvery { userDao.login("admin@bhaga.com", "admin123") } returns adminUser
        coEvery { userDao.getUserById(1L) } returns adminUser

        repository.login("admin@bhaga.com", "admin123")

        // When
        val result = repository.isCurrentUserAdmin()

        // Then
        assertTrue(result)
    }

    @Test
    fun isCurrentUserAdminshouldreturnfalseforregularuser() = runTest {
        // Given
        val regularUser = UserEntity(
            id = 1L,
            name = "Regular User",
            email = "user@example.com",
            password = "password"
        )

        coEvery { userDao.login("user@example.com", "password") } returns regularUser
        coEvery { userDao.getUserById(1L) } returns regularUser

        repository.login("user@example.com", "password")

        // When
        val result = repository.isCurrentUserAdmin()

        // Then
        assertFalse(result)
    }

    @Test
    fun logoutshouldclearcurrentuser() = runTest {
        // Given
        val user = UserEntity(1L, "Test", "test@example.com", "password")
        coEvery { userDao.login(any(), any()) } returns user
        repository.login("test@example.com", "password")

        // When
        repository.logout()

        // Then
        assertNull(repository.getCurrentUserId())
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun getCurrentUsershouldreturnuserwhenloggedin() = runTest {
        // Given
        val user = UserEntity(1L, "Test", "test@example.com", "password")
        coEvery { userDao.login(any(), any()) } returns user
        coEvery { userDao.getUserById(1L) } returns user

        repository.login("test@example.com", "password")

        // When
        val result = repository.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals(user, result)
    }

    @Test
    fun getCurrentUsershouldreturnnullwhennotloggedin() = runTest {
        // When
        val result = repository.getCurrentUser()

        // Then
        assertNull(result)
    }
}