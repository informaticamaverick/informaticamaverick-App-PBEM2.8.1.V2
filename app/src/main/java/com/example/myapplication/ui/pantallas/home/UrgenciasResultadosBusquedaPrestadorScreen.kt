package com.example.myapplication.ui.pantallas.home

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorUbicacion
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.ui.componentes.be.ui.BeBurbujaInformativaHUD
import com.example.myapplication.ui.componentes.sistema.contexto.BurbujaFiltroElite
import com.example.myapplication.ui.componentes.sistema.contexto.ModeloBurbujaFiltro
import com.example.myapplication.ui.componentes.sistema.menu.v3.*
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.pantallas.home.componentes.UrgenciasResultadosBusquedaSheet
import com.example.myapplication.uishared.ui.components.RewardedInterstitialVideoAd
import com.example.myapplication.viewmodel.home.*
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import com.example.myapplication.core.utilidades.formatearTexto
import com.example.myapplication.core.utilidades.GeoUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * --- URGENCIAS RESULTADOS BUSQUEDA PRESTADOR (v2026.RADAR.PRO) ---
 * [PROPÓSITO]: Pantalla de respuesta inmediata (Urgencias) con visualización de Radar y Triaje.
 * [LEY #1]: Pantalla Tonta (En proceso de desacoplamiento).
 * [LEY #9]: Estándar Maverick en Español.
 * [LEY #10]: Anatomía Táctica Elite.
 */

object TacticalTheme {
    val Background = Color(0xFF05070A)
    val GlassCard = Color(0xE60B0F17)
    val GlassCyanCard = Color(0xE6081C24)
    val BorderCyan = Color(0x5922D3EE)
    val BorderDefault = Color(0x26FFFFFF)
    
    val Cyan = Color(0xFF22D3EE)
    val Emerald = Color(0xFF10B981)
    val Amber = Color(0xFFF59E0B)
    val Red = Color(0xFFEF4444)
    val Blue = Color(0xFF3B82F6)
    val DarkSurface = Color(0xFF0F172A)
    val TextMuted = Color(0xFF94A3B8)
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UrgenciasResultadosBusquedaPrestadorScreen(
    navController: NavHostController,
    viewModel: UrgenciasResultadosBusquedaViewModel = hiltViewModel(),
    beViewModel: BeCerebroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resultadosPaginados = viewModel.resultadosPaginados.collectAsLazyPagingItems()
    val prestadoresRadar by viewModel.resultadosRadar.collectAsStateWithLifecycle(emptyList())

    var selectedProviderForModal by remember { mutableStateOf<ProviderUiModel?>(null) }
    
    // --- ESTADOS DE PUBLICIDAD (v2026.ADS) ---
    var showAd by remember { mutableStateOf(false) }
    var pendingCategory by remember { mutableStateOf<CategoriaDominio?>(null) }

    // 🔥 [v2026.ELITE]: Soberanía HUD Urgencia
    val beConfig = remember {
        ContextoHUD.URGENCIA.crearConfiguracionBase(
            pistaBusqueda = "BUSCAR UNIDAD TÁCTICA..."
        ).copy(
            ocultarHerramientasSistemaBusqueda = true // 🔥 [FIX]: No mostrar teclado/cerrar
        )
    }

    DisposableEffect(Unit) {
        beViewModel.navCoordinador.registrarPantalla(beConfig)
        beViewModel.beBusquedaMotor.establecerEstaBusquedaActiva(true) // 🔥 Asistente en modo búsqueda fijo
        onDispose {
            beViewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    // 🔥 [v2026.ELITE]: Cierre Maestro en Back
    BackHandler {
        beViewModel.coordinador.ejecutarCierreMaestro()
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalTheme.Background)
    ) {
        when (uiState.viewMode) {
            ViewMode.RADAR -> {
                TacticalRadarCanvas(
                    searchMode = uiState.searchMode,
                    providers = prestadoresRadar,
                    fotoPerfil = uiState.fotoPerfil,
                    usuario = uiState.ecosistemaMaestro,
                    nombrePerfilActivo = uiState.nombrePerfilActivo,
                    mostrarMenuPerfil = uiState.mostrarMenuPerfil,
                    alAlternarMenuPerfil = { viewModel.alternarMenuPerfil(it) },
                    alSeleccionarPerfil = { userId, branchId ->
                        // viewModel.userViewModel.seleccionarPerfil(branchId ?: userId)
                    },
                    onProviderClick = { provider ->
                        selectedProviderForModal = provider
                    }
                )
            }
            ViewMode.LIST -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 180.dp, bottom = 220.dp)
                ) {
                    ProviderListPagingView(
                        providers = resultadosPaginados,
                        onProviderClick = { selectedProviderForModal = null } // 🔥 Pendiente mapeo a modal
                    )
                }
            }
            else -> {}
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp) 
        ) {
            // --- ESPACIO PARA ASISTENTE FIJO (GLOBAL) ---
            Spacer(modifier = Modifier.statusBarsPadding().height(72.dp))

            // --- BURBUJA DE CONVERSACIÓN BE (TÁCTICA - POPUP SOBERANO) ---
            BeBurbujaInformativaHUD(
                visible = uiState.searchMode == SearchMode.SEARCHING, // 🔥 [ELITE]: Solo mostrar durante el escaneo
                arrowOffset = 32.dp 
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = SharedPalette.ElectricCyan
                    )
                    Column {
                        Text(
                            text = "PROTOCOLO DE BÚSQUEDA ACTIVO",
                            color = SharedPalette.ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Buscando prestadores de servicios en red...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // --- TARJETA DE DIRECCIONES (SOLO UBICACIÓN) ---
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.alternarMenuUbicacion(true) },
                    shape = CutCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = SharedPalette.SurfaceDark,
                    border = BorderStroke(1.dp, SharedPalette.BorderCyanSoft),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp) 
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f).height(38.dp)) {
                            MoldeCabeceraSuperiorUbicacion(
                                direccion = uiState.direccionActiva,
                                onClick = { viewModel.alternarMenuUbicacion(true) }
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = SharedPalette.ElectricCyan.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // --- MENÚ TÁCTICO DE UBICACIÓN ---
                MenuUbicacionV3(
                    expanded = uiState.mostrarMenuUbicacion,
                    onDismissRequest = { viewModel.alternarMenuUbicacion(false) },
                    direccionActiva = uiState.direccionActiva,
                    direccionGpsActual = null,
                    estaGpsActivo = uiState.estaGpsActivo,
                    isCargando = uiState.isCargandoUbicacion,
                    direccionesDisponibles = uiState.ecosistemaMaestro?.usuario?.direcciones ?: emptyList(),
                    alAlternarGps = { /* */ },
                    alSeleccionarDireccion = {
                        viewModel.alternarMenuUbicacion(false)
                    },
                    alignment = Alignment.BottomCenter,
                    isCenteredOnScreen = true,
                    verticalOffset = (-10).dp
                )
            }

            // ViewSwitcherSegmentedBar eliminado
        }

        // --- CAPA DE HOJA TÁCTICA (NEW v2026.ELITE) ---
        UrgenciasResultadosBusquedaSheet(
            isVisible = true, // Siempre visible en esta pantalla, pero Molde maneja la animación
            onClose = { beViewModel.coordinador.ejecutarCierreMaestro() },
            activeFilters = uiState.filtrosActivos,
            rubrosVisibles = uiState.rubrosMasUsados,
            rubroSeleccionado = uiState.rubroSeleccionado, // 🔥 [v2026.ELITE]: Nuevo flujo 3 estados
            isCargando = uiState.isCargandoRubros,
            consultaBusqueda = uiState.consultaFiltro,
            onToggleFilter = { viewModel.alternarFiltroTactico(it) },
            onSelectCategory = { cat ->
                // 🔥 [v2026.ELITE]: Primero activamos el radar, luego mostramos el video
                viewModel.prepararProtocoloDeBusqueda(cat)
                beViewModel.beBusquedaMotor.actualizarConsulta("") 
                pendingCategory = cat
                showAd = true
            },
            onClear = {
                beViewModel.beBusquedaMotor.limpiarConsulta()
                viewModel.limpiarFiltrosTacticos()
                viewModel.resetearBusqueda() // 🔥 [FIX]: Resetear rubro seleccionado
            },
            interaccionHabilitada = uiState.searchMode != SearchMode.SEARCHING // 🔥 [FIX]: Habilitar en RESULTS e IDLE
        )

        // --- SISTEMA DE MONETIZACIÓN (GOOGLE ADS) ---
        RewardedInterstitialVideoAd(
            show = showAd,
            onRewardEarned = {
                showAd = false
                pendingCategory?.let { viewModel.seleccionarRubro(it) }
                pendingCategory = null
            },
            onDismiss = {
                showAd = false
                // Si el usuario cierra el video, igual permitimos la búsqueda por Ley de Respuesta Inmediata
                pendingCategory?.let { viewModel.seleccionarRubro(it) }
                pendingCategory = null
            }
        )

        uiState.toastMensaje?.let { (title, msg) ->
            TacticalToastNotification(
                title = title,
                message = msg,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 130.dp, start = 20.dp, end = 20.dp)
            )
        }

        selectedProviderForModal?.let { provider ->
            ProviderDetailModalSheet(
                provider = provider,
                onDismiss = { selectedProviderForModal = null },
                onContactClick = { type ->
                    selectedProviderForModal = null
                }
            )
        }
    }
}

