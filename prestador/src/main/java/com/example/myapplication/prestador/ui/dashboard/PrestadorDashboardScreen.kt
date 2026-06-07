package com.example.myapplication.prestador.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.ui.calendar.PrestadorCalendarScreen
import com.example.myapplication.prestador.ui.chat.PrestadorChatScreen
import com.example.myapplication.prestador.ui.notifications.NotificacionesScreen
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.chat.ChatViewModel
import com.example.myapplication.prestador.viewmodel.dashboard.NotificacionesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * --- PRESTADOR DASHBOARD (STATELESS UI) ---
 * Orquestador principal de la aplicación.
 * Sigue la Ley #1: La UI no toma decisiones, solo delega al ViewModel.
 */
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
    notificacionesViewModel: NotificacionesViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    onNavigateToClientePerfil: (clientId: String) -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToCalendarioConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
) {
    val colors = getPrestadorColors()
    var selectedTab by rememberSaveable { mutableIntStateOf(2) }
    var isInConversation by remember { mutableStateOf(false) }
    var targetChatUserId by remember { mutableStateOf<String?>(null) }
    var autoOpenCalendarInChat by remember { mutableStateOf(false) }
    var pendingRescheduleDate by remember { mutableStateOf("") }
    var pendingRescheduleTime by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var triggerCalendarCreate by remember { mutableStateOf(false) }
    
    val unreadCount by notificacionesViewModel.unreadCount.collectAsStateWithLifecycle(initialValue = 0)
    val unreadMessageCount by chatViewModel.totalUnreadCount.collectAsStateWithLifecycle(initialValue = 0)

    Scaffold(
        containerColor = colors.backgroundColor,
        floatingActionButton = {
            if (!isInConversation && selectedTab == 1) {
                DashboardFAB(onClick = { triggerCalendarCreate = true })
            }
        },
        bottomBar = {
            if (!isInConversation || selectedTab != 3) {
                PrestadorBottomNavigationBar(
                    selectedTab = selectedTab,
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
        ) {
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
                when (currentTab) {
                    0 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            PresupuestoContent(
                                onNavigateToPresupuesto = onNavigateToPresupuesto,
                                onBackToHome = { selectedTab = 2 }
                            )
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
                        }
                    )
                    2 -> InicioContent(
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToServiceConfig = onNavigateToServiceConfig,
                        onLogout = onLogout,
                        onNavigateToPromotionList = onNavigateToPromotionList,
                        onNavigateToThemeDemo = onNavigateToThemeDemo,
                        onNavigateToCalendar = { selectedTab = 1 },
                        onNavigateToCrearPresupuesto = { onNavigateToPresupuesto() },
                        onNavigateToPresupuestos = onNavigateToPresupuestos,
                        onNavigateToChat = { clientId ->
                            targetChatUserId = clientId
                            selectedTab = 3
                        },
                        onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
                        onNavigateToCalendarioConfig = onNavigateToCalendarioConfig,
                        onNavigateToApariencia = onNavigateToApariencia,
                        onNavigateToNotificaciones = onNavigateToNotificaciones,
                        onNavigateToTerminos = onNavigateToTerminos,
                        onNavigateToPrivacidad = onNavigateToPrivacidad,
                        onNavigateToAcercaDe = onNavigateToAcercaDe,
                    )
                    3 -> PrestadorChatScreen(
                        onBack = {
                            selectedTab = 2
                            targetChatUserId = null
                        },
                        onInConversationChange = { isInConversation = it },
                        onNavigateToClientePerfil = onNavigateToClientePerfil,
                        initialChatUserId = targetChatUserId,
                        autoOpenCalendarDialog = autoOpenCalendarInChat,
                        rescheduleDate = pendingRescheduleDate,
                        rescheduleTime = pendingRescheduleTime
                    )
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
private fun DashboardFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .shadow(6.dp, CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF7043), Color(0xFFFFCCBC))
                ),
                shape = CircleShape
            )
    ) {
        Icon(Icons.Filled.Add, "Acción rápida", Modifier.size(28.dp))
    }
}

@Composable
fun PrestadorBottomNavigationBar(
    selectedTab: Int,
    unreadCount: Int,
    unreadMessageCount: Int,
    onTabSelected: (Int) -> Unit
) {
    val colors = getPrestadorColors()
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceColor,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Edit, "Presupuesto", selectedTab == 0) { onTabSelected(0) }
            BottomNavItem(Icons.Default.DateRange, "Calendario", selectedTab == 1) { onTabSelected(1) }
            
            // Botón Central (Home)
            FloatingActionButton(
                onClick = { onTabSelected(2) },
                modifier = Modifier.size(52.dp),
                containerColor = colors.primaryOrange,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Home, "Inicio", tint = Color.White, modifier = Modifier.size(26.dp))
            }

            BottomNavItem(Icons.Default.Email, "Chat", selectedTab == 3, unreadMessageCount > 0, unreadMessageCount) { onTabSelected(3) }
            BottomNavItem(Icons.Default.Notifications, "Alertas", selectedTab == 4, unreadCount > 0, unreadCount) { onTabSelected(4) }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    showBadge: Boolean = false,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    val tint = if (isSelected) colors.primaryOrange else colors.textSecondary

    Column(
        modifier = Modifier.weight(1f).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BadgedBox(
            badge = {
                if (showBadge && badgeCount > 0) {
                    Badge(containerColor = Color.Red, contentColor = Color.White) {
                        Text(if (badgeCount > 9) "9+" else badgeCount.toString())
                    }
                }
            }
        ) {
            Icon(icon, label, tint = tint, modifier = Modifier.size(24.dp))
        }
        Text(label, fontSize = 10.sp, color = tint, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresupuestoContent(onNavigateToPresupuesto: () -> Unit, onBackToHome: () -> Unit) {
    com.example.myapplication.prestador.ui.presupuesto.PresupuestosScreen(
        onBack = onBackToHome,
        onCrearNuevo = onNavigateToPresupuesto,
        onVerDetalle = { },
        showTopBar = false
    )
}
