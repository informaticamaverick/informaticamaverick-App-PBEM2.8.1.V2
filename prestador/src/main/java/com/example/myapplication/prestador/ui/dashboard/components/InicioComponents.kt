package com.example.myapplication.prestador.ui.dashboard.components

import android.icu.util.Calendar
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.dashboard.DashboardUiState
import com.example.myapplication.prestador.viewmodel.dashboard.DashboardViewModel
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.prestador.data.model.OportunidadItem
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.viewmodel.calendar.CalendarViewModel
import com.example.myapplication.prestador.viewmodel.oportunidades.OportunidadesViewModel
import com.example.myapplication.prestador.ui.dashboard.components.GestionarCatalogoSheet

@Composable
fun InicioScreen(
    state: DashboardUiState,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToServiceConfig: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onCrearTurno: () -> Unit = {},
    onNavigateToPresupuesto: () -> Unit,
    onNavigateToCrearPresupuesto: () -> Unit = {},
    onNavigateToPresupuestos: () -> Unit,
    onNavigateToChat: (clientId: String) -> Unit = {},
    onCompletarCita: (appointmentId: String, clientId: String) -> Unit = { _, _ -> },
    onCompletarTrabajoFast: (appointmentId: String, clientId: String) -> Unit = { _, _ -> },
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToCalendarioConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
) {
    val colors = getPrestadorColors()
    val mostrarFast = false // Fast se implementará más adelante
    val isProfessional = state.serviceType.equals("PROFESSIONAL", ignoreCase = true)
    val scrollState = rememberScrollState()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    var mostrarDrawer by remember { mutableStateOf(false) }
    var mostrarCrearTurno by remember { mutableStateOf(false) }
    var mostrarCrearPresupuesto by remember { mutableStateOf(false) }
    var mostrarCatalogo by remember { mutableStateOf(false) }
    var mostrarReportarProblema by remember { mutableStateOf(false) }
    var mostrarChatSoporte by remember { mutableStateOf(false) }
    var mostrarCentroAyuda by remember { mutableStateOf(false) }
    var mostrarAvatarPopup by remember { mutableStateOf(false) }


    if (mostrarCrearPresupuesto) {
        CrearPresupuestoRapidoSheet(onDismiss = { mostrarCrearPresupuesto = false })
    }

    if (mostrarCatalogo) {
        GestionarCatalogoSheet(onDismiss = { mostrarCatalogo = false })
    }

    if (mostrarReportarProblema) {
        ReportarProblemaSheet(onDismiss = { mostrarReportarProblema = false })
    }

    if (mostrarChatSoporte) {
        ChatSoporteSheet(onDismiss = { mostrarChatSoporte = false })
    }

    if (mostrarCentroAyuda) {
        CentroAyudaSheet(onDismiss = { mostrarCentroAyuda = false })
    }

    if (mostrarCrearTurno) {
        CrearTurnoDialog(
            onDismiss = { mostrarCrearTurno = false },
            onConfirmar = { nombre, fecha, hora, servicio, notas ->
                calendarViewModel.crearTurno(nombre, fecha, hora, servicio, notas)
                mostrarCrearTurno = false
            }
        )
    }
    val shadowAlpha by animateFloatAsState(
        targetValue = if (scrollState.value > 0) 1f else 0f,
        animationSpec = tween(300),
        label = "shadowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundColor)
        ) {
        // ── HEADER REDISEÑADO ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFFF7043),
                            0.45f to Color(0xFFFF9E80),
                            1.0f to Color(0xFFFFCCBC)
                        )
                    )
                )
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Column {
                // Fila superior: hamburger + saludo + avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menú hamburger (izquierda) → abre drawer animado
                    IconButton(
                        onClick = { mostrarDrawer = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Menu, null, tint = Color(0xFF5D2000), modifier = Modifier.size(24.dp))
                    }

                    // Saludo + nombre (centro)
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 14.dp)
                    ) {
                        Text(
                            text = state.saludo,
                            fontSize = 13.sp,
                            color = Color(0xFF7B3000).copy(alpha = 0.75f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = state.nombrePrestador.substringBefore(' ').ifBlank { "Prestador" } + " \uD83D\uDC4B",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3D1100),
                            letterSpacing = 0.3.sp
                        )
                    }

                    // Avatar (derecha) → abre popup de perfil
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White.copy(alpha = 0.50f), CircleShape)
                            .border(2.dp, Color(0xFFFF7043).copy(alpha = 0.6f), CircleShape)
                            .clip(CircleShape)
                            .clickable { mostrarAvatarPopup = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarBitmap = remember(state.imageBase64) {
                            val b64 = state.imageBase64
                            if (b64 != null && !b64.startsWith("http")) {
                                try {
                                    val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                } catch (e: Exception) { null }
                            } else null
                        }
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = state.nombrePrestador.firstOrNull()?.uppercase() ?: "P",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D2000)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Fila inferior: chips de stats + botón Fast
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.45f)) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF5D2000), modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("0 hoy", fontSize = 11.sp, color = Color(0xFF5D2000), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.45f)) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Pending, null, tint = Color(0xFF5D2000), modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("0 pendientes", fontSize = 11.sp, color = Color(0xFF5D2000), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Botón Fast — se implementará más adelante
                }
            }
        } // fin header

        // ── CONTENIDO SCROLLEABLE ──────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                Spacer(Modifier.height(20.dp))

                // ── CARRUSEL ───────────────────────────────────────────
                val cardSuscripcion = when (state.serviceType) {
                    "PROFESSIONAL" -> CarruselItem("Agenda Pro", "Reservas online 24/7, recordatorios y estadisticas.", Icons.Default.CalendarMonth, Brush.horizontalGradient(listOf(Color(0xFF5C6BC0), Color(0xFF7986CB))))
                    "RENTAL"       -> CarruselItem("Vitrina Premium", "Destaca tus publicaciones con hasta 20 fotos.", Icons.Default.Star, Brush.horizontalGradient(listOf(Color(0xFF26A69A), Color(0xFF4DB6AC))))
                    "TECHNICAL"    -> CarruselItem("Servicio Fast", "Aparece primero en urgencias y recibe solicitudes cercanas.", Icons.Default.Bolt, Brush.horizontalGradient(listOf(Color(0xFFFF6B35), Color(0xFFE53935))))
                    else           -> CarruselItem("Destaca tu servicio", "Completa tu perfil al 100% para aparecer primero.", Icons.Default.TrendingUp, Brush.horizontalGradient(listOf(Color(0xFF7B4FD4), Color(0xFF4A90D9))))
                }
                val carruselItems = buildList {
                    add(CarruselItem("Consigue mas clientes", "Completa tu perfil al 100% hoy mismo.", Icons.Default.TrendingUp, Brush.horizontalGradient(listOf(Color(0xFF4A90D9), Color(0xFF7B4FD4)))))
                    if (isProfessional) {
                        add(CarruselItem("Configura tu agenda", "Define horarios, duracion y modalidad.", Icons.Default.EventAvailable, Brush.horizontalGradient(listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5)))))
                        add(CarruselItem("Reduce ausencias", "Envia recordatorios y manten tu agenda al dia.", Icons.Default.NotificationsActive, Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))))
                    } else {
                        add(CarruselItem("Activa tus promociones", "Llega a mas usuarios con ofertas especiales.", Icons.Default.LocalOffer, Brush.horizontalGradient(listOf(Color(0xFFFF6B35), Color(0xFFFF8C42)))))
                    }
                    add(cardSuscripcion)
                    add(CarruselItem("Sin Anuncios", "Experiencia limpia, sin banners ni interrupciones.", Icons.Default.DoNotDisturb, Brush.horizontalGradient(listOf(Color(0xFF37474F), Color(0xFF546E7A)))))
                }

                val pagerState = rememberPagerState(pageCount = { carruselItems.size })
                LaunchedEffect(pagerState) {
                    while (true) {
                        delay(4_000L)
                        pagerState.animateScrollToPage((pagerState.currentPage + 1) % carruselItems.size)
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                        val item = carruselItems[page]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .shadow(6.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(item.gradiente)
                                .padding(horizontal = 22.dp, vertical = 18.dp)
                        ) {
                            Icon(item.icono, null, tint = Color.White.copy(alpha = 0.10f), modifier = Modifier.size(90.dp).align(Alignment.CenterEnd))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icono, null, tint = Color.White, modifier = Modifier.size(26.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(item.titulo, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Spacer(Modifier.height(4.dp))
                                    Text(item.subtitulo, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        repeat(carruselItems.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (pagerState.currentPage == index) 22.dp else 6.dp, 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (pagerState.currentPage == index) colors.primaryOrange else colors.primaryOrange.copy(alpha = 0.3f))
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── ACCESOS RAPIDOS ────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle(title = "Accesos rapidos", accentColor = colors.primaryOrange)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AccesoRapidoButton(modifier = Modifier.weight(1f), icon = Icons.Default.CalendarToday, label = "Crear Turno", color = Color(0xFF4A90D9), onClick = { mostrarCrearTurno = true })
                        AccesoRapidoButton(modifier = Modifier.weight(1f), icon = Icons.Default.Description, label = "Crear Presupuesto", color = Color(0xFF7B4FD4), onClick = { mostrarCrearPresupuesto = true })
                        AccesoRapidoButton(modifier = Modifier.weight(1f), icon = Icons.Default.Inventory2, label = "Mi Catálogo", color = Color(0xFF059669), onClick = { mostrarCatalogo = true })
                    }
                    if (isProfessional) {
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AccesoRapidoButton(modifier = Modifier.weight(1f), icon = Icons.Default.EventAvailable, label = "Disponibilidad", color = Color(0xFF10B981), onClick = onNavigateToCalendar)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── AYUDA Y SOPORTE ────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle(title = "Ayuda y soporte", accentColor = colors.primaryOrange)
                    Spacer(Modifier.height(12.dp))
                    AyudaSoporteCard(
                        icon = Icons.Default.BugReport,
                        iconColor = Color(0xFFEF4444),
                        titulo = "Reportar un problema",
                        subtitulo = "Contanos qué pasó y lo revisamos",
                        onClick = { mostrarReportarProblema = true }
                    )
                    Spacer(Modifier.height(10.dp))
                    AyudaSoporteCard(
                        icon = Icons.Default.SupportAgent,
                        iconColor = Color(0xFF25D366),
                        titulo = "Chat con soporte",
                        subtitulo = "WhatsApp o email — respondemos rápido",
                        onClick = { mostrarChatSoporte = true }
                    )
                    Spacer(Modifier.height(10.dp))
                    AyudaSoporteCard(
                        icon = Icons.Default.MenuBook,
                        iconColor = Color(0xFF4A90D9),
                        titulo = "Centro de ayuda",
                        subtitulo = "Preguntas frecuentes por categoría",
                        onClick = { mostrarCentroAyuda = true }
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── PROXIMO SERVICIO ───────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle(title = "Proximo servicio", accentColor = colors.primaryOrange)
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                        onClick = onNavigateToCalendar
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.horizontalGradient(listOf(colors.primaryOrange.copy(alpha = 0.06f), Color.Transparent)), RoundedCornerShape(20.dp))
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).background(colors.primaryOrange.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarToday, null, tint = colors.primaryOrange, modifier = Modifier.size(26.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sin servicios pendientes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("Toca para ver tu calendario", fontSize = 12.sp, color = colors.textSecondary)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = colors.primaryOrange.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                        }
                    }
                }


                Spacer(Modifier.height(32.dp))
            } // fin Column scrolleable

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.15f * shadowAlpha), Color.Transparent)))
            )
        } // fin Box contenedor
        } // fin Column principal

        // ── DRAWER DE CONFIGURACIÓN ────────────────────────────────
        ConfiguracionDrawerOverlay(
            visible = mostrarDrawer,
            onDismiss = { mostrarDrawer = false },
            onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
            onNavigateToCalendarioConfig = onNavigateToCalendarioConfig,
            onNavigateToApariencia = onNavigateToApariencia,
            onNavigateToNotificaciones = onNavigateToNotificaciones,
            onNavigateToTerminos = onNavigateToTerminos,
            onNavigateToPrivacidad = onNavigateToPrivacidad,
            onNavigateToAcercaDe = onNavigateToAcercaDe,
            onNavigateToEditProfile = onNavigateToEditProfile,
            onSignOut = onLogout
        )

        // ── POPUP DE AVATAR (anclado top-right, sale desde el avatar) ──
        AvatarProfilePopup(
            visible = mostrarAvatarPopup,
            nombrePrestador = state.nombrePrestador,
            email = state.email,
            imageBase64 = state.imageBase64,
            onClose = { mostrarAvatarPopup = false },
            onEditProfile = onNavigateToEditProfile
        )
    } // fin Box raíz
}

