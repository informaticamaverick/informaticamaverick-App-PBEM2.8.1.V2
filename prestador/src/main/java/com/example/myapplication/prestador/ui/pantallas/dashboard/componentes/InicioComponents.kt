package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.InformacionClima
import com.example.myapplication.prestador.viewmodel.dashboard.EstadoDashboardUi
import com.example.myapplication.prestador.viewmodel.dashboard.WeatherViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.core.dominio.modelos.DireccionDominio

// Paleta de Colores MAV Elite (Estilo Ejecutivo / Industrial)
private object ThemeColors {
    val DarkBg = Color(0xFF030712)
    val CardBg = Color(0xFF0F172A)
    val CardBorder = Color(0xFF334155).copy(alpha = 0.7f)
    val HeaderBg = Color(0xFF020617).copy(alpha = 0.95f)
    
    val BrandOrange = Color(0xFFFF5722)
    val BrandOrangeHover = Color(0xFFF4511E)
    val BrandCyan = Color(0xFF06B6D4)
    val BrandAmber = Color(0xFFF59E0B)
    val BrandEmerald = Color(0xFF10B981)
    val BrandPurple = Color(0xFFA855F7)
    
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}

/**
 * Pantalla Principal de Inicio (Dashboard Prestador v2)
 * Diseñada bajo estándares industriales de esquinas nítidas/rectangulares y diseño ejecutivo.
 */
@Composable
fun InicioScreen(
    state: EstadoDashboardUi = EstadoDashboardUi(),
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToCreatePromo: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToCrearPresupuesto: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToConcursos: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToHorariosConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
    onToggleConexion: () -> Unit = {},
    direccionGps: DireccionDominio? = null,
    estaDetectandoGps: Boolean = false,
    onSolicitarUbicacion: () -> Unit = {},
    onNavigateToGestionTurnos: () -> Unit = {},
    onNavigateToGestionVisitas: () -> Unit = {},
    // Parámetros adicionales para compatibilidad con InicioContent
    onCrearTurno: () -> Unit = {},
    onCompletarCita: (String, String) -> Unit = { _, _ -> },
    onCompletarTrabajoFast: (String, String) -> Unit = { _, _ -> },
    weatherViewModel: WeatherViewModel = hiltViewModel(),
) {
    var mostrarModalClima by remember { mutableStateOf(false) }
    var mostrarDrawer by remember { mutableStateOf(false) }
    var mostrarCentroAyuda by remember { mutableStateOf(false) }
    var mostrarPerfilRapido by remember { mutableStateOf(false) }
    val estadoClima by weatherViewModel.state.collectAsState()
    val climaActual = (estadoClima as? WeatherViewModel.WeatherState.Success)?.data

    val contexto = LocalContext.current
    val lanzadorPermisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido -> if (concedido) onSolicitarUbicacion() }

    LaunchedEffect(Unit) {
        val tienePermiso = ContextCompat.checkSelfPermission( contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (tienePermiso) onSolicitarUbicacion()
        else lanzadorPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.DarkBg)
    ) {
        // 1. CABECERA EJECUTIVA (Usuario + Clima + Notificaciones Unificados)
        CabeceraEjecutiva(
            nombreUsuario = state.nombreVisible.ifEmpty { "Sofía Martínez" },
            fotoUrl = state.photoUrl,
            calificacion = "%.1f".format(state.reputacion),
            estaVerificado = state.esVerificado,
            estaEnLinea = state.estaEnLinea,
            onToggleConexion = onToggleConexion,
            direccionGps = direccionGps,
            estaDetectandoGps = estaDetectandoGps,
            onAvatarClick = { mostrarDrawer = true },
            onNombreClick = { mostrarPerfilRapido = true }
        )

        // Contenido Desplazable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // Espacio para la barra de navegación inferior
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 2. CARRUSEL DE PROMOCIONES
            CarruselPromociones(
                avisos = state.publicidad,
                onVerTodasClick = onNavigateToPromotionList
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. CAJA DE HERRAMIENTAS (GRID RECTANGULAR)
            CajaHerramientas(
                onCrearPresupuesto = onNavigateToCrearPresupuesto,
                onVerCatalogo = onNavigateToCatalogo,
                onVerConcursos = onNavigateToConcursos,
                onGestionTurnos = onNavigateToGestionTurnos,
                onVisitasTecnicas = onNavigateToGestionVisitas,
                onCrearOferta = onNavigateToCreatePromo
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. AGENDA DE HOY (EVENTOS & TURNOS)
            AgendaHoy(
                onVerCalendario = onNavigateToCalendar,
                onLlamarCliente = { tef -> /* Intent de llamada */ },
                onAbrirMapa = { ub -> /* Intent de mapa */ },
                onVerDetalleEvento = { id -> /* Ver detalle */ }
            )
        }
    }

    // Diálogo Detalle de Clima
    if (mostrarModalClima) {
        DialogoClima(estado = estadoClima, onDismiss = { mostrarModalClima = false })
    }

    // Menú Lateral (Drawer) — [ELITE]: se llama SIEMPRE (sin envolver en `if`) para que
    // AnimatedVisibility pueda animar la salida; antes el `if` desmontaba el composable
    // de golpe en cuanto mostrarDrawer pasaba a false, y la animación de cierre nunca
    // llegaba a verse.
    ConfiguracionDrawerOverlay(
        visible = mostrarDrawer,
        onDismiss = { mostrarDrawer = false },
        onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
        onNavigateToCalendarioConfig = onNavigateToHorariosConfig,
        onNavigateToApariencia = onNavigateToApariencia,
        onNavigateToNotificaciones = onNavigateToNotificaciones,
        onNavigateToTerminos = onNavigateToTerminos,
        onNavigateToPrivacidad = onNavigateToPrivacidad,
        onNavigateToAcercaDe = onNavigateToAcercaDe,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onSignOut = onLogout,
        onNavigateToAyuda = { mostrarCentroAyuda = true },
    )

    // Tarjeta rápida de perfil (avatar de la cabecera)
    PerfilRapidoOverlay(
        visible = mostrarPerfilRapido,
        onDismiss = { mostrarPerfilRapido = false },
        onEditarPerfil = onNavigateToEditProfile
    )

    if (mostrarCentroAyuda) {
        CentroAyudaSheet(onDismiss = { mostrarCentroAyuda = false })
    }
}


