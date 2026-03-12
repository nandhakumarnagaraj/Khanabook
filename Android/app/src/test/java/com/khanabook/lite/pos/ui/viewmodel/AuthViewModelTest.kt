package com.khanabook.lite.pos.ui.viewmodel


import android.util.Log
import com.khanabook.lite.pos.data.local.entity.UserEntity
import com.khanabook.lite.pos.data.remote.WhatsAppApiService
import com.khanabook.lite.pos.data.repository.RestaurantRepository
import com.khanabook.lite.pos.data.repository.UserRepository
import com.khanabook.lite.pos.domain.manager.AuthManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val whatsAppApiService: WhatsAppApiService = mockk(relaxed = true)
    private val restaurantRepository: RestaurantRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    init {
        // Need to set up state flows for the mocked repository
        val currentUserFlow = MutableStateFlow<UserEntity?>(null)
        every { userRepository.currentUser } returns currentUserFlow
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(AuthManager)
        io.mockk.mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        viewModel = AuthViewModel(userRepository, whatsAppApiService, restaurantRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `login returns Success via local fallback when remote login fails and credentials are correct`() = runTest {
        // Given
        val email = "9150677849"
        val password = "owner123"
        val hashedPassword = "\$2a\$12\$SomeBcryptHash.Fake" 
        
        val fakeUser = UserEntity(
            id = 1,
            name = "Owner",
            email = email,
            passwordHash = hashedPassword,
            role = "admin",
            restaurantId = 1,
            deviceId = "device",
            isActive = true,
            isSynced = false,
            createdAt = "2026-03-10"
        )

        // Mock Remote failure
        coEvery { userRepository.remoteLogin(email, password) } returns Result.failure(Exception("Network Error"))

        // Mock Local fetch success
        coEvery { userRepository.getUserByEmail(email) } returns fakeUser
        
        // Mock AuthManager password verification
        every { AuthManager.verifyPassword(password, hashedPassword) } returns true

        // When
        viewModel.login(email, password)
        advanceUntilIdle() // Process all coroutines
        
        // Then
        val result = viewModel.loginStatus.value
        println("TEST RESULT: $result")
        assertTrue("Expected login to succeed via local fallback", result is AuthViewModel.LoginResult.Success)
        
        // Verify user repository update called
        coVerify(exactly = 1) { userRepository.setCurrentUser(fakeUser) }
        
        val successResult = result as AuthViewModel.LoginResult.Success
        assertEquals(fakeUser.email, successResult.user.email)
    }
    
    @Test
    fun `login returns Error when remote fails and local credentials do not match`() = runTest {
        // Given
        val email = "9150677849"
        val wrongPassword = "wrongPassword123"
        val hashedPassword = "\$2a\$12\$SomeBcryptHash.Fake" 
        
        val fakeUser = UserEntity(
            id = 1,
            name = "Owner",
            email = email,
            passwordHash = hashedPassword,
            role = "admin",
            restaurantId = 1,
            deviceId = "device",
            isActive = true,
            isSynced = false,
            createdAt = "2026-03-10"
        )

        // Mock Remote failure
        coEvery { userRepository.remoteLogin(email, wrongPassword) } returns Result.failure(Exception("Network Error"))

        // Mock Local fetch success
        coEvery { userRepository.getUserByEmail(email) } returns fakeUser
        
        // Mock AuthManager password verification
        every { AuthManager.verifyPassword(wrongPassword, hashedPassword) } returns false

        // When
        viewModel.login(email, wrongPassword)
        advanceUntilIdle() // Process all coroutines
        
        // Then
        val result = viewModel.loginStatus.value
        assertTrue("Expected login to fail", result is AuthViewModel.LoginResult.Error)
        
        val errorResult = result as AuthViewModel.LoginResult.Error
        assertTrue(errorResult.message.contains("Login failed"))
    }
}
