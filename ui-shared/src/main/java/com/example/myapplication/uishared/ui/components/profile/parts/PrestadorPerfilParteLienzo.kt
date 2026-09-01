package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.uishared.ui.components.profile.parts.*
import kotlinx.coroutines.launch

/**
 * --- LIENZO ESTRUCTURAL DEL PERFIL (Ley #10) ---
 * [PROPÓSITO]: Define el Scaffold, Pager y Header dinámico del perfil profesional.
 */

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PrestadorPerfilLienzo(
    identidadPrincipal: PrestadorDominio,
    identidadesHijas: List<PrestadorDominio> = emptyList(),
    esMiPropioPerfil: Boolean = false,
    todasLasCategorias: List<CategoriaDominio> = emptyList(),
    estaCargando: Boolean = false,
    mostrarCheckGuardado: Boolean = false,
    
    alVolver: () -> Unit = {},
    alActualizar: () -> Unit = {},
    alGuardarCambios: (PrestadorDominio) -> Unit = {},
    alCerrarSesion: () -> Unit = {},
    alEditarAvatar: () -> Unit = {},
    alChat: (String?) -> Unit = {},
    alEliminarIdentidad: (String, String) -> Unit = { _, _ -> },
    alNavegarAConfiguracion: () -> Unit = {},
    alConfigurarHorarios: (String) -> Unit = {},
    alSyncCloud: () -> Unit = {},
    hayCambiosPendientes: Boolean = false,
    
    alAnadirEmpresa: (Triple<EmpresaDominio, SucursalDominio, DireccionDominio>) -> Unit = { },
    alAnadirSucursal: (String, SucursalDominio, DireccionDominio) -> Unit = { _, _, _ -> },
    alActualizarDireccion: (DireccionDominio) -> Unit = { },
    alEliminarDireccion: (DireccionDominio) -> Unit = { },
    estaDetectandoGps: Boolean = false,
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {},
    
    distintivoPremium: @Composable () -> Unit = {},
    contenidoExtra: @Composable ColumnScope.() -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    var mostrarCerrarSesion by remember { mutableStateOf(false) }
    var mostrarSheetDireccion by remember { mutableStateOf<DireccionDominio?>(null) }
    var mostrarSheetEmpresa by remember { mutableStateOf(false) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf<Pair<String, String>?>(null) }
    var mostrarSheetSuscripcion by remember { mutableStateOf(false) }
    var mostrarRevertirModoEmpresa by remember { mutableStateOf(false) }
    var mostrarSheetSucursalParaEmpresaId by remember { mutableStateOf<String?>(null) }
    var mostrarAvisoPriorizarEmpresa by remember { mutableStateOf(false) }
    var mostrarSheetReseñas by remember { mutableStateOf(false) }

    val identidadesRaiz = remember(identidadPrincipal, identidadesHijas) {
        (listOf(identidadPrincipal) + identidadesHijas.filter { it.idEmpresa == null && it.tipo == TipoPrestador.EMPRESA })
    }
    val pagerState = rememberPagerState(pageCount = { identidadesRaiz.size })

    val alturaHeaderMax = 400.dp // Aumentado para estilo Telegram inmersivo
    val alturaHeaderMin = 88.dp 
    val densidad = LocalDensity.current
    val maxScroll = with(densidad) { (alturaHeaderMax - alturaHeaderMin).toPx() }
    val fraccionColapso by remember { derivedStateOf { (scrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f) } }
    val alturaHeader by remember { derivedStateOf { alturaHeaderMax - (alturaHeaderMax - alturaHeaderMin) * fraccionColapso } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFF0F0F0F),
        floatingActionButton = {
            if (fraccionColapso < 0.5f) { // Ocultamos FAB si estamos colapsados para priorizar Toolbar
                FabInteraccionPerfil(
                    paginaActual = pagerState.currentPage,
                    empresas = identidadesHijas.filter { it.tipo == TipoPrestador.EMPRESA },
                    urlFotoPersonal = identidadPrincipal.urlMiniatura,
                    esMiPropioPerfil = esMiPropioPerfil,
                    hayCambiosPendientes = hayCambiosPendientes,
                    alSeleccionarPagina = { pagina -> coroutineScope.launch { pagerState.animateScrollToPage(pagina) } },
                    alClickConfig = alNavegarAConfiguracion,
                    alClickSync = alSyncCloud,
                    alClickAnadirEmpresa = { mostrarSheetEmpresa = true },
                    alClickChat = alChat
                )
            }
        },
        floatingActionButtonPosition = if (esMiPropioPerfil) FabPosition.Center else FabPosition.End
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = estaCargando,
            onRefresh = alActualizar,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            indicator = { PullToRefreshDefaults.Indicator(state = pullToRefreshState, isRefreshing = estaCargando, containerColor = Color(0xFF1A1A24), color = Color(0xFF3B82F6)) }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    // Contenido empieza justo donde termina la imagen expandida
                    Spacer(modifier = Modifier.height(alturaHeaderMax))
                    HorizontalPager(
                        state = pagerState, 
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 800.dp), 
                        verticalAlignment = Alignment.Top, 
                        userScrollEnabled = true
                    ) { pagina ->
                        val identidadActual = identidadesRaiz[pagina]
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                             SeccionPerfilMaestroMav(
                                identidad = identidadActual,
                                todasLasEntidades = identidadesRaiz,
                                esMiPropioPerfil = esMiPropioPerfil,
                                todasLasCategorias = todasLasCategorias,
                                alGuardarIdentidad = alGuardarCambios, 
                                alConfigurarHorarios = alConfigurarHorarios,
                                alDeshacerModoEmpresa = { mostrarRevertirModoEmpresa = true },
                                alAñadirSucursal = { idEmpresa -> mostrarSheetSucursalParaEmpresaId = idEmpresa },
                                alEliminarIdentidad = { id, tipo -> mostrarConfirmacionEliminar = id to tipo },
                                alAñadirEmpresa = { mostrarSheetEmpresa = true },
                                alAbrirEditorDireccion = { _, addr -> mostrarSheetDireccion = addr },
                                alEliminarDireccion = alEliminarDireccion
                            )
                        }
                    }
                    contenidoExtra()
                    Spacer(modifier = Modifier.height(140.dp))
                }

                val identidadPaginada = identidadesRaiz[pagerState.currentPage]
                
                CabeceraPerfilDinamica(
                    altura = alturaHeader,
                    fraccionColapso = fraccionColapso,
                    fotoUrl = identidadPaginada.urlFoto,
                    miniaturaUrl = identidadPaginada.urlMiniatura,
                    titulo = identidadPaginada.titulo,
                    subtitulo = identidadPaginada.subtitulo ?: "Miembro Maverick",
                    calificacion = identidadPaginada.reputacion,
                    estaVerificado = identidadPaginada.estaVerificado,
                    estaOnline = identidadPaginada.estaOnline,
                    esMiPropioPerfil = esMiPropioPerfil,
                    trabajosRealizados = identidadPaginada.trabajosRealizados,
                    totalResenas = identidadPaginada.totalReseñas,
                    estaSuscrito = identidadPaginada.estaSuscrito,
                    alVolver = alVolver,
                    alEditarAvatar = alEditarAvatar,
                    alCerrarSesion = { mostrarCerrarSesion = true },
                    alVerReseñas = { mostrarSheetReseñas = true },
                    distintivoPremium = distintivoPremium
                )
                
                if (mostrarCheckGuardado) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        NotificacionGuardadoMav()
                    }
                }
            }
        }
    }

    if (mostrarCerrarSesion) {
        DialogoConfirmacion(
            titulo = "¿CERRAR SESIÓN?",
            mensaje = "Tu sesión se cerrará de este dispositivo.",
            onConfirm = { 
                mostrarCerrarSesion = false
                alCerrarSesion()
            },
            onDismiss = { mostrarCerrarSesion = false }
        )
    }
    
    if (mostrarConfirmacionEliminar != null) {
        val (id, tipo) = mostrarConfirmacionEliminar!!
        DialogoConfirmacion(
            titulo = "¿ELIMINAR $tipo?",
            mensaje = "Esta acción no se puede deshacer.",
            onConfirm = { 
                alEliminarIdentidad(id, tipo)
                mostrarConfirmacionEliminar = null 
            },
            onDismiss = { mostrarConfirmacionEliminar = null }
        )
    }

    if (mostrarSheetSuscripcion) {
        DialogoSuscripcion(
            onDismiss = { mostrarSheetSuscripcion = false },
            onSuscribirse = { /* Billing logic */ }
        )
    }
    
    if (mostrarSheetDireccion != null) {
        HojaEditorDireccionDominio(
            direccion = mostrarSheetDireccion!!,
            estaDetectandoGps = estaDetectandoGps,
            alDetectarGps = alDetectarGps,
            onDismiss = { mostrarSheetDireccion = null },
            onSave = { 
                alActualizarDireccion(it)
                mostrarSheetDireccion = null 
            }
        )
    }
    
    if (mostrarSheetEmpresa) {
        HojaRegistroEmpresaMav(
            idPropietario = identidadPrincipal.idPropietario,
            todasLasCategorias = todasLasCategorias,
            estaDetectandoGps = estaDetectandoGps,
            alDetectarGps = alDetectarGps,
            onDismiss = { mostrarSheetEmpresa = false },
            onFinalizar = { e, s, d -> 
                alAnadirEmpresa(Triple(e, s, d))
                mostrarSheetEmpresa = false
            }
        )
    }

    if (mostrarSheetSucursalParaEmpresaId != null) {
        HojaRegistroSucursalMav(
            idEmpresaPadre = mostrarSheetSucursalParaEmpresaId!!,
            idPropietario = identidadPrincipal.idPropietario,
            estaDetectandoGps = estaDetectandoGps,
            alDetectarGps = alDetectarGps,
            onDismiss = { mostrarSheetSucursalParaEmpresaId = null },
            onFinalizar = { s, d -> 
                alAnadirSucursal(mostrarSheetSucursalParaEmpresaId!!, s, d)
                mostrarSheetSucursalParaEmpresaId = null
            }
        )
    }

    if (mostrarAvisoPriorizarEmpresa) {
        DialogoPriorizarEmpresa(
            onConfirm = { mostrarAvisoPriorizarEmpresa = false },
            onDismiss = { mostrarAvisoPriorizarEmpresa = false }
        )
    }

    if (mostrarRevertirModoEmpresa) {
        DialogoDesactivarEmpresa(
            onConfirm = { mostrarRevertirModoEmpresa = false },
            onDismiss = { mostrarRevertirModoEmpresa = false }
        )
    }

    if (mostrarSheetReseñas) {
        val identidadPaginada = identidadesRaiz[pagerState.currentPage]
        HojaReseñasPrestador(
            reseñas = identidadPaginada.reseñas,
            onDismiss = { mostrarSheetReseñas = false }
        )
    }
}

































