package com.example.myapplication.ui.componentes.sistema.contexto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.state.ToggleableState
import coil.compose.AsyncImage

import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorUbicacion
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.utilidades.formatearTexto
import com.example.myapplication.ui.componentes.sistema.menu.v3.MenuUbicacionV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.MoldeMenuArmadorV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.MenuSectionHeaderV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.MenuItemEliteV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.MenuDividerV3
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.uishared.ui.components.*
import com.example.myapplication.uishared.estilos.SharedPalette
import kotlin.let

/**
 * --- 🧩 MOLDE TARJETA PERFIL Y DIRECCIÓN (v2026.ELITE REFACTORED) ---
 * Cabecera HUD optimizada para legibilidad, usabilidad tactil y diseño contemporáneo.
 */
@Composable
fun MoldeTarjetaPerfilDirec(
    modifier: Modifier = Modifier,
    usuario: CuentaMaestroUsuario?,
    nombrePerfilActivo: String,
    fotoPerfilActivo: Any?,
    direccionActiva: DireccionDominio?,
    estaGpsActivo: Boolean,
    isCargandoUbicacion: Boolean = false, 
    alHacerClickPerfil: () -> Unit,
    alHacerClickUbicacion: () -> Unit,
    alAlternarGps: () -> Unit,
    alSeleccionarDireccion: (DireccionDominio) -> Unit = {},
    alSeleccionarPerfil: (String, String?) -> Unit = { _, _ -> },
    estaSeleccionado: Boolean = true,
    mostrarMenuPerfil: Boolean = false,
    mostrarMenuUbicacion: Boolean = false,
    alOcultarMenu: () -> Unit = {},
    esBusquedaManual: Boolean = false
) {
    // Determinar si es perfil personal o empresa
    val esPerfilPersonal = nombrePerfilActivo == usuario?.usuario?.perfil?.nombreVisible

    // 🔥 [ELITE]: Recuperar metadatos del perfil activo para la burbuja
    val perfilData = remember(usuario, nombrePerfilActivo) {
        usuario?.aModelosUi()?.find { it.titulo == nombrePerfilActivo }
    }

    BloquearEscalaFuente(minFontScale = 0.9f, maxFontScale = 1.1f) {
        Surface(
            color = SharedPalette.SurfaceDark,
            shape = CutCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            border = BorderStroke(
                1.dp,
                if (estaSeleccionado) SharedPalette.BorderCyanSoft else Color.White.copy(alpha = 0.08f)
            ),
            shadowElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) 
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // =========================================================================
                // SECCIÓN IZQUIERDA: PERFIL (Refactored con MoldeBurbujaPerfilV3)
                // =========================================================================
                Box(modifier = Modifier.weight(1.3f)) { 
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { alHacerClickPerfil() }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MoldeBurbujaPerfilV3(
                            perfil = PerfilIdentidadV3(
                                id = perfilData?.id ?: "unknown",
                                nombre = nombrePerfilActivo,
                                iniciales = nombrePerfilActivo.take(2).uppercase(),
                                photoUrl = fotoPerfilActivo,
                                estaEnLinea = true, 
                                estaVerificado = perfilData?.estaVerificado ?: false,
                                esSuscripto = perfilData?.estaSuscrito ?: false
                            ),
                            tamanoBase = 42.dp,
                            mostrarBadges = true
                        )

                        Spacer(modifier = Modifier.width(12.dp)) 

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Badge tipo de perfil
                            Surface(
                                color = if (esPerfilPersonal) SharedPalette.ElectricCyan.copy(alpha = 0.12f)
                                else SharedPalette.AccentIndigo.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(2.dp),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (esPerfilPersonal) SharedPalette.ElectricCyan.copy(alpha = 0.3f)
                                    else SharedPalette.AccentIndigo.copy(alpha = 0.4f)
                                )
                            ) {
                                TextCompacto(
                                    text = if (esPerfilPersonal) "PERSONAL" else "EMPRESA",
                                    color = if (esPerfilPersonal) SharedPalette.ElectricCyan else Color(0xFFC084FC),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                TextCompactoAutoFit(
                                    text = nombrePerfilActivo.uppercase(),
                                    color = Color.White,
                                    maxFontSize = 12.sp,
                                    minFontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SharedPalette.TextSubtle,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // MENÚ DESPLEGABLE DE PERFIL
                    MoldeMenuArmadorV3(
                        expanded = mostrarMenuPerfil,
                        onDismissRequest = alOcultarMenu,
                        alignment = Alignment.BottomCenter, 
                        isCenteredOnScreen = true, 
                        verticalOffset = (-10).dp 
                    ) {
                        MenuSectionHeaderV3("CAMBIAR PERFIL")

                        usuario?.let { u ->
                            MenuItemEliteV3(
                                label = u.usuario.perfil.nombreVisible.formatearTexto(),
                                isSelected = estaSeleccionado && nombrePerfilActivo == u.usuario.perfil.nombreVisible,
                                onClick = {
                                    alSeleccionarPerfil(u.usuario.perfil.id, null)
                                    alOcultarMenu()
                                }
                            )
                        }

                        if (usuario != null && usuario.empresas.isNotEmpty()) {
                            usuario.empresas.forEach { company ->
                                company.sucursales.forEach { branch ->
                                    MenuItemEliteV3(
                                        label = "${branch.sucursal.nombre} (${company.empresa.nombre})".formatearTexto(),
                                        isSelected = estaSeleccionado && nombrePerfilActivo.contains(branch.sucursal.nombre),
                                        onClick = {
                                            alSeleccionarPerfil(company.empresa.id, branch.sucursal.id)
                                            alOcultarMenu()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // DIVISOR VERTICAL ELEGANTE
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // =========================================================================
                // SECCIÓN DERECHA: UBICACIÓN (Refactored con MoldeCabeceraSuperiorUbicacion)
                // =========================================================================
                Box(modifier = Modifier.weight(1.1f)) {
                    MoldeCabeceraSuperiorUbicacion(
                        direccion = direccionActiva,
                        onClick = alHacerClickUbicacion
                    )

                    // MENÚ DESPLEGABLE DE UBICACIÓN
                    val direccionesPerfilActual = remember(usuario, nombrePerfilActivo) {
                        if (nombrePerfilActivo == usuario?.usuario?.perfil?.nombreVisible) {
                            usuario.usuario.direcciones
                        } else {
                            usuario?.empresas?.flatMap { it.sucursales }
                                ?.find { nombrePerfilActivo.contains(it.sucursal.nombre) }
                                ?.let { it.direccion?.let { d -> listOf(d) } } ?: emptyList()
                        }
                    }

                    MenuUbicacionV3(
                        expanded = mostrarMenuUbicacion,
                        onDismissRequest = alOcultarMenu,
                        direccionActiva = direccionActiva,
                        direccionGpsActual = null, 
                        estaGpsActivo = estaGpsActivo,
                        isCargando = isCargandoUbicacion, 
                        direccionesDisponibles = direccionesPerfilActual,
                        alAlternarGps = alAlternarGps,
                        alSeleccionarDireccion = alSeleccionarDireccion,
                        alignment = Alignment.BottomCenter, 
                        isCenteredOnScreen = true, 
                        verticalOffset = (-10).dp 
                    )
                }
            }
        }
    }
}

// ==========================================================================================
// --- 🧪 SECCIÓN DE PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF060709)
@Composable
fun PreviewTarjetaPerfilDirecModern() {
    ClienteTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Estado 1: GPS Activo
            MoldeTarjetaPerfilDirec(
                usuario = null,
                nombrePerfilActivo = "Maxi Maverick",
                fotoPerfilActivo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
                direccionActiva = DireccionDominio(
                    id = "addr1",
                    calle = "Av. Aconquija",
                    numero = "1500",
                    localidad = "Yerba Buena",
                    codigoPostal = "T4107",
                    etiqueta = "Casa"
                ),
                estaGpsActivo = true,
                alHacerClickPerfil = {},
                alHacerClickUbicacion = {},
                alAlternarGps = {}
            )

            // Estado 2: Perfil Empresa / GPS Inactivo
            MoldeTarjetaPerfilDirec(
                usuario = null,
                nombrePerfilActivo = "Maverick Tech SRL",
                fotoPerfilActivo = null,
                direccionActiva = DireccionDominio(
                    id = "addr2",
                    calle = "San Martín",
                    numero = "650",
                    localidad = "Centro",
                    codigoPostal = "T4000",
                    etiqueta = "Oficina"
                ),
                estaGpsActivo = false,
                alHacerClickPerfil = {},
                alHacerClickUbicacion = {},
                alAlternarGps = {}
            )
        }
    }
}
