package com.example.myapplication.ui.pantallas.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.componentes.be.ui.NavegacionHUDAsistente
import com.example.myapplication.ui.componentes.navigation.NavegacionBarV3
import com.example.myapplication.viewmodel.home.*
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel

/**
 * NavegacionCajaPrincipal.kt
 * Propósito: Orquestador global de la UI (Caja).
 * Funcionamiento: Coordina el NavHost, la Barra de Navegación y el HUD del Asistente.
 * Relación: Reemplaza a AppNavigation.kt como punto de entrada principal.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavegacionCajaPrincipal(
    initialTarget: String? = null,
    onLogoutRequest: () -> Unit,
    beViewModel: BeCerebroViewModel = hiltViewModel(),
    beAssistantViewModel: BeCuerpoViewModel = hiltViewModel(),
    navBarViewModel: NavegacionBarViewModel = hiltViewModel(),
    userViewModel: ArmadorUsuarioViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionGpsObrero = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val coordinator = beViewModel.coordinador
    val navCoordinator = beViewModel.navCoordinador // 🔥 [NEW]
    val uiBe by beAssistantViewModel.uiState.collectAsStateWithLifecycle()
    
    val estaHojaVisible by navCoordinator.estaHojaVisible.collectAsStateWithLifecycle()
    val estaBusquedaActiva by beAssistantViewModel.beBusquedaMotor.estaBusquedaActiva.collectAsStateWithLifecycle()
    val estaMenuLateralAbierto by navCoordinator.estaMenuLateralAbierto.collectAsStateWithLifecycle()

    // Manejo de retroceso global (Soberanía Elite)
    BackHandler(enabled = estaHojaVisible || estaBusquedaActiva || estaMenuLateralAbierto) {
        coordinator.ejecutarCierreMaestro()
    }

    // Efecto de navegación inicial (Deep Links)
    LaunchedEffect(initialTarget) {
        if (initialTarget != null) {
            android.util.Log.d("MAV_NAV", "🎯 [INITIAL_TARGET] Procesando: $initialTarget")
            val targetRoute = when (initialTarget) {
                "perfil_cliente" -> Screen.PerfilCliente.route
                "concursos" -> Screen.Concursos.route
                else -> initialTarget // Rutas completas como "chat?..."
            }
            
            if (currentRoute != targetRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(Screen.Home.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    // --- SINCRONIZACIÓN DE RUTA (PASIVA) ---
    // [ELITE]: Ya no decidimos el ContextoHUD aquí. Las pantallas son soberanas.
    LaunchedEffect(currentRoute) {
        navBarViewModel.alCambiarRuta(currentRoute)
    }

    // --- REGLAS DE SOBERANÍA HUD (ELITE v2026) ---
    // [v2026.SUPREME]: La barra inferior obedece directamente al contrato soberano activo y estados de visibilidad global.
    val isBottomBarVisible = uiBe.configuracion.mostrarBarraNavegacion && !estaMenuLateralAbierto && !estaHojaVisible
    
    // 🔥 [AUDITORÍA]: Monitor de Barra de Navegación
    LaunchedEffect(isBottomBarVisible, uiBe.estaBusquedaActiva) {
        android.util.Log.d("MAV_NAV_BAR", "📊 [VISIBILIDAD] Visible=$isBottomBarVisible | Buscando=${uiBe.estaBusquedaActiva} | HUD_ID=${uiBe.configuracion.id}")
    }
    

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible && !uiBe.estaBusquedaActiva,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NavegacionBarV3(
                        navController = navController,
                        currentRoute = currentRoute,
                        viewModel = navBarViewModel
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavegacionLienzoPrincipal(
                navController = navController,
                innerPadding = innerPadding,
                beViewModel = beViewModel,
                beAssistantViewModel = beAssistantViewModel,
                onLogoutRequest = onLogoutRequest
            )
        }

        // --- CAPA HUD (ASISTENTE Y PANELES) ---
        NavegacionHUDAsistente(
            beCuerpoVm = beAssistantViewModel,
            beBusquedaVm = hiltViewModel(),
            beFisicaVm = hiltViewModel()
        )
    }
}
