package com.example.myapplication.ui.pantallas.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.dominio.mapeadores.UsuarioMappers
import com.example.myapplication.viewmodel.home.UbicacionGpsObrero
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.uishared.ui.components.profile.UsuarioPerfilScreen
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import kotlinx.coroutines.launch

/**
 * --- PANTALLA DE PERFIL DEL USUARIO (V3) ---
 * Orquesta el motor de identidad Maverick Elite.
 */
@Composable
fun PerfilUsuarioScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ArmadorUsuarioViewModel = hiltViewModel(),
    beViewModel: BeCerebroViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionGpsObrero = hiltViewModel()
) {
    val ecosistema by viewModel.ecosistemaMaestro.collectAsStateWithLifecycle()
    val estaCargando by viewModel.estaCargando.collectAsStateWithLifecycle()
    val hayCambiosPendientes by viewModel.hayCambiosPendientes.collectAsStateWithLifecycle()
    val estaDetectandoGps by ubicacionObrero.estaCargando.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    val beConfig = remember { com.example.myapplication.ui.componentes.be.modelos.ContextoHUD.PERFIL.crearConfiguracionBase() }
    
    DisposableEffect(Unit) {
        // 🔥 [ELITE]: Contrato de Pantalla Soberano mediante Mapa de Registros
        beViewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose {
            beViewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    if (ecosistema == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF3B82F6))
        }
    } else {
        val identidadBase = ecosistema!!.usuario.perfil
        val usuarioUi = UsuarioMappers.deDominioAPrestadorUi(identidadBase, ecosistema!!.cuenta.estaSuscrito)
        
        val todasLasIdentidades = ecosistema!!.aModelosUi()

        UsuarioPerfilScreen(
            usuario = usuarioUi,
            todasLasIdentidades = todasLasIdentidades,
            todasLasCategorias = emptyList(), // 🔥 [ELITE] Cliente solo gestiona direcciones
            esMiPropioPerfil = true,
            estaCargando = estaCargando,
            hayCambiosPendientes = hayCambiosPendientes,
            estaDetectandoGps = estaDetectandoGps,
            alVolver = onNavigateBack,
            alActualizar = { viewModel.refrescarDatos() },
            alSyncCloud = { viewModel.sincronizarPerfil() },
            alCerrarSesion = {
                viewModel.cerrarSesion()
                onLogout()
            },
            alCambiarFoto = {
                viewModel.actualizarFotoPerfil(it)
            },
            alGuardarCambios = { viewModel.guardarCambiosIdentidad(it) }, 
            alActualizarDireccion = { viewModel.actualizarDireccion(it) },
            alEliminarDireccion = { viewModel.eliminarDireccion(it) },
            alAnadirEmpresa = { (e, s, d) -> viewModel.crearEmpresa(e, s, d) },
            alAnadirSucursal = { _, s, d -> viewModel.añadirSucursal(s, d) },
            alEliminarIdentidad = { id, tipo -> if(tipo == "EMPRESA") viewModel.eliminarEmpresa(id) else viewModel.eliminarSucursal(id) },
            alChat = { /* No aplica */ },
            alNavegarAConfiguracion = {
                beViewModel.dispararAccion("settings_profile")
            },
            alDetectarGps = { onResult -> 
                scope.launch {
                    val dir = ubicacionObrero.capturarUbicacionGps()
                    dir?.let { onResult(it) }
                }
            }
        )
    }
}

// Mapeador local eliminado (Centralizado en el Core)

