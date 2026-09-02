package com.example.myapplication.uishared.ui.components.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    alEliminarFoto: () -> Unit = {},
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
    alGuardarYSalir: (alTerminar: () -> Unit) -> Unit = { it() },
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
                // [ELITE]: espera a que la sincronización termine antes de navegar —
                // salir de la pantalla antes de tiempo cancelaba la subida a Firestore
                // a mitad de camino (viewModelScope se cancela junto al ViewModel).
                alGuardarYSalir(alVolver)
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

    var mostrarMenuFoto by remember { mutableStateOf(false) }

    if (mostrarMenuFoto) {
        MenuFotoPerfilMav(
            tieneFoto = identidadPrincipal.urlFoto != null || identidadPrincipal.urlMiniatura != null,
            onCambiar = {
                mostrarMenuFoto = false
                imagePickerLauncher.launch("image/*")
            },
            onEliminar = {
                mostrarMenuFoto = false
                alEliminarFoto()
            },
            onDismiss = { mostrarMenuFoto = false }
        )
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
        alEditarAvatar = { mostrarMenuFoto = true },
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

@Composable
private fun MenuFotoPerfilMav(
    tieneFoto: Boolean,
    onCambiar: () -> Unit,
    onEliminar: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorAcento = Color(0xFFFF7043)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto de perfil", fontWeight = FontWeight.Black, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCambiar)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(if (tieneFoto) "Cambiar foto" else "Agregar foto", color = Color.White, fontSize = 15.sp)
                }
                if (tieneFoto) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onEliminar)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(14.dp))
                        Text("Eliminar foto", color = Color.Red.copy(alpha = 0.8f), fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1A24),
        shape = RoundedCornerShape(24.dp)
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

































