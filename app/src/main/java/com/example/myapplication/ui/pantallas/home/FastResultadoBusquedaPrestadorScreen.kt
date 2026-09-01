/*
package com.example.myapplication.ui.pantallas.home

import android.util.Log
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.ui.componentes.sistema.contexto.MoldeTarjetaPerfilDirec
import com.example.myapplication.uishared.ui.components.*
import com.example.myapplication.ui.componentes.sistema.AppTacticalButton
import com.example.myapplication.viewmodel.home.FastResultadoBusquedaPrestadorViewModel
import com.example.myapplication.viewmodel.home.ProviderWithDistance
import com.example.myapplication.viewmodel.home.FastFilterState
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.viewmodel.home.CategoryViewModel
import com.example.myapplication.viewmodel.home.UbicacionGpsObrero
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import kotlin.math.*

// ==========================================================================================
// --- PANTALLA FAST RESULTADO BÚSQUEDA PRESTADOR (v2026.ELITE) ---
// ==========================================================================================

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FastResultadoBusquedaPrestadorScreen(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    fastViewModel: FastResultadoBusquedaPrestadorViewModel = hiltViewModel(),
    beViewModel: BeCerebroViewModel = hiltViewModel(),
    beArchitectViewModel: BeCuerpoViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionGpsObrero = hiltViewModel(),
    userViewModel: ArmadorUsuarioViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by fastViewModel.uiState.collectAsStateWithLifecycle()
    val accountState by userViewModel.ecosistemaMaestro.collectAsStateWithLifecycle()
    val direccionActiva by ubicacionObrero.direccionActiva.collectAsStateWithLifecycle(null)
    val availableAddresses by beViewModel.coordinador.informacionDireccionesDisponibles.collectAsStateWithLifecycle(emptyList())
    val nombrePerfilActivo by userViewModel.nombrePerfilActivo.collectAsStateWithLifecycle()
    val fotoPerfilActivo by userViewModel.fotoPerfilActiva.collectAsStateWithLifecycle()
    val idPerfilSeleccionado by beViewModel.coordinador.idPerfilSeleccionado.collectAsStateWithLifecycle()
    val todasLasCategorias by categoryViewModel.todasLasCategorias.collectAsStateWithLifecycle()
    val categoriasOrdenadas by categoryViewModel.categoriasOrdenadas.collectAsStateWithLifecycle()

    val idDireccionSeleccionada by beViewModel.coordinador.idDireccionSeleccionada.collectAsStateWithLifecycle() 
    val consultaBusquedaBe by beArchitectViewModel.beBusquedaMotor.consultaCruda.collectAsStateWithLifecycle()
    val estaBusquedaBeActiva by beArchitectViewModel.beBusquedaMotor.estaBusquedaActiva.collectAsStateWithLifecycle()
    val estaGpsActivado by beViewModel.coordinador.estaGpsActivado.collectAsStateWithLifecycle()
    val isCargandoUbi by ubicacionObrero.estaCargando.collectAsStateWithLifecycle() // 🔥 [NEW]
    val fastHistory by fastViewModel.fastHistory.collectAsStateWithLifecycle()
    val shortcuts by fastViewModel.shortcuts.collectAsStateWithLifecycle()

   // val beConfig = remember {
       // com.example.myapplication.ui.componentes.be.modelos.ContextoHUD.FAST_SCREEN.crearConfiguracionBase(
       //     mensajes = listOf(com.example.myapplication.ui.componentes.be.modelos.MensajeBe("⚡", "Búsqueda táctica activada. Solo unidades de respuesta inmediata.", null, androidx.compose.ui.graphics.Color(0xFF22D3EE))),
       //     pistaBusqueda = "BUSCA POR NOMBRE O EMPRESA..."
       // ).copy(primarias = listOf("share"))
   // }

  //  DisposableEffect(Unit) {
        // 🔥 [ELITE]: En pantallas de sub-nivel, usamos registrar/remover para respetar el mapa.
       // beViewModel.navCoordinador.registrarPantalla(beConfig)
       // onDispose {
      //      beViewModel.navCoordinador.removerPantalla(beConfig.id)
       // }
  //  }

    LaunchedEffect(estaBusquedaBeActiva) {
        Log.d("FAST_SCREEN", "🔎 [BE_SEARCH_STATE] Activo: $estaBusquedaBeActiva")
        fastViewModel.setBeSearchActive(estaBusquedaBeActiva)
    }

    LaunchedEffect(direccionActiva) {
        Log.d("FAST_SCREEN", "📍 [LOCATION_CHANGE] Nueva dirección activa: ${direccionActiva?.aTextoCorto()}")
        uiState.selectedCategory?.let { category ->
            fastViewModel.startSearch(category)
        } ?: run {
            if (uiState.isSearching || uiState.searchFinished) {
                fastViewModel.resetSearch()
            }
        }
    }

    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            if (actionId == "refresh_gps") {
                if (estaGpsActivado) {
                    ubicacionObrero.ejecutarCalculoUbicacionGps(context, mostrarAvisos = false)
                } else {
                    Toast.makeText(context, "⚠️ El GPS está desactivado.", Toast.LENGTH_SHORT).show()
                }
            } 
        }
    }

    FastResultadoBusquedaPrestadorContent(
        navController = navController,
        bottomPadding = bottomPadding,
        isSearching = uiState.isSearching,
        searchFinished = uiState.searchFinished,
        searchResults = uiState.searchResults,
        direccionActiva = direccionActiva,
        availableAddresses = availableAddresses,
        usuario = accountState,
        nombrePerfilActivo = nombrePerfilActivo,
        fotoPerfilActivo = fotoPerfilActivo,
        idPerfilSeleccionado = idPerfilSeleccionado,
        estaGpsActivado = idDireccionSeleccionada == "gps_current", // 🔥 [FIX]
        isCargandoUbicacion = isCargandoUbi, 
        fastHistory = fastHistory,
        selectedCategory = uiState.selectedCategory,
        isBeSearchActive = uiState.isBeSearchActive,
        beSearchCategories = uiState.beSearchCategories,
        filters = uiState.filters,
        onAddressSelected = { addr -> ubicacionObrero.seleccionarDireccion(addr.id) },
        onGpsToggle = { ubicacionObrero.toggleGps(context) },
        onProfileSelected = { branchId -> userViewModel.seleccionarPerfil(branchId) },
        onCategoryClick = { category -> fastViewModel.selectCategory(category) },
        onToggleFilter = { id -> fastViewModel.toggleFilter(id) },
        onResetSearch = { fastViewModel.resetSearch() },
        onStartSearch = { cat -> fastViewModel.startSearch(cat) }
    )
}

@Composable
fun FastResultadoBusquedaPrestadorContent(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    isSearching: Boolean,
    searchFinished: Boolean,
    searchResults: List<ProviderWithDistance>,
    direccionActiva: DireccionDominio?,
    availableAddresses: List<DireccionDominio>,
    usuario: CuentaMaestroUsuario?,
    nombrePerfilActivo: String,
    fotoPerfilActivo: Any?,
    idPerfilSeleccionado: String?,
    estaGpsActivado: Boolean,
    isCargandoUbicacion: Boolean = false, // 🔥 [NEW]
    fastHistory: List<CategoriaEntity>,
    selectedCategory: CategoriaEntity?,
    isBeSearchActive: Boolean,
    beSearchCategories: List<CategoriaEntity>,
    filters: FastFilterState,
    onAddressSelected: (DireccionDominio) -> Unit,
    onGpsToggle: () -> Unit = {},
    onProfileSelected: (String?) -> Unit,
    onCategoryClick: (CategoriaEntity) -> Unit,
    onToggleFilter: (String) -> Unit,
    onResetSearch: () -> Unit,
    onStartSearch: (CategoriaEntity?) -> Unit
) {
    var radarScale by remember { mutableStateOf(1f) }
    var mostrarMenuPerfil by remember { mutableStateOf(false) }
    var mostrarMenuUbicacion by remember { mutableStateOf(false) }
    var selectedProviderOnRadar by remember { mutableStateOf<ProviderWithDistance?>(null) }

    var showAd by remember { mutableStateOf(false) }

    val cyberSheetMinHeight = 120.dp
    val cyberSheetExpandedHeight = 320.dp
    val currentCyberSheetHeight by animateDpAsState(
        targetValue = if (!isSearching && !searchFinished && !isBeSearchActive) cyberSheetExpandedHeight else cyberSheetMinHeight,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "cyberSheetHeight"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A))) {
        TacticalMapBackground(
            isSearching = isSearching,
            searchFinished = searchFinished,
            results = searchResults,
            userLat = direccionActiva?.latitud ?: -26.8310,
            userLon = direccionActiva?.longitud ?: -65.2045,
            scale = radarScale,
            onScaleChange = { zoom -> radarScale = (radarScale * zoom).coerceIn(0.5f, 3f) },
            onProviderClick = { selectedProviderOnRadar = it }
        )

        // 🔥 [ELITE]: Animación de pulso de radar mientras busca
        if (isSearching) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                RadarPulse(0)
                RadarPulse(1000)
                RadarPulse(2000)
            }
        }

        Column(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().zIndex(999f)) {
          //  BarraCabezera(title = "Urgencias", subtitle = "", emoji = "⚡", onBack = { navController.popBackStack() }, accentColor = Color(0xFFFF7043))
            MoldeTarjetaPerfilDirec(
                usuario = usuario,
                nombrePerfilActivo = nombrePerfilActivo,
                fotoPerfilActivo = fotoPerfilActivo,
                direccionActiva = direccionActiva,
                estaGpsActivo = estaGpsActivado && direccionActiva?.id == "gps_current",
                isCargandoUbicacion = isCargandoUbicacion, // 🔥 [FIX]
                alHacerClickPerfil = { mostrarMenuPerfil = !mostrarMenuPerfil; mostrarMenuUbicacion = false },
                alHacerClickUbicacion = { mostrarMenuUbicacion = !mostrarMenuUbicacion; mostrarMenuPerfil = false },
                alAlternarGps = onGpsToggle,
                alSeleccionarDireccion = { addr -> onAddressSelected(addr); mostrarMenuUbicacion = false },
                alSeleccionarPerfil = { userId, branchId -> onProfileSelected(branchId ?: userId); mostrarMenuPerfil = false },
                mostrarMenuPerfil = mostrarMenuPerfil,
                mostrarMenuUbicacion = mostrarMenuUbicacion,
                alOcultarMenu = { mostrarMenuPerfil = false; mostrarMenuUbicacion = false },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        CyberSheet(modifier = Modifier.align(Alignment.BottomCenter).zIndex(30f).heightIn(min = currentCyberSheetHeight)) {
            Text("SISTEMA DE RESPUESTA RÁPIDA", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filterOptions = remember {
                    listOf(BeDictionary.Filters["filter_online"], BeDictionary.Filters["filter_chat_24h"], BeDictionary.Filters["filter_chat_sub"], BeDictionary.Filters["filter_chat_local"]).filterNotNull()
                }
                filterOptions.forEach { item ->
                    val isSelected = when(item.id) {
                        "filter_online" -> filters.estaOnline
                        "filter_chat_24h" -> filters.atiende24h
                        "filter_chat_sub" -> filters.estaSuscrito
                        "filter_chat_local" -> filters.tieneLocalFisico
                        else -> false
                    }
                    Surface(onClick = { onToggleFilter(item.id) }, color = if (isSelected) item.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, if (isSelected) item.color else Color.White.copy(alpha = 0.1f))) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.emoji ?: "🔹", fontSize = 14.sp)
                            Text(item.label.uppercase(), color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            if (!isSearching && !searchFinished && !isBeSearchActive) {
                Spacer(Modifier.height(16.dp))
                val emergencyCategories = fastHistory
                Text("SERVICIOS DE EMERGENCIA", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    emergencyCategories.forEach { category ->
                        Box(modifier = Modifier.size(80.dp).background(Color.White.copy(0.05f), RoundedCornerShape(8.dp)).clickable { onCategoryClick(category) }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(category.icono, fontSize = 24.sp)
                                Text(category.nombre, fontSize = 8.sp, color = Color.White, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        AppTacticalButton(
            onClick = {
                if (isSearching || searchFinished) onResetSearch()
                else showAd = true
            },
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 14.dp).size(76.dp).zIndex(45f),
            accentColor = if (isSearching || searchFinished) Color(0xFFEF4444) else Color(0xFF22D3EE)
        ) {
            Icon(imageVector = if (isSearching || searchFinished) Icons.Default.Close else Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }

        RewardedInterstitialVideoAd(show = showAd, onRewardEarned = { showAd = false; onStartSearch(selectedCategory) }, onDismiss = { showAd = false })
    }
}

@Composable
fun TacticalMapBackground(isSearching: Boolean, searchFinished: Boolean, results: List<ProviderWithDistance>, userLat: Double, userLon: Double, scale: Float, onScaleChange: (Float) -> Unit, onProviderClick: (ProviderWithDistance) -> Unit) {
    val gridColor = if (isSearching) Color(0xFF22D3EE).copy(0.1f) else Color(0xFF1A1F26)
    val transformState = rememberTransformableState { zoomChange, _, _ -> onScaleChange(zoomChange) }
    Box(modifier = Modifier.fillMaxSize().transformable(state = transformState).drawBehind {
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
        for (y in 0..size.height.toInt() step step.toInt()) drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
    }) {
        if (searchFinished) {
            results.forEach { data ->
                val bearing = calculateBearing(userLat, userLon, data.lat, data.lon)
                val angleRadians = Math.toRadians(bearing - 90.0)
                val offsetX = (cos(angleRadians) * 100.0).toFloat()
                val offsetY = (sin(angleRadians) * 100.0).toFloat()
                Surface(modifier = Modifier.align(Alignment.Center).offset(x = offsetX.dp, y = offsetY.dp).size(48.dp).clickable { onProviderClick(data) }, shape = CircleShape, border = BorderStroke(2.dp, Color(0xFF00FFC2))) {
                    val foto = (data.service.urlMiniatura ?: data.service.urlFoto) as? String
                    AsyncImage(model = ImageUtils.processImageSource(foto), contentDescription = null, contentScale = ContentScale.Crop)
                }
            }
        }
        Surface(modifier = Modifier.size(40.dp).align(Alignment.Center), shape = CircleShape, color = Color(0xFF22D3EE), border = BorderStroke(4.dp, Color(0xFF05070A))) {
            Icon(Icons.Default.Navigation, null, modifier = Modifier.padding(8.dp), tint = Color(0xFF05070A))
        }
    }
}

fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val phi1 = Math.toRadians(lat1); val phi2 = Math.toRadians(lat2)
    val deltaLambda = Math.toRadians(lon2 - lon1)
    return (Math.toDegrees(atan2(sin(deltaLambda) * cos(phi2), cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda))) + 360.0) % 360.0
}

@Composable
fun CyberSheet(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = modifier.fillMaxWidth().heightIn(min = 160.dp).background(Color.Black.copy(0.9f)).padding(20.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
fun RadarPulse(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val scale by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 4f, animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)), label = "scale")
    val animAlpha by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)), label = "alpha")
    Box(modifier = Modifier.size(150.dp).graphicsLayer { scaleX = scale; scaleY = scale; alpha = animAlpha }.border(2.dp, Color(0xFF22D3EE).copy(0.4f), CircleShape))
}
*/
