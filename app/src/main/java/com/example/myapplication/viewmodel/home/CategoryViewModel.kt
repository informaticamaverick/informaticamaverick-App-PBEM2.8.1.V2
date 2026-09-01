package com.example.myapplication.viewmodel.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.utilidades.filtroDeTexto
import com.example.myapplication.core.utilidades.normalizeFull
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio
import com.example.myapplication.core.dominio.modelos.CategoryUiState
import com.example.myapplication.core.dominio.modelos.FilaCategoriaDominio
import com.example.myapplication.core.dominio.modelos.FilaSuperCategoriaDominio
import com.example.myapplication.core.dominio.mapeadores.CategoriaMappers
import com.example.myapplication.core.dominio.mapeadores.SuperCategoriaMappers
import com.example.myapplication.ui.componentes.be.modelos.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

/**
 * --- CATEGORY VIEWMODEL (MOTOR ELITE v2026.FINAL) ---
 * [LEY #10]: Desacoplamiento total usando UiModels.
 * [LEY #3.2]: Carga Bajo Demanda (On-Demand) para optimizar RAM.
 * [HERENCIA]: Inyecta el color de Supercategoría en los rubros hijos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repositorioCategoria: CategoriaRepositorio,
    private val repositorioAccesosDirectos: AccesoDirectoRepositorio,
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor,
    private val coordinator: CoordinadorAcciones,
    private val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion // 🔥 [NEW]
) : ViewModel() {

    private val _filtrosOrdenActivos = MutableStateFlow(setOf("view_bento", "sort_favorites"))

    // 🔥 [v2026.ELITE]: Estados de multiselección táctica para Supercategorías
    private val _estaEnModoSeleccionSuper = MutableStateFlow(false)
    private val _idsSuperSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    private val _categoriaParaDetalle = MutableStateFlow<CategoriaDominio?>(null)

    /**
     * 🔥 [ELITE]: Flujo reactivo de rubros base (SQLView).
     * Decide si la fuente de datos es Global o Segmentada por Supercategoría.
     */
    private val rubrosBaseFlow: Flow<List<CategoriaDominio>> = combine(
        coordinator.superCategoriaSeleccionada,
        beBusquedaMotor.estaBusquedaActiva,
        beBusquedaMotor.consultaNormalizada
    ) { seleccionadoSuper, estaBuscando, queryNormal ->
        Log.d("SEARCH_AUDIT", "🔎 [BASE_TRIGGER] Super: ${seleccionadoSuper?.titulo} | Buscando: $estaBuscando | Query: '$queryNormal'")
        Triple(seleccionadoSuper, estaBuscando, queryNormal)
    }.flatMapLatest { (seleccionadoSuper, estaBuscando, queryNormal) ->
        when {
            seleccionadoSuper != null -> {
                Log.i("SEARCH_AUDIT", "🎯 [SOURCE] Segmentada por: ${seleccionadoSuper.titulo}")
                repositorioCategoria.obtenerResumenPorSuperCategoria(seleccionadoSuper.id)
                    .map { list -> list.map { CategoriaMappers.deVistaADominio(it) } }
            }
            estaBuscando && queryNormal.isNotEmpty() -> {
                Log.i("SEARCH_AUDIT", "🌍 [SOURCE] Global en todo el catálogo para: '$queryNormal'")
                // 🔥 [ELITE FIX]: Usamos obtenerResumenTodas para garantizar reactividad y 
                // permitir que el filtrado fino manual (tokens/acentos) funcione sobre el universo total.
                repositorioCategoria.obtenerResumenTodas()
                    .map { list -> list.map { CategoriaMappers.deVistaADominio(it) } }
            }
            else -> {
                Log.v("SEARCH_AUDIT", "⏸️ [SOURCE] Ninguna fuente activa.")
                flowOf(emptyList())
            }
        }
    }

    /**
     * [ELITE SSOT]: Estado único de la UI.
     * Centraliza el filtrado final, ordenamiento y chunking.
     */
    val uiState: StateFlow<CategoryUiState> = combine(
        navCoordinador.estaHojaVisible, 
        coordinator.superCategoriaSeleccionada,
        repositorioCategoria.obtenerMetadatosSuperCategorias().map { list -> list.map { SuperCategoriaMappers.aUiModel(it) } },
        _filtrosOrdenActivos,
        repositorioAccesosDirectos.obtenerShortcutsPorContexto("home").map { list -> list.map { it.idDestino }.toSet() },
        beBusquedaMotor.estaBusquedaActiva,
        beBusquedaMotor.consultaNormalizada,
        beBusquedaMotor.consultaNormalizadaDebounced,
        rubrosBaseFlow,
        _estaEnModoSeleccionSuper,
        _idsSuperSeleccionados,
        _categoriaParaDetalle
    ) { flows ->
        val hojaVisible = flows[0] as Boolean
        val seleccionadoSuper = flows[1] as SuperCategoriaDominio?
        val metaSuper = flows[2] as List<SuperCategoriaDominio>
        val filtros = flows[3] as Set<String>
        val favoritos = flows[4] as Set<String>
        val estaBuscando = flows[5] as Boolean
        val queryNormal = flows[6] as String
        val queryDebounced = flows[7] as String
        val rubrosBase = flows[8] as List<CategoriaDominio>
        val multiSuper = flows[9] as Boolean
        val seleccionadosSuper = flows[10] as Set<String>
        val catDetalle = flows[11] as CategoriaDominio?

        Log.d("SEARCH_AUDIT", "🧪 [FILTER_START] Rubros Base: ${rubrosBase.size} | Query Debounced: '$queryDebounced'")

        // 1. Filtrado Fino de Rubros (Segundo paso para tokens/acentos)
        val rubrosFiltrados = if (queryNormal.isEmpty()) {
            val favs = rubrosBase.filter { favoritos.contains(it.id) }
            val resto = rubrosBase.filter { !favoritos.contains(it.id) }
            (favs + resto).take(80)
        } else {
            val q = queryDebounced.ifEmpty { queryNormal }
            val qLower = q.lowercase()
            val filtrados = rubrosBase.filter { it.nombre.filtroDeTexto(q) }
            
            Log.d("SEARCH_AUDIT", "📝 [FILTER_RESULT] Query: '$q' | Match: ${filtrados.size} de ${rubrosBase.size}")
            
            filtrados.sortedWith(
                compareByDescending<CategoriaDominio> { 
                    it.nombre.normalizeFull().startsWith(qLower) 
                }
                .thenByDescending { favoritos.contains(it.id) }
            )
        }

        // 2. Filtrado y Ordenamiento de Supercategorías
        val superFiltradas = if (estaBuscando && queryNormal.isNotEmpty() && seleccionadoSuper == null) {
            metaSuper.filter { it.titulo.filtroDeTexto(queryNormal) }
        } else {
            metaSuper
        }

        val sortId = filtros.find { it.startsWith("sort_") } ?: "sort_favorites"
        val superOrdenadas = when (sortId) {
            "sort_favorites" -> superFiltradas.sortedWith(
                compareByDescending<SuperCategoriaDominio> { favoritos.contains(it.id) }
                .thenBy { it.titulo.normalizeFull() }
            )
            "sort_alpha_asc" -> superFiltradas.sortedBy { it.titulo.normalizeFull() }
            "sort_alpha_desc" -> superFiltradas.sortedByDescending { it.titulo.normalizeFull() }
            else -> superFiltradas // Mantiene el orden de base (SQL)
        }

        // 3. Chunking Estratégico
        val columnas = if (filtros.contains("view_bento") && queryNormal.isEmpty()) 2 else 3
        
        val filasCats = rubrosFiltrados.chunked(columnas).map { FilaCategoriaDominio(it) }
        val filasSupers = superOrdenadas.chunked(columnas).map { FilaSuperCategoriaDominio(it) }

        CategoryUiState(
            estaHojaVisible = hojaVisible,
            superCategoriaSeleccionada = seleccionadoSuper,
            superCategoriasFiltradas = filasSupers,
            categoriasFiltradas = filasCats,
            categoriasPlanas = rubrosFiltrados,
            cantidadResultados = rubrosFiltrados.size,
            estaCargando = metaSuper.isEmpty(),
            filtrosOrden = filtros,
            idsFavoritos = favoritos,
            estaEnModoSeleccionSuper = multiSuper,
            idsSuperSeleccionados = seleccionadosSuper,
            categoriaParaDetalle = catDetalle
        )
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState())

    // --- MANTENEMOS COMPATIBILIDAD PARA EVITAR CAMBIOS MASIVOS EN UI ---
    val estaHojaVisible = uiState.map { it.estaHojaVisible }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val superCategoriaSeleccionada = uiState.map { it.superCategoriaSeleccionada }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val todasLasCategorias = uiState.map { it.categoriasPlanas }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriasOrdenadas = uiState.map { it.categoriasPlanas }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val idsAccesosDirectosHome = uiState.map { it.idsFavoritos }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun establecerCategoriaParaDetalle(cat: CategoriaDominio?) {
        _categoriaParaDetalle.value = cat
    }

    fun seleccionarSuperCategoria(superCat: SuperCategoriaDominio?) = coordinator.seleccionarSuperCategoria(superCat)

    fun gestionarAccesoDirecto(contexto: String, idDestino: String, tipo: String, esAgregar: Boolean, etiqueta: String? = null, icono: String? = null) {
        viewModelScope.launch {
            if (esAgregar) repositorioAccesosDirectos.agregarShortcut(contexto, idDestino, tipo, etiqueta, icono)
            else repositorioAccesosDirectos.eliminarShortcut(contexto, idDestino)
        }
    }

    fun alternarFavoritoCategoria(categoria: CategoriaDominio) {
        viewModelScope.launch {
            val favs = idsAccesosDirectosHome.value
            val esFavorito = favs.contains(categoria.id)
            gestionarAccesoDirecto("home", categoria.id, "category", !esFavorito, categoria.nombre, categoria.icono)
        }
    }

    /**
     * 🔥 [ELITE]: Alterna la selección de una supercategoría en modo multiselección.
     */
    fun alternarSeleccionSuper(id: String, totalItems: Int) {
        val actual = _idsSuperSeleccionados.value.toMutableSet()
        if (actual.contains(id)) {
            actual.remove(id)
            if (actual.isEmpty()) {
                _estaEnModoSeleccionSuper.value = false
                coordinator.actualizarTodoSeleccionado(false)
            }
        } else {
            actual.add(id)
            _estaEnModoSeleccionSuper.value = true
        }
        _idsSuperSeleccionados.value = actual
        
        // Sincronizar con el coordinador global para que Be muestre las herramientas de edición
        coordinator.actualizarMultiseleccion(_estaEnModoSeleccionSuper.value)
        coordinator.actualizarTodoSeleccionado(actual.isNotEmpty() && actual.size >= totalItems)
    }

    fun establecerModoSeleccionSuper(activo: Boolean) {
        _estaEnModoSeleccionSuper.value = activo
        if (!activo) {
            _idsSuperSeleccionados.value = emptySet()
            coordinator.actualizarTodoSeleccionado(false)
        }
        coordinator.actualizarMultiseleccion(activo)
    }

    fun seleccionarTodoSuper(ids: List<String>) {
        _idsSuperSeleccionados.value = ids.toSet()
        _estaEnModoSeleccionSuper.value = true
        coordinator.actualizarMultiseleccion(true)
        coordinator.actualizarTodoSeleccionado(true)
    }

    fun deseleccionarTodo() {
        _idsSuperSeleccionados.value = emptySet()
        coordinator.actualizarTodoSeleccionado(false)
        // 🔥 [v2026.ELITE]: No cerramos el modo para permitir alternancia 'Ninguno' -> 'Todo'
    }

    fun agregarSeleccionadasAFavoritos() {
        viewModelScope.launch {
            val ids = _idsSuperSeleccionados.value
            val todasLasSupers = uiState.value.superCategoriasFiltradas.flatMap { it.elementos }
            ids.forEach { id ->
                val s = todasLasSupers.find { it.id == id }
                gestionarAccesoDirecto("home", id, "supercategory", true, s?.titulo, s?.icono)
            }
            establecerModoSeleccionSuper(false)
            coordinator.emitirFeedbackVisual("Supercategorías añadidas a favoritos ❤️", EmocionBe.FELIZ)
        }
    }

    fun quitarSeleccionadasDeFavoritos() {
        viewModelScope.launch {
            val ids = _idsSuperSeleccionados.value
            ids.forEach { id ->
                gestionarAccesoDirecto("home", id, "supercategory", false)
            }
            establecerModoSeleccionSuper(false)
            coordinator.emitirFeedbackVisual("Supercategorías eliminadas de favoritos 💔", EmocionBe.TRISTE)
        }
    }

    fun alternarFiltroOrden(idFiltro: String) {
        val actual = _filtrosOrdenActivos.value.toMutableSet()
        if (idFiltro.startsWith("view_")) {
            actual.removeAll { it.startsWith("view_") }
            actual.add(idFiltro)
        } else if (idFiltro.startsWith("sort_")) {
            if (actual.contains(idFiltro)) actual.remove(idFiltro)
            else {
                actual.removeAll { it.startsWith("sort_") }
                if (idFiltro.isNotEmpty()) actual.add(idFiltro)
            }
        }
        _filtrosOrdenActivos.value = actual
    }

    init {
        // 🔥 [ELITE REACTIVITY]: Escuchar cancelación global de Be para limpiar selección local
        viewModelScope.launch {
            coordinator.estaMultiseleccionActiva.collect { activa ->
                if (!activa && _estaEnModoSeleccionSuper.value) {
                    Log.d("CategoryVM", "🧹 [CLEAN] Limpiando selección de supercategorías.")
                    _estaEnModoSeleccionSuper.value = false
                    _idsSuperSeleccionados.value = emptySet()
                }
            }
        }

        combine(
            uiState.map { it.superCategoriasFiltradas.flatMap { f -> f.elementos } }, 
            beBusquedaMotor.estaBusquedaActiva, 
            beBusquedaMotor.consultaNormalizada,
            navCoordinador.contratoActivo // 🔥 [ELITE]: Soberanía de contexto
        ) { lista, activa, consulta, contrato ->
            // Solo publicamos si estamos en la Home o si no hay un contrato específico que lo prohíba
            if (activa && consulta.isNotEmpty() && contrato.pistaBusqueda.contains("BE")) {
                lista.filter { it.titulo.filtroDeTexto(consulta) }
            } else {
                emptyList()
            }
        }.onEach { filtradas ->
            coordinator.publicarSuperCategoriasEncontradas(filtradas)
        }.launchIn(viewModelScope)
    }

    fun limpiarFiltros() {
        _filtrosOrdenActivos.value = setOf("view_bento", "sort_favorites")
        coordinator.seleccionarSuperCategoria(null)
        beBusquedaMotor.limpiarConsulta()
    }
}


