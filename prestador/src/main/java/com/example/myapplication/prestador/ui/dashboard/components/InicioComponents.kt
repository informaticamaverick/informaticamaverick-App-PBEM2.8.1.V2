package com.example.myapplication.prestador.ui.dashboard.components

import android.R
import android.view.RoundedCorner
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.TableInfo
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.dashboard.DashboardUiState
import com.example.myapplication.prestador.viewmodel.dashboard.DashboardViewModel
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.prestador.data.model.OportunidadItem
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.viewmodel.oportunidades.OportunidadesViewModel
@Composable
fun InicioScreen(
    state: DashboardUiState,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToServiceConfig: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPresupuesto: () -> Unit,
    onNavigateToPresupuestos: () -> Unit,
    onNavigateToChat: (clientId: String) -> Unit,
    onCompletarCita: (appointmentId: String, clientId: String) -> Unit = { _, _ ->},
    onCompletarTrabajoFast: (appointmentId: String, clientId: String) -> Unit = { _, _ -> },
    oportunidades: List<OportunidadItem> = emptyList(),
    oportunidadesLoading: Boolean = false,
    mensajeAceptar: String? = null,
    onAceptarOportunidad: (OportunidadItem) -> Unit = {},
    onRefreshOportunidades: () -> Unit = {},
    onLimpiarMensaje: () -> Unit = {},
    nuevaSolicitud: OportunidadItem? = null,
    onDescartarNuevaSolicitud: () -> Unit = {},
    proximaCita: String? = null,
    restriccionHorario: String? = null,
    restriccionDistancia: String? = null,
    restriccionSolicitudActiva: String? = null,
    restriccionCitaEnCurso: String? = null,
    conectadoFast: Boolean = true,
    onNavigateToPromotionList: () -> Unit = {},
    onToggleConexionFast: () -> Unit = {},
) {
    val colors = getPrestadorColors()
    val mostarFast = state.serviceType.equals("TECHNICAL", ignoreCase = true)
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showCompletarDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val shadowAlpha by animateFloatAsState(
        targetValue = if (scrollState.value > 0) 1f else 0f,
        animationSpec = tween(300),
        label = "shadowAlpha"
    )
    LaunchedEffect(mensajeAceptar) {
        if (mensajeAceptar != null) {
            kotlinx.coroutines.delay(3000)
            onLimpiarMensaje()
        }
    }

    // Popup automático cuando llega una nueva solicitud Fast
    if (mostarFast && nuevaSolicitud != null) {
        NuevaSolicitudFastDialog(
            solicitud = nuevaSolicitud,
            onAceptar = {
                onAceptarOportunidad(nuevaSolicitud)
            },
            onDescartar = onDescartarNuevaSolicitud,
            proximaCita = proximaCita,
            restriccionHorario = restriccionHorario
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${state.saludo}, ${state.nombrePrestador.substringBefore(' ').ifBlank { "Prestador" }} 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarBitmap = remember(state.imageBase64) {
                                val b64 = state.imageBase64
                                if (b64 != null && !b64.startsWith("http")) {
                                    try {
                                        val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            ?.asImageBitmap()
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
                                IconButton(onClick = { showMenu = !showMenu }) {
                                    Text(
                                        text = state.nombrePrestador.firstOrNull()?.uppercase() ?: "P",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        // Tap sobre la foto también abre el menú
                        if (state.imageBase64 != null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable { showMenu = !showMenu }
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(colors.surfaceColor)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Mi Perfil", fontSize = 14.sp, color = colors.textPrimary)
                                    }
                                },
                                onClick = { showMenu = false; onNavigateToEditProfile() }
                            )
                            HorizontalDivider(color = colors.divider)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Configuración", fontSize = 14.sp, color = colors.textPrimary)
                                    }
                                },
                                onClick = { showMenu = false; onNavigateToServiceConfig() }
                            )
                            HorizontalDivider(color = colors.divider)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ExitToApp, null, tint = colors.error, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Cerrar Sesion", fontSize = 14.sp, color = colors.error)
                                    }
                                },
                                onClick = { showMenu = false; onLogout() }
                            )
                        }
                    }
                }

                // Botón conectar/desconectar Fast
                Spacer(Modifier.height(14.dp))
                var isConnecting by remember { mutableStateOf(false) }
                val sweepAngle = remember { androidx.compose.animation.core.Animatable(0f) }
                val pulseScale = remember { androidx.compose.animation.core.Animatable(1f) }
                val wave1 = remember { androidx.compose.animation.core.Animatable(0f) }
                val wave2 = remember { androidx.compose.animation.core.Animatable(0f) }
                val wave3 = remember { androidx.compose.animation.core.Animatable(0f) }

                LaunchedEffect(isConnecting) {
                    if (isConnecting) {
                        sweepAngle.snapTo(0f)
                        sweepAngle.animateTo(360f, animationSpec = tween(1500, easing = androidx.compose.animation.core.LinearEasing))
                        onToggleConexionFast()
                        isConnecting = false
                    }
                }
                LaunchedEffect(conectadoFast) {
                    if (conectadoFast) {
                        wave1.snapTo(0f); wave2.snapTo(0f); wave3.snapTo(0f)
                        while (true) {
                            pulseScale.animateTo(1.15f, animationSpec = tween(800))
                            pulseScale.animateTo(1f, animationSpec = tween(800))
                        }
                    } else {
                        pulseScale.snapTo(1f)
                    }
                }
                LaunchedEffect(conectadoFast) {
                    if (!conectadoFast) {
                        while (true) {
                            wave1.snapTo(0f)
                            wave1.animateTo(1f, animationSpec = tween(3500, easing = androidx.compose.animation.core.LinearEasing))
                            kotlinx.coroutines.delay(400)
                            wave2.snapTo(0f)
                            wave2.animateTo(1f, animationSpec = tween(3500, easing = androidx.compose.animation.core.LinearEasing))
                            kotlinx.coroutines.delay(400)
                            wave3.snapTo(0f)
                            wave3.animateTo(1f, animationSpec = tween(3500, easing = androidx.compose.animation.core.LinearEasing))
                            kotlinx.coroutines.delay(400)
                        }
                    } else {
                        wave1.snapTo(0f); wave2.snapTo(0f); wave3.snapTo(0f)
                    }
                }

                val colorConectado = Color(0xFF00C853)
                val colorDesconectado = Color(0xFFFF1744)
                val colorActual = if (conectadoFast) colorConectado else colorDesconectado

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = if (mostarFast || state.serviceType.equals("PROFESSIONAL", ignoreCase = true)) "¡Bienvenido!" else "¡Vamos a trabajar!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (state.serviceType.equals("PROFESSIONAL", ignoreCase = true))
                                "Tienes 0 citas programadas para hoy"
                            else
                                "Tienes 0 trabajos programados para hoy",
                            fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    if(mostarFast) {

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                drawCircle(
                                    color = colorActual.copy(alpha = 0.18f),
                                    radius = (size.minDimension / 2) * pulseScale.value
                                )
                            }
                            Canvas(modifier = Modifier.size(64.dp)) {
                                drawCircle(
                                    color = colorActual.copy(alpha = 0.35f),
                                    radius = size.minDimension / 2 + 3.dp.toPx()
                                )
                                drawCircle(color = colorActual, radius = size.minDimension / 2)
                                if (!conectadoFast) {
                                    val maxRadius = size.minDimension / 2
                                    listOf(
                                        wave1.value,
                                        wave2.value,
                                        wave3.value
                                    ).forEach { progress ->
                                        drawCircle(
                                            color = Color(0xFFFFD600).copy(alpha = (1f - progress) * 0.55f),
                                            radius = maxRadius * progress
                                        )
                                    }
                                }
                                if (isConnecting) {
                                    drawArc(
                                        color = Color.White,
                                        startAngle = -90f,
                                        sweepAngle = sweepAngle.value,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 4.dp.toPx(),
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                        )
                                    )
                                }
                            }
                            androidx.compose.material3.IconButton(
                                onClick = { if (!isConnecting) isConnecting = true },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (conectadoFast) Icons.Default.Wifi else Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (conectadoFast) "EN\nLÍNEA" else "OFFLINE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } // fin header Box

        // Contenido scrolleable
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {

        Spacer(Modifier.height(16.dp))

        // CARRUSEL
        val isProfessional = state.serviceType.equals("PROFESSIONAL", ignoreCase = true)

                val cardSuscripcion = when (state.serviceType) {
                    "PROFESSIONAL" -> CarruselItem(
                        titulo = "Agenda Pro",
                        subtitulo = "Reservas online 24/7, recordatorios automaticos y estadisticas de tu agenda",
                        icono = Icons.Default.CalendarMonth,
                        gradiente = Brush.horizontalGradient(listOf(Color(0xFF5C6BCC0), Color(0xFF7986CB)))
                    )
                    "RENTAL" -> CarruselItem(
                        titulo = "Vitrina Premiun",
                        subtitulo = "Destaca tus publicaciones, sube hasta 20 fotos y ve cuantos te visitan.",
                        icono = Icons.Default.Star,
                        gradiente = Brush.horizontalGradient(listOf(Color(0xFF26A69A), Color(0xFF4DB6AC)))
                    )
                    "TECHNICAL" -> CarruselItem(
                        titulo = "Servicio Fast",
                        subtitulo = "Aparece primero en urgencias y recibe solicitudes de clientes cercanos.",
                        icono = Icons.Default.Bolt,
                        gradiente = Brush.horizontalGradient(listOf(Color(0xFFFF6B35), Color(0xFFE53935)))
                    )
                    else -> CarruselItem(
                        titulo = "Destaca tu servicio",
                        subtitulo = "Completá tu perfil al 100% para aparecer primero en las busquedas.",
                        icono = Icons.Default.TrendingUp,
                        gradiente = Brush.horizontalGradient(listOf(Color(0xFF7B4FD4),Color(0xFF4A90D9)))
                    )
                }

                val carruselItems = buildList {
                    add(
                        CarruselItem(
                            titulo = "Consigue mas clientes",
                            subtitulo = "Completa tu perfil a 100% hoy mismo.",
                            icono = Icons.Default.TrendingUp,
                            gradiente = Brush.horizontalGradient(listOf(Color(0xFF4A90D9), Color(0xDD7B4FD4)))
                        )
                    )
                    if (isProfessional) {
                        add(
                            CarruselItem(
                                titulo = "Configurá tu agenda",
                                subtitulo = "Definí horarios, duración y modalidad de antención.",
                                icono = Icons.Default.EventAvailable,
                                gradiente = Brush.horizontalGradient(listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5)))
                            )
                        )
                        add(
                            CarruselItem(
                                titulo = "Reducí ausencias",
                                subtitulo = "Enviá recordatorios y mantené tu agenda al día.",
                                icono = Icons.Default.NotificationsActive,
                                gradiente = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                            )
                        )
                    } else {
                        add(
                            CarruselItem(
                                titulo = "Activa tus promociones",
                                subtitulo = "Llega a mas usuarios con ofertas especiales.",
                                icono = Icons.Default.LocalOffer,
                                gradiente = Brush.horizontalGradient(listOf(Color(0xFFFF6B35), Color(0xFFFF8C42)))
                            )
                        )
                    }

                    add(cardSuscripcion)
                    add(
                        CarruselItem(
                            titulo = "Sin Anuncios",
                            subtitulo = "Experiencia limpia, sin banners ni interrupciones. Suscripción mensual.",
                            icono = Icons.Default.DoNotDisturb,
                            gradiente = Brush.horizontalGradient(listOf(Color(0xFF37474F), Color(0Xff546E7A)))
                        )
                    )
                }
        val pagerState = rememberPagerState(pageCount = { carruselItems.size })

        LaunchedEffect(pagerState) {
            while (true) {
                delay(4_000L)
                val nextPage = (pagerState.currentPage + 1) % carruselItems.size

                pagerState.animateScrollToPage(nextPage)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val item = carruselItems[page]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(item.gradiente)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    //Icono decorativo de fondo
                    Icon(
                        imageVector = item.icono,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterEnd)
                    )

                    //Contenido
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = item.icono,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .padding(6.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = item.titulo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.subtitulo,

                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Dots indicador
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(carruselItems.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (pagerState.currentPage == index) 20.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (pagerState.currentPage == index) colors.primaryOrange
                                else colors.primaryOrange.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // BOTÓN MIS PROMOCIONES (solo para no-profesionales)
        if (!isProfessional) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToPromotionList,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.primaryOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Mis Promociones",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // TU PROXIMO SERVICIO
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Tú proximo servicio",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(4.dp),
                onClick = onNavigateToCalendar
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(colors.primaryOrange.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = colors.primaryOrange, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = "Sin servicios pendientes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Toca para ver tu calendario",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        //SOLICITUDES RECIENTES
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solicitudes recientes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                TextButton(onClick = onNavigateToCalendar) {
                    Text("Ver todas", color = colors.primaryOrange, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin solicitudes pendientes",
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            Spacer(Modifier.height(20.dp))
        }

                if(mostarFast) {
                    //OPORTUNIDADES FAST
                    Spacer(Modifier.height(20.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (!conectadoFast) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.WifiOff,
                                        null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            "Estas desconectado",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            "Conectate para ver solicitude Fast", fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Oportunidades Fast",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        "Solicitudes urgentes cerca tuyo",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                IconButton(onClick = onRefreshOportunidades) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        null,
                                        tint = colors.primaryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } // cierra Row header
                            Spacer(Modifier.height(8.dp))
                            if (mensajeAceptar != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFF4CAF50
                                        ).copy(alpha = 0.15f)
                                    )
                                ) {
                                    Text(
                                        text = mensajeAceptar,
                                        modifier = Modifier.padding(12.dp),
                                        color = Color(0xFF4CAF50),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            if (oportunidadesLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = colors.primaryOrange)
                                }
                            } else if (oportunidades.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Sin solicitudes Fast por ahora",
                                            color = colors.textSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    oportunidades.take(3).forEach { op ->
                                        val accentColor =
                                            if (op.urgente) Color(0xFFE53935) else colors.primaryOrange
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                                            elevation = CardDefaults.cardElevation(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth()
                                                    .height(IntrinsicSize.Min)
                                            ) {
                                                // Franja de color izquierda
                                                Box(
                                                    modifier = Modifier
                                                        .width(5.dp)
                                                        .fillMaxHeight()
                                                        .background(
                                                            accentColor,
                                                            RoundedCornerShape(
                                                                topStart = 16.dp,
                                                                bottomStart = 16.dp
                                                            )
                                                        )
                                                )
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 12.dp,
                                                            vertical = 14.dp
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Columna icono + distancia
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.width(52.dp)
                                                    ) {
                                                        Icon(
                                                            if (op.urgente) Icons.Default.PriorityHigh else Icons.Default.Bolt,
                                                            null,
                                                            tint = accentColor,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                        Text(
                                                            text = if (op.distanciaKm > 0) "${
                                                                "%.1f".format(
                                                                    op.distanciaKm
                                                                )
                                                            } km" else "Cerca",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = accentColor
                                                        )
                                                    }
                                                    // Divisor
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(horizontal = 10.dp)
                                                            .width(1.dp)
                                                            .height(52.dp)
                                                            .background(
                                                                Color(0xFF424242).copy(alpha = 0.25f),
                                                                RoundedCornerShape(1.dp)
                                                            )
                                                    )
                                                    // Contenido
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = op.titulo,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = colors.textPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "Cliente: ${op.clienteNombre}",
                                                            fontSize = 13.sp,
                                                            color = colors.textSecondary
                                                        )
                                                        if (op.urgente) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    Icons.Default.PriorityHigh,
                                                                    null,
                                                                    tint = Color(0xFFE53935),
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Spacer(Modifier.width(3.dp))
                                                                Text(
                                                                    "URGENTE",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFFE53935)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    // Botón aceptar
                                                    Button(
                                                        onClick = { onAceptarOportunidad(op) },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = accentColor
                                                        ),
                                                        contentPadding = PaddingValues(
                                                            horizontal = 12.dp,
                                                            vertical = 6.dp
                                                        ),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Text(
                                                            "Aceptar",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } // cierra else conectadoFast
                    }
                }

        Spacer(Modifier.height(24.dp))
            } // fin Column scrolleable

            // Sombra fija sobre el contenido (se anima al hacer scroll)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.18f * shadowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        } // fin Box contenedor
    }
}

