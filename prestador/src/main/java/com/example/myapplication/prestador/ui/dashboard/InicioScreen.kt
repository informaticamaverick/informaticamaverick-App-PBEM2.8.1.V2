package com.example.myapplication.prestador.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.viewmodel.dashboard.DashboardViewModel
import com.example.myapplication.prestador.ui.dashboard.components.*

@Composable
fun InicioContent(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCreatePromo: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToThemeDemo: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToPresupuesto: () -> Unit = {},
    onNavigateToCrearPresupuesto: () -> Unit = {},
    onNavigateToPresupuestos: () -> Unit = {},
    onNavigateToChat: (clientId: String) -> Unit = {},
    onCrearTurno: () -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToCalendarioConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    InicioScreen(
        state = state,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToServiceConfig = onNavigateToServiceConfig,
        onLogout = onLogout,
        onNavigateToCalendar = onNavigateToCalendar,
        onCrearTurno = onCrearTurno,
        onNavigateToPresupuesto = onNavigateToPresupuesto,
        onNavigateToCrearPresupuesto = onNavigateToCrearPresupuesto,
        onNavigateToPresupuestos = onNavigateToPresupuestos,
        onNavigateToChat = onNavigateToChat,
        onCompletarCita = { _, _ -> },
        onCompletarTrabajoFast = { _, _ -> },
        onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
        onNavigateToCalendarioConfig = onNavigateToCalendarioConfig,
        onNavigateToApariencia = onNavigateToApariencia,
        onNavigateToNotificaciones = onNavigateToNotificaciones,
        onNavigateToTerminos = onNavigateToTerminos,
        onNavigateToPrivacidad = onNavigateToPrivacidad,
        onNavigateToAcercaDe = onNavigateToAcercaDe,

    )
}