// ============================================================================
// 1. COMPONENTE CABECERA EJECUTIVA
// ============================================================================
@Composable
private fun CabeceraEjecutiva(
    nombreUsuario: String,
    fotoUrl: Any?,
    calificacion: String,
    estaVerificado: Boolean,
    estaEnLinea: Boolean,
    onToggleConexion: () -> Unit,
    direccionGps: DireccionDominio?,
    estaDetectandoGps: Boolean,
    onAvatarClick: () -> Unit,
    onNombreClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = ThemeColors.HeaderBg,
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (toca para abrir el menú lateral) + Nombre (toca para ver el perfil)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.clickable { onAvatarClick() }) {

                    if (fotoUrl != null) {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Avatar Usuario",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Sin foto real: mostramos la inicial en vez de una foto de stock
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeColors.BrandOrange)
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nombreUsuario.trim().firstOrNull()?.uppercase() ?: "?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    // Dot indicador En Línea
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ThemeColors.BrandEmerald, CircleShape)
                            .border(1.5.dp, ThemeColors.DarkBg, CircleShape)
                            .align(Alignment.BottomEnd)

                    )
                }

                Column(modifier = Modifier.clickable { onNombreClick() }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Text(

                            text = nombreUsuario,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ThemeColors.TextPrimary
                        )
                        Surface(
                            color = ThemeColors.BrandOrange,
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = ThemeColors.BrandAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = calificacion,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.BrandAmber
                            )
                        }
                        Text(text = "•", fontSize = 10.sp, color = ThemeColors.TextMuted)
                        Text(
                            text = if (estaVerificado) "Verificado" else "Sin verificar",
                            fontSize = 11.sp,
                            color = ThemeColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // [PULIDO]: transición suave de color al alternar + pulso en el punto cuando
            // está en línea (mismo patrón de rememberInfiniteTransition que ya usan los rayos
            // del sol en EscenaClimatica y el radar de MoldeCabeceraSuperiorUbicacion).
            val colorConexion by animateColorAsState(
                targetValue = if (estaEnLinea) ThemeColors.BrandEmerald else ThemeColors.TextMuted,
                animationSpec = tween(280),
                label = "colorConexion"
            )
            val pulso = rememberInfiniteTransition(label = "pulsoConexion")
            val alphaPulso by pulso.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alphaPulso"
            )

            Surface(
                onClick = onToggleConexion,
                shape = RoundedCornerShape(20.dp),
                color = ThemeColors.CardBg,
                border = BorderStroke(1.dp, ThemeColors.CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                colorConexion.copy(alpha = if (estaEnLinea) alphaPulso else 1f),
                                CircleShape
                            )
                    )
                    Text(
                        text = if (estaEnLinea) "EN LÍNEA" else "DESCONECTADO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = colorConexion
                    )
                }
            }
        }

        // --- FILA DE UBICACIÓN (GPS EN VIVO) ---
        if (direccionGps != null || estaDetectandoGps) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛰️", fontSize = 16.sp)
                }

                if (estaDetectandoGps) {
                    Text(
                        text = "DETECTANDO UBICACIÓN…",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.TextMuted
                    )
                } else if (direccionGps != null) {
                    val infiniteRadar = rememberInfiniteTransition(label = "radarGps")
                    val alphaRadar by infiniteRadar.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
                        label = "alphaRadar"
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(ThemeColors.BrandOrange.copy(alpha = alphaRadar), CircleShape)
                            )
                            Text(
                                text = "GPS EN VIVO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = ThemeColors.BrandOrange
                            )
                        }
                        Text(
                            text = direccionGps.calleYNumero.uppercase().ifBlank { "UBICACIÓN DETECTADA" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOf(direccionGps.localidad, direccionGps.codigoPostal)
                                .filter { it.isNotBlank() }.joinToString(" "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
    }
}

// ============================================================================
// 2. COMPONENTE CARRUSEL DE PROMOCIONES
// ============================================================================
@Composable
private fun CarruselPromociones(
    avisos: List<com.example.myapplication.core.dominio.modelos.PublicidadDominio>,
    onVerTodasClick: () -> Unit
) {
    if (avisos.isEmpty()) return
    val contexto = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera de Sección
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ThemeColors.BrandOrange, RoundedCornerShape(2.dp))
                )
                Text(
                    text = "PROMOCIONES & OFERTAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.TextPrimary,
                    letterSpacing = 0.8.sp
                )
            }

            TextButton(
                onClick = onVerTodasClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "VER TODAS >",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.BrandCyan
                )
            }
        }

        // Horizontal Carousel LazyRow
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(avisos, key = { it.id }) { aviso ->
                TarjetaPromocionRectangular(
                    tag = "PUBLICIDAD",
                    tagColor = ThemeColors.BrandCyan,
                    promoId = aviso.id.take(6),
                    vencimiento = aviso.rubro ?: "",
                    titulo = aviso.empresa,
                    descripcion = aviso.descripcion ?: aviso.direccion.orEmpty(),
                    infoPie = aviso.direccion ?: "",
                    textoBoton = "Contactar",
                    iconoBoton = Icons.Default.Call,
                    onClickBoton = { abrirContactoPublicidad(contexto, aviso) }
                )
            }
        }
    }
}

