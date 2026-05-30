package com.example.myapplication.prestador.ui.dashboard

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
import com.example.myapplication.prestador.ui.dashboard.components.*



@Composable
fun InicioContent(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCreatePromo: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToThemeDemo: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToPresupuesto: () -> Unit = {},
    onNavigateToCrearPrespuesto: () -> Unit = {},
    onNavigateToPresupuestos: () -> Unit = {},
    onNavigateToChat: (clientId: String) -> Unit = {},
    onCrearTurno: () -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToCalendarioConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val oportunidadesVM: OportunidadesViewModel = hiltViewModel()
    val oportunidades by oportunidadesVM.oportunidades.collectAsState()
    val oportunidadesLoading by oportunidadesVM.isLoading.collectAsState()
    val mensajeAceptar by oportunidadesVM.mensajeAceptar.collectAsState()
    val nuevaSolicitud by oportunidadesVM.nuevaSolicitud.collectAsState()
    val restriccionHorario by oportunidadesVM.restriccionHorario.collectAsState()
    val restriccionDistancia by oportunidadesVM.restriccionDistancia.collectAsState()
    val conectadoFast by oportunidadesVM.conectadoFast.collectAsState()
    val restriccionSolicitudActiva by oportunidadesVM.resticcionSolicitudActiva.collectAsState()
    val restriccionCitaEnCurso by oportunidadesVM.restriccionCitaEnCurso.collectAsState()

    InicioScreen(
        state = state,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToServiceConfig = onNavigateToServiceConfig,
        onLogout = onLogout,
        onNavigateToCalendar = onNavigateToCalendar,
        onCrearTurno = onCrearTurno,
        onNavigateToPresupuesto = onNavigateToPresupuesto,
        onNavigateToCrearPrespuesto,
        onNavigateToPresupuestos = onNavigateToPresupuestos,
        onNavigateToChat = onNavigateToChat,
        onCompletarCita = { _, _ -> },
        onCompletarTrabajoFast = { _, _ -> },
        onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
        onNavigateToCalendarioConfig = onNavigateToCalendarioConfig,
        onNavigateToApariencia = onNavigateToApariencia,
        onNavigateToNotificaciones = onNavigateToNotificaciones,
        onNavigateToTerminos = onNavigateToTerminos,
        onNavigateToPrivacidad = onNavigateToPrivacidad,
        onNavigateToAcercaDe = onNavigateToAcercaDe,
    )
}