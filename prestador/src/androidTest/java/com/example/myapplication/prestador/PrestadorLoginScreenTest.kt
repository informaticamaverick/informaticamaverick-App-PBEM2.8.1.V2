package com.example.myapplication.prestador

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.prestador.ui.login.LoginState
import com.example.myapplication.prestador.ui.login.PrestadorLoginScreen
import com.example.myapplication.prestador.ui.login.PrestadorLoginViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrestadorLoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    private val hasProfile = MutableStateFlow(false)
    private val passwordResetEmailSent = MutableStateFlow(false)
    private lateinit var viewModel: PrestadorLoginViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.loginState } returns loginState
        every { viewModel.hasProfile } returns hasProfile
        every { viewModel.passwordResetEmailSent } returns passwordResetEmailSent
    }

    @Test
    fun clickingLoginWithEmptyFieldsShowsValidationMessage() {
        composeRule.setContent {
            PrestadorLoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithText("Iniciar Sesión").performClick()

        composeRule.onNodeWithText("Por favor completa todos los campos").assertExists()
        verify(exactly = 0) { viewModel.login(any(), any()) }
    }

    @Test
    fun clickingRegisterInvokesNavigationCallback() {
        var navigated = false

        composeRule.setContent {
            PrestadorLoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { navigated = true },
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithText("Registrate sin Google").performClick()

        composeRule.runOnIdle {
            assertTrue(navigated)
        }
    }
}