/** Abre el link (web/instagram) si hay, si no llama al teléfono cargado en el aviso. */
private fun abrirContactoPublicidad(
    contexto: android.content.Context,
    aviso: com.example.myapplication.core.dominio.modelos.PublicidadDominio
) {
    try {
        if (!aviso.contactoLink.isNullOrBlank()) {
            var url = aviso.contactoLink
            if (!url!!.startsWith("http")) url = "https://$url"
            contexto.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        } else if (!aviso.contactoTelefono.isNullOrBlank()) {
            contexto.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${aviso.contactoTelefono}")))
        }
    } catch (e: Exception) {
        android.util.Log.e("CarruselPublicidad", "No se pudo abrir el contacto: ${e.message}")
    }
}

@Composable
private fun TarjetaPromocionRectangular(
    tag: String,
    tagColor: Color,
    promoId: String,
    vencimiento: String,
    titulo: String,
    descripcion: String,
    infoPie: String,
    textoBoton: String,
    iconoBoton: ImageVector,
    onClickBoton: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(310.dp)
            .height(145.dp),
        color = ThemeColors.CardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ThemeColors.CardBorder)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Acento lateral izquierdo
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(tagColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = tagColor,
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "ID: #$promoId",
                            fontSize = 10.sp,
                            color = ThemeColors.TextMuted
                        )
                    }

                    Text(
                        text = vencimiento,
                        fontSize = 10.sp,
                        color = ThemeColors.BrandAmber,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Centro: Título y Descrip
                Column {
                    Text(
                        text = titulo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = descripcion,
                        fontSize = 11.sp,
                        color = ThemeColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                // Fila Inferior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = infoPie,
                        fontSize = 11.sp,
                        color = ThemeColors.TextMuted
                    )

                    OutlinedButton(
                        onClick = onClickBoton,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeColors.TextPrimary)
                    ) {
                        Text(text = textoBoton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = iconoBoton, contentDescription = null, modifier = Modifier.size(12.dp), tint = tagColor)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. COMPONENTE CAJA DE HERRAMIENTAS (GRID)
// ============================================================================
@Composable
private fun CajaHerramientas(
    onCrearPresupuesto: () -> Unit,
    onVerCatalogo: () -> Unit,
    onVerConcursos: () -> Unit,
    onGestionTurnos: () -> Unit,
    onVisitasTecnicas: () -> Unit,
    onCrearOferta: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(ThemeColors.BrandCyan, RoundedCornerShape(2.dp)))
                Text(
                    text = "CAJA DE HERRAMIENTAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.TextPrimary,
                    letterSpacing = 0.8.sp
                )
            }
            Text(text = "ATAJOS APP", fontSize = 9.sp, color = ThemeColors.TextMuted, letterSpacing = 1.sp)
        }

        // Grid 3x2 Rectangular
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonHerramienta(
                    titulo = "Crear Presupuesto",
                    icono = Icons.Default.ReceiptLong,
                    colorAcento = ThemeColors.BrandOrange,
                    onClick = onCrearPresupuesto,
                    modifier = Modifier.weight(1f)
                )
                BotonHerramienta(
                    titulo = "Catálogo Servicios",
                    icono = Icons.Default.MenuBook,
                    colorAcento = ThemeColors.BrandCyan,
                    onClick = onVerCatalogo,
                    modifier = Modifier.weight(1f)
                )
                BotonHerramienta(
                    titulo = "Concursos Activos",
                    icono = Icons.Default.Gavel,
                    colorAcento = ThemeColors.BrandAmber,
                    tieneBadge = true,
                    onClick = onVerConcursos,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonHerramienta(
                    titulo = "Gestión Turnos",
                    icono = Icons.Default.CalendarMonth,
                    colorAcento = ThemeColors.BrandEmerald,
                    onClick = onGestionTurnos,
                    modifier = Modifier.weight(1f)
                )
                BotonHerramienta(
                    titulo = "Visitas Técnicas",
                    icono = Icons.Default.AssignmentInd,
                    colorAcento = ThemeColors.TextSecondary,
                    onClick = onVisitasTecnicas,
                    modifier = Modifier.weight(1f)
                )
                BotonHerramienta(
                    titulo = "Crear Oferta",
                    icono = Icons.Default.Campaign,
                    colorAcento = ThemeColors.BrandPurple,
                    onClick = onCrearOferta,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BotonHerramienta(
    titulo: String,
    icono: ImageVector,
    colorAcento: Color,
    tieneBadge: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(84.dp),
        color = ThemeColors.CardBg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ThemeColors.CardBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            if (tieneBadge) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(ThemeColors.BrandEmerald, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(colorAcento.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, colorAcento.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icono, contentDescription = null, tint = colorAcento, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = titulo,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================================
// 4. COMPONENTE AGENDA DE HOY (CALENDARIO)
// ============================================================================
@Composable
private fun AgendaHoy(
    onVerCalendario: () -> Unit,
    onLlamarCliente: (String) -> Unit,
    onAbrirMapa: (String) -> Unit,
    onVerDetalleEvento: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(ThemeColors.BrandAmber, RoundedCornerShape(2.dp)))
                Text(
                    text = "AGENDA DE HOY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.TextPrimary,
                    letterSpacing = 0.8.sp
                )
            }

            Surface(
                color = ThemeColors.CardBg,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, ThemeColors.CardBorder),
                onClick = onVerCalendario
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = ThemeColors.BrandOrange, modifier = Modifier.size(10.dp))
                    Text(text = "Mar, 04 Ago", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ThemeColors.TextPrimary)
                }
            }
        }

        // Lista de Eventos
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Evento 1: Visita Técnica
            TarjetaEventoAgenda(
                tipoEvento = "VISITA TÉCNICA",
                estado = "EN PROCESO",
                hora = "09:30 AM",
                titulo = "Instalación Tablero Trifásico Comercial",
                cliente = "Carlos R.",
                ubicacion = "Av. Aconquija 1240",
                colorAcento = ThemeColors.BrandOrange,
                acciones = {
                    Button(
                        onClick = { onLlamarCliente("123456") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(text = "Llamar", fontSize = 10.sp, color = ThemeColors.BrandCyan)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onAbrirMapa("Av. Aconquija 1240") },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.BrandOrange),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(text = "Mapa", fontSize = 10.sp, color = Color.White)
                    }
                }
            )

            // Evento 2: Turno Confirmado
            TarjetaEventoAgenda(
                tipoEvento = "TURNO CONFIRMADO",
                estado = null,
                hora = "02:00 PM",
                titulo = "Mantenimiento Split 4500 Frigorías",
                cliente = "Mariana L.",
                ubicacion = "Calle Jujuy 450",
                colorAcento = ThemeColors.BrandCyan,
                acciones = {
                    TextButton(onClick = { onVerDetalleEvento("EV-2") }) {
                        Text(text = "Detalle", fontSize = 10.sp, color = ThemeColors.TextSecondary)
                    }
                }
            )

            // Evento 3: Envío / Entrega
            TarjetaEventoAgenda(
                tipoEvento = "ENVÍO MATERIALES",
                estado = "EN PREPARACIÓN",
                hora = "05:15 PM",
                titulo = "Entrega Inversor Solar Grid 3KW",
                cliente = "#ORD-993",
                ubicacion = "Depósito Yerba Buena",
                colorAcento = ThemeColors.BrandPurple,
                acciones = null
            )
        }
    }
}