private data class CarruselItem(
    val titulo: String,
    val subtitulo: String,
    val gradiente: Brush,
    val icono: ImageVector = Icons.Default.Star
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
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        }
    }
}

@Composable
private fun NuevaSolicitudFastDialog(
    solicitud: OportunidadItem,
    onAceptar: () -> Unit,
    onDescartar: () -> Unit,
    proximaCita: String? = null,
    restriccionHorario: String? = null,
    restriccionDistancia: String? = null,
    restriccionSolicitudActiva: String? = null,
    restriccionCitaEnCurso: String? = null
) {
    val colors = getPrestadorColors()
    val accentColor = if (solicitud.urgente) Color(0xFFE53935) else Color(0xFFF57C00)
    var segundos by remember(solicitud.id) { mutableStateOf(30) }
    val progreso = remember(solicitud.id) { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(solicitud.id) {
        progreso.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 30_000, easing = androidx.compose.animation.core.LinearEasing)
        )
    }

    LaunchedEffect(solicitud.id) {
        while (segundos > 0) {
            kotlinx.coroutines.delay(1000)
            segundos--
        }
        onDescartar()
    }

    Dialog(onDismissRequest = onDescartar) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.backgroundColor),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                //Header de color con contador
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accentColor, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (solicitud.urgente) "SOLICITUD URGENTE" else "NUEVA SOLICITUD FAST",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        //Contador circular
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.25f),
                                    CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$segundos",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                //Barra de progreso sincronizada con el contador
                LinearProgressIndicator(
                    progress = progreso.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.2f)
                )

                //Cuerpo
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = solicitud.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))

                        Spacer(Modifier.width(4.dp))

                        Text(solicitud.clienteNombre, fontSize = 13.sp, color = colors.textSecondary)
                    }

                    Spacer(Modifier.height(16.dp))

                    //Cajitas de info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        //Cajita Distancia
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = accentColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (solicitud.distanciaKm > 0) "%.1f km".format(solicitud.distanciaKm) else "__ km",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                                Text("DISTANCIA", fontSize = 10.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                            }
                        }

                        //Cajita Categori
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Build, null, tint = accentColor, modifier = Modifier.size(20.dp))

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = solicitud.categoria.ifBlank { "General" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("CATEGORÍA", fontSize = 10.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // Aviso de restricciones - uno por cada problema detectado
                    val hayRestriccion = proximaCita != null || restriccionHorario != null || restriccionDistancia != null || restriccionSolicitudActiva != null || restriccionCitaEnCurso != null
                    val restricciones = listOfNotNull(
                        restriccionCitaEnCurso,
                        restriccionSolicitudActiva,
                        restriccionDistancia,
                        restriccionHorario,
                        if (proximaCita != null) "Tenés una cita a las $proximaCita. No podés aceptar." else
                        null
                    )
                    if (restricciones.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            restricciones.forEach { mensaje ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, null, tint = Color(0xFFF57C00), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = mensaje,
                                            fontSize = 12.sp,
                                            color = Color(0xFF7B4F00),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    //Botones
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDescartar,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("Omitir", fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = onAceptar,
                            enabled = !hayRestriccion,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("Aceptar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
