package com.example.myapplication.prestador.ui.pantallas.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.prestador.ui.premium.components.BotonGoEliteTactico
import com.example.myapplication.prestador.ui.premium.components.EtiquetaEliteMaestra
import com.example.myapplication.prestador.viewmodel.profile.ArmadorPrestadorPremiumViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import com.example.myapplication.uishared.ui.components.profile.PrestadorPerfilScreen

/**
 * --- PANTALLA DE PERFIL DEEP (APP PRESTADOR v2026.ELITE) ---
 * [PROPÓSITO]: Implementar la interfaz de gestión profunda del prestador.
 * [LEY #10]: Rompecabezas. Consume el componente universal de ui-shared.
 */
@Composable
fun PerfilPrestadorScreen(
    alVolver: () -> Unit,
    alCerrarSesion: () -> Unit,
    onConfig: () -> Unit,
    alHorarios: (String) -> Unit,
    onNavigateToPaywall: () -> Unit,
    viewModel: PerfilPrestadorDeepViewModel = hiltViewModel(),
    premiumViewModel: ArmadorPrestadorPremiumViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.ecosistema?.let { deep ->
        val identidadesMapped = deep.aModelosUi()
        val identidadPrincipal = identidadesMapped.firstOrNull() ?: return@let

        PrestadorPerfilScreen(
            identidadPrincipal = identidadPrincipal,
            identidadesHijas = identidadesMapped.drop(1),
            todasLasCategorias = state.todasLasCategorias,
            esMiPropioPerfil = true,
            estaCargando = state.estaCargando,
            hayCambiosPendientes = state.hayCambiosPendientes,
            alVolver = alVolver,
            alActualizar = { viewModel.refrescarDatos() },
            alCerrarSesion = { viewModel.cerrarSesion(); alCerrarSesion() },
            alCambiarFoto = { viewModel.actualizarFoto(it) },
            alEliminarIdentidad = { id, tipo -> 
                if (tipo == "EMPRESA") viewModel.eliminarEmpresa(id)
                else if (tipo == "SUCURSAL") viewModel.eliminarSucursal(id)
            },
            alGuardarCambios = { viewModel.guardarCambiosLocales(it) },
            alActualizarDireccion = { viewModel.actualizarDireccion(it) },
            alEliminarDireccion = { viewModel.eliminarDireccion(it) },
            alAnadirEmpresa = { viewModel.anadirEmpresa(it) },
            alAnadirSucursal = { idEmpresa, sucursal, direccion -> viewModel.anadirSucursal(idEmpresa, sucursal, direccion) },
            alConfigurarHorarios = alHorarios,
            estaDetectandoGps = state.estaDetectandoGps,
            alDetectarGps = { viewModel.detectarGps() },
            alSyncCloud = { viewModel.sincronizarEcosistemaCloud() },
            onNavigateToConfig = onConfig,
            distintivoPremium = {
                if (identidadPrincipal.estaSuscrito) {
                    EtiquetaEliteMaestra()
                } else {
                    BotonGoEliteTactico(onClick = onNavigateToPaywall)
                }
            }
        )
    }
}