@Composable
private fun SectionTitle(title: String, accentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(accentColor, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.2.sp)
    }
}

private data class CarruselItem(
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector = Icons.Default.Star,
    val gradiente: Brush
)

@Composable
private fun AccesoRapidoButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AyudaSoporteCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text(subtitulo, fontSize = 12.sp, color = colors.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTurnoDialog(
    onDismiss: () -> Unit,
    onConfirmar: (clienteNombre: String, fecha: String, hora: String, servicio: String, notas: String) -> Unit
) {
    val colors = getPrestadorColors()
    val accent = colors.primaryOrange

    var clienteNombre by remember { mutableStateOf("") }
    var servicio by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }

    // Fecha
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)
    var showDatePicker by remember { mutableStateOf(false) }
    var fechaDisplay by remember {
        mutableStateOf(String.format("%02d/%02d/%04d",
            calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)))
    }
    var fechaInternal by remember {
        mutableStateOf(String.format("%04d-%02d-%02d",
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))
    }

    // Hora con TimeInput de Material3
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = 0,
        is24Hour = true
    )
    var showTimePicker by remember { mutableStateOf(false) }
    var horaDisplay by remember {
        mutableStateOf(String.format("%02d:00", calendar.get(Calendar.HOUR_OF_DAY)))
    }

    val puedeGuardar = clienteNombre.isNotBlank() && servicio.isNotBlank()

    // DatePicker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        fechaDisplay = String.format("%02d/%02d/%04d",
                            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
                        fechaInternal = String.format("%04d-%02d-%02d",
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                    }
                    showDatePicker = false
                }) { Text("Confirmar", color = accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = colors.textSecondary) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = colors.surfaceColor,
                titleContentColor = colors.textPrimary,
                headlineContentColor = accent,
                weekdayContentColor = colors.textSecondary,
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = accent,
                todayContentColor = accent,
                todayDateBorderColor = accent
            )
        ) { DatePicker(state = datePickerState) }
    }

    // TimePicker dialog con Material3 TimeInput
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surfaceColor,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seleccionar hora", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(Modifier.height(16.dp))
                    TimeInput(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            timeSelectorSelectedContainerColor = accent,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContainerColor = accent.copy(alpha = 0.1f),
                            timeSelectorUnselectedContentColor = accent,
                            clockDialColor = accent.copy(alpha = 0.08f),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = colors.textPrimary,
                            selectorColor = accent,
                            containerColor = colors.surfaceColor,
                            periodSelectorSelectedContainerColor = accent,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContainerColor = accent.copy(alpha = 0.08f),
                            periodSelectorUnselectedContentColor = accent
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancelar", color = colors.textSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                horaDisplay = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Confirmar", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceColor,
            shadowElevation = 8.dp
        ) {
            Column {
                // Header — gradiente suave naranja → fondo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(accent.copy(alpha = 0.15f), Color.Transparent)),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(accent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, null, tint = accent, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Crear Turno", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("Turno manual sin chat", fontSize = 12.sp, color = colors.textSecondary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {

                    OutlinedTextField(
                        value = clienteNombre,
                        onValueChange = { clienteNombre = it },
                        label = { Text("Nombre del cliente *") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent, focusedLabelColor = accent,
                            cursorColor = accent, unfocusedBorderColor = colors.border
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Fecha y hora en fila
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Botón fecha
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.07f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
                            onClick = { showDatePicker = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, null, tint = accent, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Fecha", fontSize = 11.sp, color = accent, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(fechaDisplay, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                        }
                        // Botón hora
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.07f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
                            onClick = { showTimePicker = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, null, tint = accent, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Hora", fontSize = 11.sp, color = accent, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(horaDisplay, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = servicio,
                        onValueChange = { servicio = it },
                        label = { Text("Descripción del servicio *") },
                        leadingIcon = { Icon(Icons.Default.Build, null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent, focusedLabelColor = accent,
                            cursorColor = accent, unfocusedBorderColor = colors.border
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas (opcional)") },
                        leadingIcon = { Icon(Icons.Default.Notes, null, tint = colors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent, focusedLabelColor = accent,
                            cursorColor = accent, unfocusedBorderColor = colors.border
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { onConfirmar(clienteNombre.trim(), fechaInternal, horaDisplay, servicio.trim(), notas.trim()) },
                        enabled = puedeGuardar,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            disabledContainerColor = accent.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar Turno", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

