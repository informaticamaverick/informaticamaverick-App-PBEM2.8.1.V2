package com.example.myapplication.prestador

import androidx.activity.ComponentActivity
//import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.prestador.ui.dashboard.PrestadorBottomNavigationBar
import com.example.myapplication.prestador.ui.dashboard.components.InicioScreen
//import com.example.myapplication.prestador.viewmodel.DashboardUiState
import com.example.myapplication.prestador.viewmodel.dashboard.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrestadorDashboardScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun inicioScreenShowsGreetingForProvider() {
        composeRule.setContent {
            InicioScreen(
                state = DashboardUiState(
                    saludo = "Buenas tardes",
                    nombrePrestador = "Ana Torres",
                    gananciasSemanales = 12500.0,
                    serviceType = "TECHNICAL"
                ),
                onNavigateToEditProfile = {},
                onNavigateToServiceConfig = {},
                onLogout = {},
                onNavigateToCalendar = {},
                onNavigateToPresupuesto = {},
                onNavigateToPresupuestos = {},
                onNavigateToChat = { _ -> }
            )
        }

        composeRule.onNodeWithText("Buenas tardes, Ana 👋").assertExists()
    }

    @Test
    fun bottomNavigationUsesChatActionAndShowsBadges() {
        var selectedTab = -1

        composeRule.setContent {
            PrestadorBottomNavigationBar(
                selectedTab = 2,
                unreadCount = 3,
                unreadMessageCount = 2,
                onTabSelected = { selectedTab = it }
            )
        }

        composeRule.onNodeWithText("Alertas").assertExists()
        composeRule.onNodeWithText("2").assertExists()
        composeRule.onNodeWithContentDescription("Chat").performClick()

        composeRule.runOnIdle {
            assertEquals(3, selectedTab)
        }
    }
}
