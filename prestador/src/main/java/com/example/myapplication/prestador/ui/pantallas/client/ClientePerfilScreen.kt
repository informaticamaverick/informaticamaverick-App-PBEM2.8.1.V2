package com.example.myapplication.prestador.ui.pantallas.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.viewmodel.cliente.ClientePerfilViewModel
import com.example.myapplication.uishared.ui.components.profile.UsuarioPerfilScreen
import com.example.myapplication.core.dominio.mapeadores.UsuarioMappers
import com.example.myapplication.core.dominio.modelos.UsuarioDominio

/**
 * --- PANTALLA DE PERFIL DEL CLIENTE (VISTA PRESTADOR) ---
 * Visualiza datos del cliente bajo el protocolo Maverick Elite.
 */
@Composable
fun ClientePerfilScreen(
    onBack: () -> Unit = {},
    viewModel: ClientePerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.identidad == null && uiState.isLoading) {
        com.example.myapplication.uishared.ui.components.profile.parts.ShimmerPerfilElite()
    } else if (uiState.identidad == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(uiState.error ?: "Perfil no disponible", color = Color.Gray)
        }
    } else {
        val identidad = uiState.identidad!!
        val uiModel = UsuarioMappers.deEntidadAPrestadorUi(identidad)
        
        UsuarioPerfilScreen(
            usuario = uiModel,
            todasLasIdentidades = listOf(uiModel), // Por ahora solo la personal
            esMiPropioPerfil = false, // Vista desde el prestador (Lectura estricta)
            estaCargando = uiState.isLoading,
            alVolver = onBack,
            alActualizar = { viewModel.refreshProfile() }
        )
    }
}















































