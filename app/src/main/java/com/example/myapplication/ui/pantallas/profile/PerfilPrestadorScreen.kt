package com.example.myapplication.ui.pantallas.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.example.myapplication.coordinadores.BeCerebroViewModel
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.uishared.ui.components.profile.PrestadorPerfilScreen
import com.example.myapplication.viewmodel.profile.ArmadorPerfilPrestadorViewModel

/**
 * --- PANTALLA DE PERFIL DEL PRESTADOR (VISTA CLIENTE) ---
 * Visualiza identidades remotas bajo el protocolo Maverick Elite.
 * [ACTUALIZADO]: Soporta navegación multi-sucursal via Pager Shared.
 */
@Composable
fun PerfilPrestadorScreen(
    providerId: String,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: ArmadorPerfilPrestadorViewModel = hiltViewModel(),
    BeCerebroViewModel: BeCerebroViewModel = hiltViewModel()
) {
    val identidadPrincipal by viewModel.perfilPrestador.collectAsStateWithLifecycle()
    val vinculadas = emptyList<com.example.myapplication.core.dominio.modelos.PrestadorDominio>() // Fase 2 vinculadas
    val estaCargando by viewModel.estaCargando.collectAsStateWithLifecycle()
    val todasLasCategorias by viewModel.todasLasCategorias.collectAsStateWithLifecycle()

    val beConfig = remember { ContextoHUD.PERFIL_PRESTADOR.crearConfiguracionBase() }
    
    DisposableEffect(providerId) {
        viewModel.cargarPerfil(providerId)
        BeCerebroViewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose {
            BeCerebroViewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    if (identidadPrincipal == null && estaCargando) {
        com.example.myapplication.uishared.ui.components.profile.parts.ShimmerPerfilElite()
    } else if (identidadPrincipal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Perfil no disponible", color = Color.Gray)
        }
    } else {
        PrestadorPerfilScreen(
            identidadPrincipal = identidadPrincipal!!,
            identidadesHijas = vinculadas,
            todasLasCategorias = todasLasCategorias,
            esMiPropioPerfil = false,
            estaCargando = estaCargando,
            alVolver = onBack,
            alActualizar = { viewModel.cargarPerfil(providerId) },
            alChat = { id -> onNavigateToChat(id) }
        )
    }
}


































