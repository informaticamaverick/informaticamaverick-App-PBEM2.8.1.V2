package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.animation.*
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
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.uishared.ui.components.profile.parts.*
import kotlinx.coroutines.launch

/**
 * --- LIENZO ESTRUCTURAL DEL PERFIL DE USUARIO (Elite v2026) ---
 * [PROPÓSITO]: Implementar el carrusel de identidades (Personal + Corporativo) para el cliente.
 * [LEY #10]: Reutilización de piezas jerárquicas con foco en la soberanía de datos.
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UsuarioPerfilLienzo(
    identidadPrincipal: PrestadorDominio,
    identidadesHijas: List<PrestadorDominio> = emptyList(),
    todasLasCategorias: List<CategoriaDominio> = emptyList(),
    esMiPropioPerfil: Boolean = false,
    estaCargando: Boolean = false,
    enModoEdicion: Boolean = false,
    hayCambiosPendientes: Boolean = false,
    
    alVolver: () -> Unit = {},
    alActualizar: () -> Unit = {},
    alSyncCloud: () -> Unit = {},
    alCerrarSesion: () -> Unit = {},
    alEditarAvatar: () -> Unit = {},
    alChat: () -> Unit = {},
    alGuardarCambios: (PrestadorDominio) -> Unit = {},
    alNavegarAConfiguracion: () -> Unit = {},
    alActualizarDireccion: (DireccionDominio) -> Unit = {},
    alEliminarDireccion: (DireccionDominio) -> Unit = {},
    alAnadirEmpresa: (Triple<EmpresaDominio, SucursalDominio, DireccionDominio>) -> Unit = { },
    alAnadirSucursal: (String, SucursalDominio, DireccionDominio) -> Unit = { _, _, _ -> },
    alEliminarIdentidad: (String, String) -> Unit = { _, _ -> },
    estaDetectandoGps: Boolean = false,
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    var mostrarCerrarSesion by remember { mutableStateOf(false) }
    var direccionEnEdicion by remember { mutableStateOf<DireccionDominio?>(null) }
    var mostrarSheetEmpresa by remember { mutableStateOf(false) }
    var mostrarSheetSucursalParaEmpresaId by remember { mutableStateOf<String?>(null) }
    
    val identidadesRaiz = remember(identidadPrincipal, identidadesHijas) {
        (listOf(identidadPrincipal) + identidadesHijas.filter { it.idEmpresa == null && it.tipo == TipoPrestador.EMPRESA })
    }
    val pagerState = rememberPagerState(pageCount = { identidadesRaiz.size })

    val alturaHeaderMax = 400.dp
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
            if (esMiPropioPerfil) {
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
                    alClickChat = { alChat() }
                )
            } else {
                FloatingActionButton(
                    onClick = { alChat() },
                    containerColor = Color(0xFF3B82F6),
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.White)
                }
            }
        }
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
                    Spacer(modifier = Modifier.height(alturaHeaderMax + 24.dp))
                    
                    HorizontalPager(
                        state = pagerState, 
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 500.dp), 
                        verticalAlignment = Alignment.Top, 
                        userScrollEnabled = !enModoEdicion
                    ) { pagina ->
                        val identidadActual = identidadesRaiz[pagina]
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                             SeccionPerfilMaestroMav(
                                identidad = identidadActual,
                                todasLasEntidades = identidadesRaiz,
                                esMiPropioPerfil = esMiPropioPerfil,
                                todasLasCategorias = todasLasCategorias,
                                alGuardarIdentidad = alGuardarCambios, 
                                alAñadirSucursal = { idEmpresa -> mostrarSheetSucursalParaEmpresaId = idEmpresa },
                                alEliminarIdentidad = { id, tipo -> alEliminarIdentidad(id, tipo) },
                                alAñadirEmpresa = { mostrarSheetEmpresa = true },
                                alAbrirEditorDireccion = { _, addr -> direccionEnEdicion = addr },
                                alEliminarDireccion = alEliminarDireccion
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(140.dp))
                }

                val identidadPaginada = identidadesRaiz[pagerState.currentPage]

                CabeceraUsuarioPerfilMav(
                    altura = alturaHeader,
                    fraccionColapso = fraccionColapso,
                    fotoUrl = identidadPaginada.urlFoto,
                    miniaturaUrl = identidadPaginada.urlMiniatura,
                    titulo = identidadPaginada.titulo,
                    subtitulo = identidadPaginada.subtitulo ?: "Cliente Maverick",
                    estaVerificado = identidadPaginada.estaVerificado,
                    estaOnline = identidadPaginada.estaOnline,
                    esMiPropioPerfil = esMiPropioPerfil,
                    estaSuscrito = identidadPaginada.estaSuscrito,
                    alVolver = alVolver,
                    alEditarAvatar = alEditarAvatar,
                    alCerrarSesion = { mostrarCerrarSesion = true },
                    enModoEdicion = enModoEdicion 
                )

                if (direccionEnEdicion != null) {
                    HojaEditorDireccionDominio(
                        direccion = direccionEnEdicion!!,
                        estaDetectandoGps = estaDetectandoGps,
                        alDetectarGps = alDetectarGps,
                        onDismiss = { direccionEnEdicion = null },
                        onSave = {
                            alActualizarDireccion(it)
                            direccionEnEdicion = null
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
}

































