package com.example.myapplication.prestador.ui.pantallas.dashboard

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.viewmodel.dashboard.PrestadorDashboardViewModel
import com.example.myapplication.prestador.ui.pantallas.dashboard.componentes.*

/**
 * --- ORQUESTADOR DE INICIO (PRESTADOR v2026.8) ---
 * ELITE: Conecta el ViewModel con los componentes visuales de Inicio.
 * LEY #9: Variables en español y segmentación por sectores.
 */
@Composable
fun InicioContent(
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCreatePromo: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onNavigateToThemeDemo: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToCrearPresupuesto: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToConcursos: () -> Unit = {},
    onNavigateToChat: (clientId: String) -> Unit = {},
    onCrearTurno: () -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToHorariosConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
) {
    // --- SECTOR: ESTADO (LEY #7) ---
    val viewModel: PrestadorDashboardViewModel = hiltViewModel()
    val estadoUi by viewModel.uiState.collectAsState()
    val TAG = "InicioContent"
    
    // --- SECTOR: UI ---
    InicioScreen(
        state = estadoUi,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onLogout = onLogout,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToCreatePromo = onNavigateToCreatePromo,
        onNavigateToPromotionList = onNavigateToPromotionList,
        onCrearTurno = onCrearTurno,
        onNavigateToCrearPresupuesto = onNavigateToCrearPresupuesto,
        onNavigateToCatalogo = onNavigateToCatalogo,
        onNavigateToConcursos = onNavigateToConcursos,
        onNavigateToChat = onNavigateToChat,
        onCompletarCita = { id, _ -> Log.d(TAG, "📅 [COMPLETE_APPOINTMENT] ID: $id") },
        onCompletarTrabajoFast = { id, _ -> Log.d(TAG, "⚡ [COMPLETE_FAST] ID: $id") },
        onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
        onNavigateToHorariosConfig = onNavigateToHorariosConfig,
        onNavigateToApariencia = onNavigateToApariencia,
        onNavigateToNotificaciones = onNavigateToNotificaciones,
        onNavigateToTerminos = onNavigateToTerminos,
        onNavigateToPrivacidad = onNavigateToPrivacidad,
        onNavigateToAcercaDe = onNavigateToAcercaDe,
    )
}















