@Composable
private fun TarjetaEventoAgenda(
    tipoEvento: String,
    estado: String?,
    hora: String,
    titulo: String,
    cliente: String,
    ubicacion: String,
    colorAcento: Color,
    acciones: (@Composable () -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ThemeColors.CardBg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ThemeColors.CardBorder)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(colorAcento)
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = colorAcento.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text(
                                text = tipoEvento,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = colorAcento,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (estado != null) {
                            Surface(
                                color = ThemeColors.BrandEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(2.dp)
                            ) {
                                Text(
                                    text = estado,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeColors.BrandEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(text = hora, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ThemeColors.TextPrimary)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ThemeColors.TextPrimary)

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ThemeColors.TextMuted, modifier = Modifier.size(12.dp))
                        Text(text = cliente, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ThemeColors.TextPrimary)
                        Text(text = "•", fontSize = 10.sp, color = ThemeColors.TextMuted)
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ThemeColors.TextMuted, modifier = Modifier.size(12.dp))
                        Text(text = ubicacion, fontSize = 11.sp, color = ThemeColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    if (acciones != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            acciones()
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// DIÁLOGO DETALLE DE CLIMA
// ============================================================================
@Composable
private fun DialogoClima(estado: WeatherViewModel.WeatherState, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            color = ThemeColors.CardBg,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ThemeColors.CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CLIMA OPERATIVO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ThemeColors.TextPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = ThemeColors.TextMuted)
                    }
                }

                when (estado) {
                    is WeatherViewModel.WeatherState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ThemeColors.BrandAmber)
                        }
                    }
                    is WeatherViewModel.WeatherState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            Text("No se pudo obtener el clima", fontSize = 12.sp, color = ThemeColors.TextMuted)
                        }
                    }
                    is WeatherViewModel.WeatherState.Success -> {
                        val clima = estado.data
                        val estadoClimatico = remember(clima.emojiClima) { estadoClimaDesdeEmoji(clima.emojiClima) }

                        Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                            EscenaClimatica(estado = estadoClimatico, modifier = Modifier.matchParentSize())

                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                val infiniteEmoji = rememberInfiniteTransition(label = "dialogoEmoji")
                                val escala by infiniteEmoji.animateFloat(
                                    initialValue = 1f, targetValue = 1.14f,
                                    animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
                                    label = "dialogoEmojiEscala"
                                )
                                val balanceo by infiniteEmoji.animateFloat(
                                    initialValue = -4f, targetValue = 4f,
                                    animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
                                    label = "dialogoEmojiBalanceo"
                                )

                                Text(
                                    text = clima.emojiClima,
                                    fontSize = 58.sp,
                                    modifier = Modifier.scale(escala).rotate(balanceo)
                                )
                                Column {
                                    Text(text = clima.temperatura, fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    Text(text = clima.descripcionClima, fontSize = 14.sp, color = Color.White.copy(alpha = 0.92f), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("💧 ${clima.humedad}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                                        Text("💨 ${clima.velocidadViento}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                                    }
                                }
                            }
                        }

                        FilaDetalleClima("Ubicación", clima.nombreCiudad)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = "Cerrar", fontSize = 12.sp, color = ThemeColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun FilaDetalleClima(etiqueta: String, valor: String, colorValor: Color = ThemeColors.TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, fontSize = 11.sp, color = ThemeColors.TextMuted)
        Text(text = valor, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorValor)
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", backgroundColor = 0xFF030712)
@Composable
fun InicioScreenPreview() {
    InicioScreen(
        state = EstadoDashboardUi(
            nombreVisible = "Sofía Martínez",
            saludo = "¡Buenos días!"
        )
    )
}
