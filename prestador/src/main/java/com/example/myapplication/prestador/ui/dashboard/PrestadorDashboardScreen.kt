package com.example.myapplication.prestador.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.chat.PrestadorChatScreen
import com.example.myapplication.prestador.ui.calendar.PrestadorCalendarScreen
import com.example.myapplication.prestador.ui.presupuesto.PresupuestosScreen
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.myapplication.prestador.viewmodel.NotificacionesViewModel
import com.example.myapplication.prestador.ui.notifications.NotificacionesScreen
import com.example.myapplication.prestador.viewmodel.ChatViewModel



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDashboard() {
    // Preview sin ViewModel (no es posible testearlo con Hilt)
    // MaterialTheme {
    //     PrestadorDashboardScreen(...)
    // }
}

@Composable
fun PrestadorDashboardScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToPresupuesto: () -> Unit = {},
    onNavigateToPresupuestoCita: (appointmentId: String) -> Unit = {},
    onNavigateToPresupuestos: () -> Unit = {},
    onNavigateToPromotion: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToThemeDemo: () -> Unit = {},
  //  fastSimulationViewModel: com.example.myapplication.prestador.viewmodel.FastSimulationViewModel = hiltViewModel(),
    notificacionesViewModel: NotificacionesViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    onNavigateToClientePerfil: (clientId: String) -> Unit = {},
) {
    val colors = getPrestadorColors()
    var selectedTab by rememberSaveable { mutableStateOf(2) }
    var isInConversation by remember { mutableStateOf(false) }
    var targetChatUserId by remember { mutableStateOf<String?>(null) }
    var autoOpenCalendarInChat by remember { mutableStateOf(false) }
    var pendingRescheduleDate by remember { mutableStateOf("") }
    var pendingRescheduleTime by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var triggerCalendarCreate by remember { mutableStateOf(false) }
    val isProfessional = false // TODO: Obtener de perfil real
    val unreadCount by notificacionesViewModel.unreadCount.collectAsState()
    val unreadMessageCount by chatViewModel.totalUnreadCount.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (!isInConversation && selectedTab in listOf(1, 2)) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 2) onNavigateToPromotion()
                        else if (selectedTab == 1) triggerCalendarCreate = true
                    },
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            (scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200))) togetherWith
                            (scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)))
                        },
                        label = "fab_icon"
                    ) { tab ->
                        Icon(
                            imageVector = if (tab == 2) Icons.Filled.Campaign else Icons.Filled.Add,
                            contentDescription = if (tab == 2) "Crear promoción" else "Nueva cita",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Ocultar barra de navegación cuando se está en una conversación individual
            if (!(selectedTab == 3 && isInConversation)) {
                PrestadorBottomNavigationBar(
                    selectedTab = selectedTab,
                    isProfessional = isProfessional,
                    unreadCount = unreadCount,
                    unreadMessageCount = unreadMessageCount,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.backgroundColor)
        ) {
            // Animación suave al cambiar de tab
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300)) +
                        scaleOut(targetScale = 0.92f, animationSpec = tween(300))
                },
                label = "tab_transition"
            ) { currentTab ->
                // Contenido según el tab seleccionado
                when (currentTab) {
                    0 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            PresupuestoContent(
                                onNavigateToPresupuesto = onNavigateToPresupuesto,
                                onBackToHome = { selectedTab = 2 }
                            )
                        } else {
                            PresupuestoNotSupportedContent(onBackToHome = { selectedTab = 2 })
                        }
                    }
                    1 -> PrestadorCalendarScreen(
                        onBack = { selectedTab = 2 },
                        onNavigateToPresupuesto = onNavigateToPresupuestoCita,
                        triggerCreateDialog = triggerCalendarCreate,
                        onCreateDialogHandled = { triggerCalendarCreate = false },
                        onNavigateToClientePerfil = onNavigateToClientePerfil,
                        onNavigateToChat = { clientId, _, newDate, newTime, appointmentId ->
                            targetChatUserId = clientId
                            coroutineScope.launch {
                                if (appointmentId == "RESCHEDULE") {
                                    pendingRescheduleDate = newDate
                                    pendingRescheduleTime = newTime
                                    autoOpenCalendarInChat = true
                                }
                                delay(100)
                                selectedTab = 3
                                if (appointmentId == "RESCHEDULE") {
                                    delay(600)
                                    autoOpenCalendarInChat = false
                                }
                            }
                        },

                    )
                    2 -> InicioContent(
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToServiceConfig = onNavigateToServiceConfig,
                        onLogout = onLogout,
                        onNavigateToPromotionList = onNavigateToPromotionList,
                        onNavigateToThemeDemo = onNavigateToThemeDemo,
                        onNavigateToCalendar = { selectedTab = 1 },
                        onNavigateToPresupuesto = onNavigateToPresupuesto,
                        onNavigateToPresupuestos = onNavigateToPresupuestos,
                        onNavigateToChat = { clientId ->
                            targetChatUserId = clientId
                            selectedTab = 3
                        }
                    )
                    3 -> {
                        println("🔥 DASHBOARD: Renderizando tab de chat")
                        println("🔥 targetChatUserId actual: $targetChatUserId")
                        
                        // Efecto para manejar la navegación al chat específico
                        LaunchedEffect(targetChatUserId) {
                            if (targetChatUserId != null) {
                                println("🔥 LaunchedEffect: targetChatUserId = $targetChatUserId")
                                // Esperar un frame para que el chat se renderice
                                kotlinx.coroutines.delay(100)
                            }
                        }
                        
                        PrestadorChatScreen(
                            onBack = {
                                selectedTab = 2
                                targetChatUserId = null
                            },
                            onInConversationChange = { isInConversation = it },
                            onNavigateToClientePerfil = onNavigateToClientePerfil,
                            onNavigateToPresupuesto = onNavigateToPresupuesto,
                            initialChatUserId = targetChatUserId,
                            autoOpenCalendarDialog = autoOpenCalendarInChat,
                            rescheduleDate = pendingRescheduleDate,
                            rescheduleTime = pendingRescheduleTime
                        )
                    }
                    4 -> NotificacionesScreen(
                        onNavigateBack = { selectedTab = 2 },
                        onAccion = { route ->
                            if (route.startsWith("open_chat/")) {
                                targetChatUserId = route.removePrefix("open_chat/")
                                selectedTab = 3
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PrestadorBottomNavigationBar(
    selectedTab: Int,
    isProfessional: Boolean,
    unreadCount: Int,
    unreadMessageCount: Int = 0,
    onTabSelected: (Int) -> Unit
) {
    val colors = getPrestadorColors()
    // Animación para el botón central
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BottomAppBar(
        modifier = Modifier
            .navigationBarsPadding(), // AÑADIDO: Respeta el espacio de gestos
        containerColor = colors.surfaceColor,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp) // Altura del contenido
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Presupuesto (Extremo izquierdo)
            BottomNavItem(
                icon = Icons.Default.Edit,
                label = if (isProfessional) "Consulta" else "Presupuesto",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            // Calendario
            BottomNavItem(
                icon = Icons.Default.DateRange,
                label = "Calendario",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )

            // Inicio (Centro - Botón destacado)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(if (selectedTab == 2) pulseScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = { onTabSelected(2) },
                    containerColor = if (selectedTab == 2) {
                        colors.primaryOrange // Naranja intenso
                    } else {
                        colors.primaryOrange.copy(alpha = 0.7f) // Naranja medio
                    },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Chat
            BottomNavItem(
                icon = Icons.Default.Email,
                label = "Chat",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                showBadge = unreadMessageCount > 0,
                badgeCount = unreadMessageCount
            )

            // Notificaciones (Extremo derecha)
            BottomNavItem(
                icon = Icons.Default.Notifications,
                label = "Alertas",
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                showBadge = unreadCount > 0,
                badgeCount = unreadCount
            )
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showBadge: Boolean = false,
    badgeCount: Int = 0
) {
    val colors = getPrestadorColors()
    val selectedColor = colors.primaryOrange // Naranja
    val unselectedColor = colors.textSecondary // Gris

    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top, // Cambio: alineación superior
            modifier = Modifier
                .padding(top = 4.dp, bottom = 2.dp) // Menos padding
        ) {
            BadgedBox(
                badge = {
                    if (showBadge && badgeCount > 0) {
                        Badge(
                            containerColor = colors.primaryOrange,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp) // Más compacto
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) selectedColor else unselectedColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(0.dp)) // Sin espacio extra

            Text(
                text = label,
                fontSize = 10.sp, // Más pequeño
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) selectedColor else unselectedColor,
                maxLines = 1
            )
        }
    }
}

// ==================== CONTENIDO DE CADA TAB ====================

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresupuestoContent(
    onNavigateToPresupuesto: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    // Mostrar directamente la lista de presupuestos sin TopBar
    PresupuestosScreen(
        onBack = onBackToHome, // Regresar al tab de Inicio
        onCrearNuevo = onNavigateToPresupuesto, // Navegar a crear presupuesto
        onVerDetalle = { presupuesto ->
            // TODO: Navegar a detalle
        },
        showTopBar = false // Ocultar TopBar porque ya está en el dashboard
    )
}

@Composable
private fun PresupuestoNotSupportedContent(onBackToHome: () -> Unit) {
    val colors = getPrestadorColors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Presupuestos no disponible en este Android",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Requiere Android 8.0 (API 26) o superior.",
            fontSize = 14.sp,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBackToHome) {
            Text("Volver")
        }
    }
}


@Composable
fun CalendarioContent() {
    val colors = getPrestadorColors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Calendario",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryOrange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestiona tus citas y horarios",
            fontSize = 16.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
fun ChatContent(unreadMessagesCount: Int = 0) {
    val colors = getPrestadorColors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chat",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryOrange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Conversa con tus clientes",
            fontSize = 16.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badge de mensajes pendientes
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.error.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (unreadMessagesCount > 0) "Tienes $unreadMessagesCount mensajes(s) sin leer" else "No tienes mensajes sin leer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.error
                )
            }
        }
    }
}

@Composable
fun NotificacionesContent() {
    val colors = getPrestadorColors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Notificaciones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryOrange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mantente al día con alertas importantes",
            fontSize = 16.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badge de notificaciones pendientes
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.primaryOrange.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tienes 5 notificaciones nuevas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primaryOrange
                )
            }
        }
    }
}
