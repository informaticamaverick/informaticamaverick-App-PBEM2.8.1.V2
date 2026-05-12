package com.example.myapplication.prestador

import com.example.myapplication.prestador.data.repository.AuthRepository
import com.example.myapplication.prestador.ui.login.LoginState
import com.example.myapplication.prestador.ui.login.PrestadorLoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrestadorLoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: PrestadorLoginViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        authRepository = mockk()
        viewModel = PrestadorLoginViewModel(authRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with invalid email emits validation error without hitting repository`() {
        viewModel.login("correo-invalido", "123456")

        val state = viewModel.loginState.value
        assertTrue(state is LoginState.Error)
        assertEquals("Ingresa un email válido", (state as LoginState.Error).message)
        coVerify(exactly = 0) { authRepository.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `login failure exposes repository error`() = runTest {
        coEvery {
            authRepository.signInWithEmailAndPassword("prestador@test.com", "123456")
        } returns Result.failure(IllegalStateException("Credenciales inválidas"))

        viewModel.login("prestador@test.com", "123456")
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertTrue(state is LoginState.Error)
        assertEquals("Credenciales inválidas", (state as LoginState.Error).message)
    }

    @Test
    fun `reset password success marks email as sent and restores idle state`() = runTest {
        coEvery { authRepository.sendPasswordResetEmail("prestador@test.com") } returns Result.success(Unit)

        viewModel.resetPassword("prestador@test.com")
        advanceUntilIdle()

        assertTrue(viewModel.passwordResetEmailSent.value)
        assertEquals(LoginState.Idle, viewModel.loginState.value)
    }

    @Test
    fun `reset password maps too many requests error`() = runTest {
        coEvery { authRepository.sendPasswordResetEmail("prestador@test.com") } returns
            Result.failure(IllegalStateException("too-many-requests"))

        viewModel.resetPassword("prestador@test.com")
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertTrue(state is LoginState.Error)
        assertEquals("Demasiados intentos. Intenta más tarde", (state as LoginState.Error).message)
    }
}
