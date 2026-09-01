package com.example.myapplication.uishared.ui.components.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.uishared.ui.components.profile.parts.UsuarioPerfilLienzo

/**
 * --- PANTALLA DE PERFIL DE USUARIO (ELITE v2026) ---
 * [PROPÓSITO]: Orquestador dedicado para identidades de tipo Cliente.
 * Sigue la Ley #10 (Rompecabezas).
 */
@Composable
fun UsuarioPerfilScreen(
    usuario: PrestadorDominio, 
    todasLasIdentidades: List<PrestadorDominio> = emptyList(),
    todasLasCategorias: List<CategoriaDominio> = emptyList(),
    esMiPropioPerfil: Boolean = false,
    estaCargando: Boolean = false,
    estaDetectandoGps: Boolean = false,
    enModoEdicion: Boolean = false,
    hayCambiosPendientes: Boolean = false,
    
    alVolver: () -> Unit = {},
    alActualizar: () -> Unit = {},
    alSyncCloud: () -> Unit = {},
    alCerrarSesion: () -> Unit = {},
    alCambiarFoto: (Uri) -> Unit = {},
    alGuardarCambios: (PrestadorDominio) -> Unit = {},
    alNavegarAConfiguracion: () -> Unit = {},
    alActualizarDireccion: (DireccionDominio) -> Unit = {},
    alEliminarDireccion: (DireccionDominio) -> Unit = {},
    alAnadirEmpresa: (Triple<EmpresaDominio, SucursalDominio, DireccionDominio>) -> Unit = { },
    alAnadirSucursal: (String, SucursalDominio, DireccionDominio) -> Unit = { _, _, _ -> },
    alEliminarIdentidad: (String, String) -> Unit = { _, _ -> },
    alChat: () -> Unit = {},
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {}
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { alCambiarFoto(it) }
    }

    UsuarioPerfilLienzo(
        identidadPrincipal = usuario,
        identidadesHijas = todasLasIdentidades,
        todasLasCategorias = todasLasCategorias,
        esMiPropioPerfil = esMiPropioPerfil,
        estaCargando = estaCargando,
        enModoEdicion = enModoEdicion,
        hayCambiosPendientes = hayCambiosPendientes,
        alVolver = alVolver,
        alActualizar = alActualizar,
        alSyncCloud = alSyncCloud,
        alCerrarSesion = alCerrarSesion,
        alEditarAvatar = { imagePickerLauncher.launch("image/*") },
        alChat = alChat,
        alGuardarCambios = alGuardarCambios,
        alNavegarAConfiguracion = alNavegarAConfiguracion,
        alActualizarDireccion = alActualizarDireccion,
        alEliminarDireccion = alEliminarDireccion,
        alAnadirEmpresa = alAnadirEmpresa,
        alAnadirSucursal = alAnadirSucursal,
        alEliminarIdentidad = alEliminarIdentidad,
        estaDetectandoGps = estaDetectandoGps,
        alDetectarGps = alDetectarGps
    )
}

































