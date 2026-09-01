package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.example.myapplication.prestador.viewmodel.dashboard.EstadoDashboardUi

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
    onNavigateToGestionTurnos: () -> Unit = {},
    onNavigateToGestionVisitas: () -> Unit = {},
    // Parámetros adicionales para compatibilidad con InicioContent
    onCrearTurno: () -> Unit = {},
    onCompletarCita: (String, String) -> Unit = { _, _ -> },
    onCompletarTrabajoFast: (String, String) -> Unit = { _, _ -> },
) {
    var mostrarModalClima by remember { mutableStateOf(false) }
    var mostrarDrawer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.DarkBg)
    ) {
        // 1. CABECERA EJECUTIVA (Usuario + Clima + Notificaciones Unificados)
        CabeceraEjecutiva(
            nombreUsuario = state.nombreVisible.ifEmpty { "Sofía Martínez" },
            fotoUrl = state.photoUrl?.toString() ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
            calificacion = "4.95",
            notificacionesPendientes = 3,
            onVerClimaClick = { mostrarModalClima = true },
            onNotificacionesClick = onNavigateToNotificaciones,
            onPerfilClick = { mostrarDrawer = true }
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
                onVerTodasClick = onNavigateToPromotionList,
                onCompartirPromo = { id -> /* Lógica de compartir */ },
                onReclamarPromo = { id -> /* Lógica de reclamar */ }
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
        DialogoClima(onDismiss = { mostrarModalClima = false })
    }

    // Menú Lateral (Drawer)
    if (mostrarDrawer) {
        ConfiguracionDrawerOverlay(
            visible = true,
            onDismiss = { mostrarDrawer = false },
            onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
            onNavigateToCalendarioConfig = onNavigateToHorariosConfig,
            onNavigateToApariencia = onNavigateToApariencia,
            onNavigateToNotificaciones = onNavigateToNotificaciones,
            onNavigateToTerminos = onNavigateToTerminos,
            onNavigateToPrivacidad = onNavigateToPrivacidad,
            onNavigateToAcercaDe = onNavigateToAcercaDe,
            onNavigateToEditProfile = onNavigateToEditProfile,
            onSignOut = onLogout
        )
    }
}


// ============================================================================
// 1. COMPONENTE CABECERA EJECUTIVA
// ============================================================================
@Composable
private fun CabeceraEjecutiva(
    nombreUsuario: String,
    fotoUrl: String?,
    calificacion: String,
    notificacionesPendientes: Int,
    onVerClimaClick: () -> Unit,
    onNotificacionesClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = ThemeColors.HeaderBg,
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Usuario + Avatar Rectangular
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable { onPerfilClick() }
            ) {
                Box {
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = "Avatar Usuario",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Dot indicador En Línea
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ThemeColors.BrandEmerald, CircleShape)
                            .border(1.5.dp, ThemeColors.DarkBg, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Column {
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
                            text = "Especialista App",
                            fontSize = 11.sp,
                            color = ThemeColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Clima & Botón Notificaciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Widget Clima
                Surface(
                    onClick = onVerClimaClick,
                    color = ThemeColors.CardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ThemeColors.CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Clima",
                            tint = ThemeColors.BrandAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "24°C",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ThemeColors.TextPrimary
                            )
                            Text(
                                text = "TUCUMÁN",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.TextMuted
                            )
                        }
                    }
                }

                // Botón Notificaciones
                Box {
                    IconButton(
                        onClick = onNotificacionesClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(ThemeColors.CardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ThemeColors.CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = ThemeColors.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (notificacionesPendientes > 0) {
                        Surface(
                            color = ThemeColors.BrandOrange,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = notificacionesPendientes.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
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
    onVerTodasClick: () -> Unit,
    onCompartirPromo: (String) -> Unit,
    onReclamarPromo: (String) -> Unit
) {
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
            item {
                TarjetaPromocionRectangular(
                    tag = "HOT SALE 20% OFF",
                    tagColor = ThemeColors.BrandOrange,
                    promoId = "PR-882",
                    vencimiento = "Vence en 3d",
                    titulo = "Mantenimiento Integral de Aires Split",
                    descripcion = "Limpieza de filtros, carga de gas y diagnóstico eléctrico.",
                    infoPie = "Disponibles: 5/10",
                    textoBoton = "Compartir",
                    iconoBoton = Icons.Default.Share,
                    onClickBoton = { onCompartirPromo("PR-882") }
                )
            }

            item {
                TarjetaPromocionRectangular(
                    tag = "COMBO 2X1",
                    tagColor = ThemeColors.BrandCyan,
                    promoId = "PR-901",
                    vencimiento = "Destacado",
                    titulo = "Inspección de Tableros + Termografía",
                    descripcion = "Medición de consumo y detección de puntos calientes.",
                    infoPie = "Zona: Yerba Buena",
                    textoBoton = "Reclamar",
                    iconoBoton = Icons.Default.ChevronRight,
                    onClickBoton = { onReclamarPromo("PR-901") }
                )
            }
        }
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
private fun DialogoClima(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = ThemeColors.CardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ThemeColors.BrandCyan.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = ThemeColors.BrandAmber)
                        Text(text = "CLIMA OPERATIVO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ThemeColors.TextPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = ThemeColors.TextMuted)
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                FilaDetalleClima("Ubicación", "San Miguel de Tucumán")
                FilaDetalleClima("Temperatura", "24°C (Soleado)")
                FilaDetalleClima("Humedad / Viento", "55% | 12 km/h SE")
                FilaDetalleClima("Condición Trabajo", "Óptimo para Exterior", ThemeColors.BrandEmerald)

                Spacer(modifier = Modifier.height(4.dp))

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