@Composable
fun TacticalRadarCanvas(
    searchMode: SearchMode,
    providers: List<ProviderUiModel>,
    fotoPerfil: Any? = null,
    usuario: com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario? = null,
    nombrePerfilActivo: String = "",
    mostrarMenuPerfil: Boolean = false,
    alAlternarMenuPerfil: (Boolean) -> Unit = {},
    alSeleccionarPerfil: (String, String?) -> Unit = { _, _ -> },
    onProviderClick: (ProviderUiModel) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
        label = "Sweep"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // 1. Digital Grid (Punteada Táctica)
                val gridStep = 50.dp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 10f), 0f)
                val gridColor = if (searchMode == SearchMode.SEARCHING) TacticalTheme.Cyan.copy(0.08f) else Color(0xFF0E1420)
                
                for (x in 0..size.width.toInt() step gridStep.toInt()) {
                    drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f, pathEffect = dashEffect)
                }
                for (y in 0..size.height.toInt() step gridStep.toInt()) {
                    drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f, pathEffect = dashEffect)
                }

                // 2. Scanlines (Efecto Monitor Antiguo)
                val scanlineColor = Color.White.copy(alpha = 0.02f)
                for (y in 0..size.height.toInt() step 8) {
                    drawLine(scanlineColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // --- BRÚJULA TÁCTICA (NORTE) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .graphicsLayer { alpha = 0.5f },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = TacticalTheme.Cyan,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "N",
                color = TacticalTheme.Cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(TacticalTheme.Cyan, Color.Transparent)
                        )
                    )
            )
        }

        // Radar Advanced Canvas
        Canvas(modifier = Modifier.size(340.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radii = listOf(50.dp, 100.dp, 150.dp)
            val labels = listOf("1.0 KM", "2.5 KM", "5.0 KM")

            radii.forEachIndexed { index, radius ->
                val rPx = radius.toPx()
                // Anillos con dash sutil
                drawCircle(
                    color = TacticalTheme.Cyan.copy(0.12f),
                    radius = rPx,
                    center = center,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
                )
                
                // Etiquetas de Distancia
                drawText(
                    textMeasurer = textMeasurer,
                    text = labels[index],
                    topLeft = Offset(center.x + 4.dp.toPx(), center.y - rPx - 14.dp.toPx()),
                    style = androidx.compose.ui.text.TextStyle(
                        color = TacticalTheme.Cyan.copy(0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Marcas de Azimut (Grados)
            for (angle in 0 until 360 step 30) {
                val angleRad = Math.toRadians(angle.toDouble())
                val start = Offset(
                    (center.x + cos(angleRad) * 155.dp.toPx()).toFloat(),
                    (center.y + sin(angleRad) * 155.dp.toPx()).toFloat()
                )
                val end = Offset(
                    (center.x + cos(angleRad) * 165.dp.toPx()).toFloat(),
                    (center.y + sin(angleRad) * 165.dp.toPx()).toFloat()
                )
                drawLine(
                    color = TacticalTheme.Cyan.copy(alpha = 0.2f),
                    start = start,
                    end = end,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // --- BARRIDO DE RADAR PRO (Leading Edge + Tail) ---
            val alphaBarrido = if (searchMode == SearchMode.SEARCHING) 0.7f else 0.2f
            
            // 1. Estela de Gradiente
            drawArc(
                brush = Brush.sweepGradient(
                    0.75f to Color.Transparent,
                    1f to TacticalTheme.Cyan.copy(alpha = alphaBarrido * 0.4f)
                ),
                startAngle = sweepAngle - 90f,
                sweepAngle = 90f,
                useCenter = true,
                size = size
            )

            // 2. Filo Brillante (Leading Edge)
            val lineRad = Math.toRadians(sweepAngle.toDouble())
            drawLine(
                color = TacticalTheme.Cyan.copy(alpha = alphaBarrido),
                start = center,
                end = Offset(
                    (center.x + cos(lineRad) * (size.width / 2)).toFloat(),
                    (center.y + sin(lineRad) * (size.height / 2)).toFloat()
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center User GPS Pin
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Glow exterior circular
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = TacticalTheme.Cyan.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, TacticalTheme.Cyan.copy(alpha = 0.2f))
                ) {}

                // Imagen de perfil en el centro (HUB TÁCTICO)
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { alAlternarMenuPerfil(!mostrarMenuPerfil) },
                    shape = CircleShape,
                    color = TacticalTheme.DarkSurface,
                    border = BorderStroke(2.dp, TacticalTheme.Cyan)
                ) {
                    AsyncImage(
                        model = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(fotoPerfil),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                // Badge de GPS (Indicador táctico)
                Surface(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp),
                    shape = CircleShape,
                    color = TacticalTheme.Cyan,
                    border = BorderStroke(1.5.dp, TacticalTheme.Background)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                // --- MENÚ SOBERANO DE PERFIL ---
                MoldeMenuArmadorV3(
                    expanded = mostrarMenuPerfil,
                    onDismissRequest = { alAlternarMenuPerfil(false) },
                    alignment = Alignment.TopCenter,
                    isArrowBottom = true,
                    autoArrow = true,      
                    verticalOffset = 0.dp  
                ) {
                    MenuSectionHeaderV3("IDENTIDAD ACTIVA")

                    usuario?.let { u ->
                        MenuItemEliteV3(
                            label = u.usuario.perfil.nombreVisible.formatearTexto(),
                            isSelected = nombrePerfilActivo == u.usuario.perfil.nombreVisible,
                            onClick = {
                                alSeleccionarPerfil(u.usuario.perfil.id, null)
                                alAlternarMenuPerfil(false)
                            }
                        )
                    }

                    if (usuario != null && usuario.empresas.isNotEmpty()) {
                        usuario.empresas.forEach { company ->
                            company.sucursales.forEach { branch ->
                                MenuItemEliteV3(
                                    label = "${branch.sucursal.nombre} (${company.empresa.nombre})".formatearTexto(),
                                    isSelected = nombrePerfilActivo.contains(branch.sucursal.nombre),
                                    onClick = {
                                        alSeleccionarPerfil(company.empresa.id, branch.sucursal.id)
                                        alAlternarMenuPerfil(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = TacticalTheme.Background.copy(0.9f),
                border = BorderStroke(1.dp, TacticalTheme.BorderCyan)
            ) {
                Text(
                    "TÚ (ID MAVERICK)",
                    color = TacticalTheme.Cyan,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Provider Pins on Radar (Visible when Search Finished or Results State)
        if (searchMode == SearchMode.RESULTS) {
            if (providers.isEmpty()) {
                // --- ESTADO VACÍO TÁCTICO (v2026.ELITE) ---
                Column(
                    modifier = Modifier.padding(top = 160.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TacticalTheme.Red.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "NO HAY NADIE CERCA,\nDISCULPA LO VOLVEREMOS A INTENTAR",
                        color = TacticalTheme.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                providers.forEach { provider ->
                    // Calculamos ángulo del proveedor para el efecto Ping
                    val providerAngle = Math.toDegrees(atan2(provider.lonOffsetDp.toDouble(), provider.latOffsetDp.toDouble())).let {
                        if (it < 0) it + 360 else it
                    }
                    
                    // Diferencia angular con el barrido
                    val diff = (sweepAngle - providerAngle + 360) % 360
                    val isScanningOver = diff in 0.0..15.0

                    Box(
                        modifier = Modifier
                            .offset(x = provider.latOffsetDp.dp, y = provider.lonOffsetDp.dp)
                            .clickable { onProviderClick(provider) }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val esResaltado = provider.tags.contains("highlight")
                            val colorBordePin = if (esResaltado) TacticalTheme.Cyan else TacticalTheme.Emerald
                            
                            Box(contentAlignment = Alignment.Center) {
                                // Ping Effect (Halo)
                                if (isScanningOver) {
                                    Surface(
                                        modifier = Modifier.size(if (esResaltado) 64.dp else 54.dp),
                                        shape = CircleShape,
                                        color = colorBordePin.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, colorBordePin.copy(alpha = 0.5f))
                                    ) {}
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(if (esResaltado) 50.dp else 40.dp)
                                        .graphicsLayer {
                                            if (esResaltado) {
                                                scaleX = 1.05f; scaleY = 1.05f
                                            }
                                        },
                                    shape = CircleShape,
                                    color = TacticalTheme.DarkSurface,
                                    border = BorderStroke(2.dp, colorBordePin)
                                ) {
                                    AsyncImage(
                                        model = provider.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TacticalTheme.Background.copy(0.9f),
                                border = BorderStroke(1.dp, colorBordePin.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    "⚡ ${provider.distance}",
                                    color = colorBordePin,
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderListPagingView(
    providers: LazyPagingItems<PrestadorDominio>,
    onProviderClick: (PrestadorDominio) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${providers.itemCount} UNIDADES DETECTADAS", color = TacticalTheme.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Orden: Cercanía ⚡", color = TacticalTheme.TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }

        items(
            count = providers.itemCount,
            key = providers.itemKey { it.id }
        ) { index ->
            providers[index]?.let { provider ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onProviderClick(provider) },
                    shape = RoundedCornerShape(16.dp),
                    color = TacticalTheme.GlassCyanCard,
                    border = BorderStroke(1.dp, TacticalTheme.BorderCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(provider.urlMiniatura ?: provider.urlFoto),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(provider.titulo, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = TacticalTheme.Emerald.copy(0.2f)
                                    ) {
                                        Text(if (provider.estaVerificado) "VERIFICADO" else "DISPONIBLE", color = TacticalTheme.Emerald, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("★ ${provider.reputacion}", color = TacticalTheme.Amber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("•", color = TacticalTheme.TextMuted, fontSize = 10.sp)
                                    Text("${"%.1f".format(provider.distanciaKm ?: 0.0)} km", color = TacticalTheme.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("•", color = TacticalTheme.TextMuted, fontSize = 10.sp)
                                    Text("Llegada ~${GeoUtils.estimarMinutosLlegada(provider.distanciaKm ?: 0.0)} min", color = TacticalTheme.Emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("$ ---", color = TacticalTheme.Emerald, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("visita est.", color = TacticalTheme.TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = TacticalTheme.Cyan
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderDetailModalSheet(
    provider: ProviderUiModel,
    onDismiss: () -> Unit,
    onContactClick: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = TacticalTheme.GlassCyanCard,
                border = BorderStroke(1.dp, TacticalTheme.BorderCyan)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = provider.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                            )
                            Column {
                                Text(provider.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("★ ${provider.rating}", color = TacticalTheme.Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("•", color = TacticalTheme.TextMuted, fontSize = 10.sp)
                                    Text(provider.distance, color = TacticalTheme.Cyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TacticalTheme.TextMuted)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("LLEGADA EST.", "< ${provider.eta}", TacticalTheme.Emerald, Modifier.weight(1f))
                        StatBox("PRECIO BASE", provider.estPrice, TacticalTheme.Cyan, Modifier.weight(1f))
                        StatBox("ESTADO", "🟢 ONLINE", TacticalTheme.Emerald, Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onContactClick("Llamada") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalTheme.Emerald)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(6.dp))
                            Text("LLAMAR AHORA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Surface(
                            modifier = Modifier.size(44.dp).clickable { onContactClick("Chat") },
                            shape = RoundedCornerShape(14.dp),
                            color = TacticalTheme.DarkSurface,
                            border = BorderStroke(1.dp, TacticalTheme.BorderCyan)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(com.example.myapplication.uishared.estilos.AppIcons.Message, contentDescription = null, tint = TacticalTheme.Cyan)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = TacticalTheme.DarkSurface,
        border = BorderStroke(1.dp, TacticalTheme.BorderDefault)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TacticalTheme.TextMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
            Text(value, color = valueColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun TacticalToastNotification(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = TacticalTheme.GlassCyanCard,
        border = BorderStroke(1.dp, TacticalTheme.BorderCyan)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = TacticalTheme.Cyan.copy(0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⚡", fontSize = 14.sp)
                }
            }
            Column {
                Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(message, color = TacticalTheme.TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ==================================================================================
// --- 🧪 SECCIÓN DE PREVIEWS (LEY #10: MODO EDICIÓN) ---
// ==================================================================================

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Urgencias Screen - Radar Mode", device = "spec:width=411dp,height=891dp", showBackground = true)
@Composable
fun PreviewUrgenciasRadar() {
    PBEMTheme {
        UrgenciasResultadosBusquedaPrestadorScreen(navController = rememberNavController())
    }
}
