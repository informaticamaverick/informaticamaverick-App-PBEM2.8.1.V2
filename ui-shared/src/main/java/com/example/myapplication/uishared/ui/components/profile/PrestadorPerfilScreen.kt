package com.example.myapplication.uishared.ui.components.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.uishared.ui.components.profile.parts.*

/**
 * --- PANTALLA DE PERFIL DEL PRESTADOR (ELITE v2026.FINAL) ---
 * [PROPÓSITO]: Orquestador de la pantalla de perfil. Une datos con el lienzo.
 * Implementa la Ley #10 (Rompecabezas).
 */
@Composable
fun PrestadorPerfilScreen(
    identidadPrincipal: PrestadorDominio,
    identidadesHijas: List<PrestadorDominio> = emptyList(),
    todasLasCategorias: List<CategoriaDominio> = emptyList(),
    esMiPropioPerfil: Boolean = false,
    estaCargando: Boolean = false,
    hayCambiosPendientes: Boolean = false,
    mostrarCheckGuardado: Boolean = false,

    alVolver: () -> Unit = {},
    alActualizar: () -> Unit = {},
    alCerrarSesion: () -> Unit = {},
    alCambiarFoto: (Uri) -> Unit = {},
    alEliminarIdentidad: (String, String) -> Unit = { _, _ -> },
    alGuardarCambios: (PrestadorDominio) -> Unit = {},
    alActualizarDireccion: (DireccionDominio) -> Unit = {},
    alEliminarDireccion: (DireccionDominio) -> Unit = {},
    alAnadirEmpresa: (Triple<EmpresaDominio, SucursalDominio, DireccionDominio>) -> Unit = { },
    alAnadirSucursal: (String, SucursalDominio, DireccionDominio) -> Unit = { _, _, _ -> },
    alConfigurarHorarios: (String) -> Unit = {},
    estaDetectandoGps: Boolean = false,
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {},
    alSyncCloud: () -> Unit = {},
    onNavigateToConfig: () -> Unit = {},
    alChat: (String) -> Unit = {},
    distintivoPremium: @Composable () -> Unit = {}
) {
    var mostrarConfirmacionSalida by remember { mutableStateOf(false) }

    BackHandler(enabled = hayCambiosPendientes) {
        mostrarConfirmacionSalida = true
    }

    if (mostrarConfirmacionSalida) {
        DialogoSalidaSegura(
            onGuardar = {
                mostrarConfirmacionSalida = false
                alSyncCloud()
                alVolver()
            },
            onDescartar = {
                mostrarConfirmacionSalida = false
                alVolver()
            },
            onCancelar = {
                mostrarConfirmacionSalida = false
            }
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { alCambiarFoto(it) }
    }

    PrestadorPerfilLienzo(
        identidadPrincipal = identidadPrincipal,
        identidadesHijas = identidadesHijas,
        todasLasCategorias = todasLasCategorias,
        esMiPropioPerfil = esMiPropioPerfil,
        estaCargando = estaCargando,
        mostrarCheckGuardado = mostrarCheckGuardado,
        alVolver = {
            if (hayCambiosPendientes) mostrarConfirmacionSalida = true
            else alVolver()
        },
        alActualizar = alActualizar,
        alGuardarCambios = alGuardarCambios,
        alCerrarSesion = alCerrarSesion,
        alEditarAvatar = { imagePickerLauncher.launch("image/*") },
        alEliminarIdentidad = alEliminarIdentidad,
        alActualizarDireccion = alActualizarDireccion,
        alEliminarDireccion = alEliminarDireccion,
        alAnadirEmpresa = alAnadirEmpresa,
        alAnadirSucursal = alAnadirSucursal,
        alConfigurarHorarios = alConfigurarHorarios,
        estaDetectandoGps = estaDetectandoGps,
        alDetectarGps = alDetectarGps,
        alNavegarAConfiguracion = onNavigateToConfig,
        alChat = { id -> id?.let { alChat(it) } },
        hayCambiosPendientes = hayCambiosPendientes,
        alSyncCloud = alSyncCloud,
        distintivoPremium = distintivoPremium
    )
}

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun PreviewScreenCompletaMav() {
    val m = PrestadorPerfilMocks.elenaRodriguez
    PrestadorPerfilScreen(
        identidadPrincipal = m,
        esMiPropioPerfil = true
    )
}

































