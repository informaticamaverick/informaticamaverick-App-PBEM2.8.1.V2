package com.example.myapplication.viewmodel.home

import android.util.Log
import android.content.pm.PackageManager
import android.os.Build
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.pantallas.home.*
import com.example.myapplication.ui.pantallas.home.componentes.SheetDetalleSuperCategoriaV3
import com.example.myapplication.ui.componentes.*
import com.example.myapplication.uishared.ui.modelos.AccordionBanner
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import androidx.compose.ui.unit.sp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- HOME SCREEN VIEWMODEL (EL COORDINADOR v2026.FINAL) ---
 * [PROPÓSITO]: Orquestar la pantalla de inicio y sincronizar los obreros en un ÚNICO ESTADO.
 * [LEY #1]: Pantalla Tonta. Centraliza la recolección de +15 flujos en uno solo.
 * [LEY #9]: Estándar Mav en Español.
 */
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    val coordinador: CoordinadorAcciones
) : ViewModel() {

    private val _estaRefrescando = MutableStateFlow(false)
    private val _mostrarMenuUbicacion = MutableStateFlow(false)
    private val _idsSeleccionados = MutableStateFlow(emptySet<String>())

    init {
        // 🔥 [ELITE REACTIVITY]: Sincronizar selección local con el estado global del Coordinador.
        // Si el Coordinador apaga la multiselección (ej: por navegación), limpiamos los IDs.
        viewModelScope.launch {
            coordinador.estaMultiseleccionActiva.collect { activa ->
                if (!activa) {
                    _idsSeleccionados.value = emptySet()
                }
            }
        }
    }

    /**
     * 🔥 [ELITE SSOT]: Recolector Maestro de Obreros.
     * Centraliza la recolección de flujos mediante una orquestación por grupos.
     */
    fun crearFlujoUiState(
        userVm: ArmadorUsuarioViewModel,
        ubicacionObrero: UbicacionGpsObrero,
        climaObrero: ClimaViewModel,
        categoryVm: CategoryViewModel,
        promoVm: PromoViewModel,
        beBrainVm: BeCerebroViewModel,
        beCuerpoVm: BeCuerpoViewModel?,
        favoritosVm: FavoritosViewModel,
        transicionFinalizada: Boolean
    ): Flow<HomeScreenUiState> {
        android.util.Log.d("MAV_HOME", "🧠 [UI_STATE_CREATE] Iniciando flujo SSOT | Transicion: $transicionFinalizada")

        // --- 1. GRUPO IDENTIDAD ---
        val flujoIdentidad = userVm.identidadesSoberanas.map { identidades ->
            val idPerfilActivo = coordinador.idPerfilSeleccionado.value ?: "personal"
            val perfilActual = identidades.find { it.id == idPerfilActivo } ?: identidades.firstOrNull()
            Triple(perfilActual, idPerfilActivo, identidades)
        }.distinctUntilChanged()

        // --- 2. GRUPO UBICACIÓN Y CLIMA ---
        val flujoContexto = combine(
            ubicacionObrero.direccionActiva,
            coordinador.modoGpsActivo,
            ubicacionObrero.estaCargando,
            coordinador.informacionDireccionesDisponibles,
            climaObrero.temperatura,
            climaObrero.emojiClima,
            climaObrero.descripcionClima,
            climaObrero.mostrarDetalles,
            _mostrarMenuUbicacion
        ) { args -> args }.distinctUntilChanged()

        // --- 3. GRUPO BANNERS (Optimizado) ---
        val flujoBanners = if (transicionFinalizada) {
            combine(categoryVm.uiState.map { it.categoriasPlanas }, promoVm.promotions) { cats, promos ->
                promoVm.generateHomeBanners(cats, promos)
            }
        } else flowOf(emptyList())

        // --- 4. COMBINACIÓN MAESTRA (LEY #10) ---
        return combine(
            flujoIdentidad,
            flujoContexto,
            categoryVm.uiState,
            _estaRefrescando,
            beBrainVm.navCoordinador.estaMenuLateralAbierto, // 🔥 [FIX]
            beCuerpoVm?.beBusquedaMotor?.estaBusquedaActiva ?: flowOf(false),
            beCuerpoVm?.beBusquedaMotor?.consultaCruda ?: flowOf(""),
            flujoBanners,
            coordinador.estaMultiseleccionActiva,
            _idsSeleccionados,
            favoritosVm.mostrarPanelFavoritos
        ) { args: Array<Any> ->
            val ident = args[0] as Triple<*, *, *>
            val ctx = args[1] as Array<Any>
            val catState = args[2] as CategoryUiState
            val refrescando = args[3] as Boolean
            val menuAbierto = args[4] as Boolean
            val buscando = args[5] as Boolean
            val consulta = args[6] as String
            val banners = args[7] as List<AccordionBanner>
            val multiActivo = (args[8] as Boolean) || catState.estaEnModoSeleccionSuper
            val seleccionados = (args[9] as Set<String>) + catState.idsSuperSeleccionados
            val favoritosAbierto = args[10] as Boolean

            Log.v("HOME_STATE_AUDIT", "🔄 [EMIT] Buscando: $buscando | Multi: $multiActivo | Seleccionados: ${seleccionados.size}")

            val perfilActual = ident.first as? PerfilIdentidadV3
            val idPerfilActivo = ident.second as? String ?: "personal"
            val dirActiva = ctx[0] as? DireccionDominio

            HomeScreenUiState(
                nombrePerfilActivo = perfilActual?.nombre ?: "Usuario",
                fotoPerfilActivo = perfilActual?.photoUrl,
                idPerfilSeleccionado = if (idPerfilActivo == "personal") null else idPerfilActivo,
                estaVerificado = perfilActual?.estaVerificado ?: false,
                esSuscripto = perfilActual?.esSuscripto ?: false,
                
                direccionActiva = dirActiva,
                estaGpsActivado = ctx[1] as Boolean,
                estaCargandoUbicacion = ctx[2] as Boolean,
                direccionesDisponibles = ctx[3] as List<DireccionDominio>,
                mostrarMenuUbicacion = ctx[8] as Boolean,
                
                temperatura = ctx[4] as String,
                emojiClima = ctx[5] as String,
                descripcionClima = ctx[6] as String,
                nombreCiudad = dirActiva?.localidad ?: "Detectando...",
                mostrarDetallesClima = ctx[7] as Boolean,
                
                categoriaState = catState,
                estaRefrescando = refrescando,
                
                estaMenuLateralAbierto = menuAbierto,
                estaPanelFavoritosAbierto = favoritosAbierto,
                estaBuscando = buscando,
                consultaBusqueda = consulta,
                itemsBanner = banners,
                modoMultiseleccion = multiActivo,
                idsSeleccionados = seleccionados
            )
        }
    }

    fun alRefrescar() {
        viewModelScope.launch {
            _estaRefrescando.value = true
            delay(1000.milliseconds)
            _estaRefrescando.value = false
        }
    }

    fun establecerMostrarMenuUbicacion(mostrar: Boolean) {
        _mostrarMenuUbicacion.value = mostrar
    }

    fun alternarSeleccionItem(id: String, totalItems: Int) {
        val actual = _idsSeleccionados.value.toMutableSet()
        if (actual.contains(id)) {
            actual.remove(id)
        } else {
            actual.add(id)
        }
        _idsSeleccionados.value = actual
        
        if (actual.isEmpty()) {
            coordinador.actualizarMultiseleccion(false)
            coordinador.actualizarTodoSeleccionado(false)
        } else {
            if (!coordinador.estaMultiseleccionActiva.value) {
                coordinador.actualizarMultiseleccion(true)
            }
            coordinador.actualizarTodoSeleccionado(actual.size >= totalItems)
        }
    }

    fun seleccionarTodo(ids: List<String>) {
        _idsSeleccionados.value = ids.toSet()
        coordinador.actualizarMultiseleccion(true)
        coordinador.actualizarTodoSeleccionado(true)
    }

    fun deseleccionarTodo() {
        _idsSeleccionados.value = emptySet()
        // 🔥 [v2026.ELITE]: No cerramos el modo, solo limpiamos la selección para permitir alternar.
        coordinador.actualizarTodoSeleccionado(false)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController,
    brainViewModel: BeCerebroViewModel = hiltViewModel(),
    assistantViewModel: BeCuerpoViewModel? = null,
    homeViewModel: HomeScreenViewModel = hiltViewModel()
) {
    Log.d("HOME_SCREEN", "🏠 [HOME_INIT] Orquestando HomeScreenComplete...")

    var sePuedenCargarObrerosPesados by remember { mutableStateOf(false) }
    val beConfig = remember {
        ContextoHUD.INICIO.crearConfiguracionBase(
            edicion = listOf("add_fav_multi", "remove_fav_multi", "select_all"),
            mensajes = emptyList(),
            pistaBusqueda = "¡DIME QUÉ RUBRO BUSCÁS! 🔍🛠️"
        )
    }
    
    val categoryVm: CategoryViewModel = hiltViewModel()

    DisposableEffect(Unit) {
        sePuedenCargarObrerosPesados = true
        // 🔥 [v2026.ELITE]: Al entrar a una pantalla raíz, reiniciamos el mapa de soberanía.
        brainViewModel.navCoordinador.reiniciarContextoHUD(ContextoHUD.INICIO)
        brainViewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose { 
            brainViewModel.navCoordinador.removerPantalla(beConfig.id)
            homeViewModel.deseleccionarTodo() // 🔥 [SANEAMIENTO]
            categoryVm.establecerModoSeleccionSuper(false) // 🔥 [SANEAMIENTO SUPER]
        }
    }

    val userVm: ArmadorUsuarioViewModel = hiltViewModel()
    val ubicacionObrero: UbicacionGpsObrero = hiltViewModel()
    val climaObrero: ClimaViewModel = hiltViewModel()
    val promoVm: PromoViewModel = hiltViewModel()
    val favoritosViewModel: FavoritosViewModel = hiltViewModel()

    val uiState by remember(sePuedenCargarObrerosPesados) {
        homeViewModel.crearFlujoUiState(userVm, ubicacionObrero, climaObrero, categoryVm, promoVm, brainViewModel, assistantViewModel, favoritosViewModel, sePuedenCargarObrerosPesados)
            .flowOn(Dispatchers.Default)
    }.collectAsStateWithLifecycle(HomeScreenUiState())

    // 🔥 [ELITE SOBERANÍA]: Sincronización del contrato Be basado en Multiselección
    LaunchedEffect(uiState.modoMultiseleccion, uiState.idsSeleccionados) {
        val sonTodosFavoritos = uiState.idsSeleccionados.isNotEmpty() && 
                                uiState.idsSeleccionados.all { uiState.categoriaState.idsFavoritos.contains(it) }
        
        val accionFavorito = if (sonTodosFavoritos) "remove_fav_multi" else "add_fav_multi"

        val config = ContextoHUD.INICIO.crearConfiguracionBase(
            navegacion = if (uiState.modoMultiseleccion) listOf(accionFavorito) else emptyList(),
            edicion = if (uiState.modoMultiseleccion) listOf("select_all", "cancel") else emptyList(),
            mensajes = emptyList()
        ).copy(
            ocultarOjos = uiState.modoMultiseleccion
        )
        brainViewModel.navCoordinador.actualizarContratoActual(config)
    }

    LaunchedEffect(Unit) {
        brainViewModel.actionEvent.collect { actionId ->
            // 🔥 [LEY #12]: Soberanía por Contrato.
            val contratoActivoId = brainViewModel.navCoordinador.contratoActivo.value.id
            if (contratoActivoId != "root_inicio") return@collect

            when (actionId) {
                "fav" -> favoritosViewModel.establecerMostrarPanelFavoritos(true)
                "fast" -> navController.navigate(Screen.Urgencia.route)
                "close_all_sheets" -> favoritosViewModel.establecerMostrarPanelFavoritos(false)
                "select_all" -> {
                    val enSuper = uiState.categoriaState.superCategoriaSeleccionada == null && uiState.consultaBusqueda.isEmpty()
                    if (enSuper) {
                        val todas = uiState.categoriaState.superCategoriasFiltradas.flatMap { it.elementos }.map { it.id }
                        if (uiState.idsSeleccionados.size >= todas.size && todas.isNotEmpty()) {
                            categoryVm.deseleccionarTodo()
                        } else {
                            categoryVm.seleccionarTodoSuper(todas)
                        }
                    } else {
                        val todas = uiState.categoriaState.categoriasPlanas.map { it.id }
                        if (uiState.idsSeleccionados.size >= todas.size && todas.isNotEmpty()) {
                            homeViewModel.deseleccionarTodo()
                        } else {
                            homeViewModel.seleccionarTodo(todas)
                        }
                    }
                }
                "add_fav_multi" -> categoryVm.agregarSeleccionadasAFavoritos()
                "remove_fav_multi" -> categoryVm.quitarSeleccionadasDeFavoritos()
            }
        }
    }

    val estadoCuenta by userVm.ecosistemaMaestro.collectAsStateWithLifecycle()
    val perfilUsuario = estadoCuenta?.usuario?.perfil

    val mostrarPopupDireccion by ubicacionObrero.mostrarDialogoDireccion.collectAsStateWithLifecycle()
    val tieneDirecciones = (estadoCuenta?.usuario?.direcciones?.isNotEmpty() == true)
    val deberiaMostrarPopup = (!tieneDirecciones) && mostrarPopupDireccion && (perfilUsuario != null)

    HomeScreenClienteV4(
        navController = navController,
        state = uiState,
        alRefrescar = { 
            homeViewModel.alRefrescar()
            climaObrero.refrescarClima()
        },
        alSeleccionarSuperCategoria = { categoryVm.seleccionarSuperCategoria(it) },
        alAlternarFavoritoSuperCategoria = { id ->
            val esAgregado = !uiState.categoriaState.idsFavoritos.contains(id)
            val superCat = uiState.categoriaState.superCategoriasFiltradas.flatMap { it.elementos }.find { it.id == id }
            categoryVm.gestionarAccesoDirecto("home", id, "supercategory", esAgregado, superCat?.titulo, superCat?.icono)
        },
        alAlternarFavoritoCategoria = { categoryVm.alternarFavoritoCategoria(it) },
        alEstablecerVisibilidadDetallesClima = { climaObrero.establecerVisibilidadDetalles(it) },
        alAlternarGps = { ubicacionObrero.toggleGps(navController.context) },
        alHacerClickClima = { climaObrero.alternarDetalles() },
        alSeleccionarUbicacion = { ubicacionObrero.seleccionarDireccion(it.id) },
        alEstablecerVisibilidadMenuUbicacion = { homeViewModel.establecerMostrarMenuUbicacion(it) },
        alGestionarAccesoDirecto = { id, tipo, agregar, etiqueta, icono -> categoryVm.gestionarAccesoDirecto("home", id, tipo, agregar, etiqueta, icono) },
        alAlternarMenu = { brainViewModel.navCoordinador.establecerEstaMenuLateralAbierto(it) }, // 🔥 [FIX]
        alAlternarSeleccionItem = { 
            val enSuper = uiState.categoriaState.superCategoriaSeleccionada == null && uiState.consultaBusqueda.isEmpty()
            if (enSuper) {
                val total = uiState.categoriaState.superCategoriasFiltradas.flatMap { it.elementos }.size
                categoryVm.alternarSeleccionSuper(it, total)
            } else {
                homeViewModel.alternarSeleccionItem(it, uiState.categoriaState.categoriasPlanas.size)
            }
        },
        alAlternarFiltroOrden = { categoryVm.alternarFiltroOrden(it) }, 
        alHacerClickInfoCategoria = { categoryVm.establecerCategoriaParaDetalle(it) }, 
        alHacerClickCategoria = { id ->
            // 🔥 [v2026.ELITE]: Al seleccionar un resultado de búsqueda, apagamos el motor para cerrar el HUD y limpiar el texto.
            assistantViewModel?.beBusquedaMotor?.establecerEstaBusquedaActiva(false)
            navController.navigate(Screen.ResultBusqueda.createRoute(id))
        },
        alCerrarBusqueda = { assistantViewModel?.beBusquedaMotor?.establecerEstaBusquedaActiva(false) },
        alCerrarFavoritos = { favoritosViewModel.establecerMostrarPanelFavoritos(false) },
        panelDetalles = {
            SheetDetalleSuperCategoriaV3(
                beViewModel = brainViewModel,
                categoryViewModel = categoryVm,
                beArchitectViewModel = assistantViewModel,
                alHacerClickCategoria = { id: String ->
                    // 🔥 [v2026.ELITE]: También al seleccionar desde el detalle de la supercategoría.
                    assistantViewModel?.beBusquedaMotor?.establecerEstaBusquedaActiva(false)
                    navController.navigate(Screen.ResultBusqueda.createRoute(id))
                }
            )
        },
        panelFavoritos = {
            val mostrarFavoritos by favoritosViewModel.mostrarPanelFavoritos.collectAsStateWithLifecycle()
            AnimatedVisibility(
                visible = mostrarFavoritos,
                enter = slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(),
                exit = slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
            ) {
                FavoritosPanel(
                    navController = navController,
                    onClose = { favoritosViewModel.establecerMostrarPanelFavoritos(false) },
                    viewModel = favoritosViewModel
                )
            }
        },
        panelMenu = {
            MenuLateralHomeScreen(
                onNavigate = { route: String ->
                    brainViewModel.navCoordinador.establecerEstaMenuLateralAbierto(false) // 🔥 [FIX]
                    navController.navigate(route) { launchSingleTop = true }
                },
                onClose = { brainViewModel.navCoordinador.establecerEstaMenuLateralAbierto(false) } // 🔥 [FIX]
            )
        }
    )

    // --- POPUPS ---
    if (deberiaMostrarPopup) {
        ModernAddressPopup(
            onDismiss = { ubicacionObrero.establecerMostrarDialogoDireccion(false) },
            onGoToProfile = {
                ubicacionObrero.establecerMostrarDialogoDireccion(false)
                navController.navigate("perfil_cliente") { launchSingleTop = true }
            }
        )
    }
}

