package com.example.myapplication.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import com.example.myapplication.core.utilidades.ChatPreviewUtils
import com.example.myapplication.core.dominio.modelos.ConversacionHiloMDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.mapeadores.PrestadorMappers
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.ui.componentes.be.modelos.BeDictionary
import com.example.myapplication.ui.componentes.be.modelos.TipoBeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- VIEWMODEL LISTA DE CHATS (V2026.ELITE) ---
 * [LEY #1]: Pantalla Tonta - El ViewModel decide el filtrado multiperfil.
 */
@HiltViewModel
class UsuarioListaChatsViewModel @Inject constructor(
    private val chatRepository: ChatMotorSincRepositorio,
    private val categoryRepository: CategoriaRepositorio,
    private val coordinator: CoordinadorAcciones,
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor,
    private val authRepo: com.example.myapplication.datos.repositorios.UsuarioAutenticacionRepositorio,
    private val accesoDirectoRepositorio: com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
) : ViewModel() {

    val searchQuery: StateFlow<String> = beBusquedaMotor.consultaCruda

    /**
     * IDs de los prestadores marcados como favoritos en el contexto 'home'.
     */
    val favoriteIds: StateFlow<Set<String>> = accesoDirectoRepositorio.obtenerShortcutsPorContexto("home")
        .map { list -> 
            list.filter { it.tipo == "provider" }.map { it.idDestino }.toSet() 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    
    private val _uiState = MutableStateFlow(ListaChatsUiState(
        itemsFiltro = listOf(
            BeDictionary.Filters["filter_online"]!!,
            BeDictionary.Filters["filter_unread"]!!,
            BeDictionary.Filters["filter_verified"]!!
        ),
        itemsOrden = listOf(
            BeDictionary.Sorts["sort_date"]!!,
            BeDictionary.Sorts["sort_alpha"]!!
        )
    ))
    val uiState: StateFlow<ListaChatsUiState> = _uiState.asStateFlow()

    init {
        observarFlujosEstado()
        
        // 🔥 [ELITE REACTIVITY]: Limpiar selección local si el modo se desactiva globalmente (ej: Botón Cancelar en Be).
        viewModelScope.launch {
            coordinator.estaMultiseleccionActiva.collect { activa ->
                if (!activa) {
                    val actual = _uiState.value
                    if (actual.idsChatsSeleccionados.isNotEmpty() || actual.modoMultiseleccion) {
                        android.util.Log.d("ListaChatsVM", "🧹 [CLEAN] Limpiando selección por orden global de Be.")
                        _uiState.update { it.copy(idsChatsSeleccionados = emptySet(), modoMultiseleccion = false) }
                    }
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observarFlujosEstado() {
        val currentLocalIdFlow = coordinator.idPerfilSeleccionado.map { it ?: "personal" }.flatMapLatest { profileId ->
            if (profileId == "personal") {
                authRepo.observarUsuarioActual().map { it?.uid ?: "" }
            } else {
                flowOf(profileId)
            }
        }.distinctUntilChanged()

        // --- 1. BASE DE DATOS CRUDA (Para metadatos como Categorías) ---
        val baseConversacionesFlow = currentLocalIdFlow.flatMapLatest { idLocal ->
            if (idLocal.isEmpty()) flowOf(emptyList())
            else chatRepository.obtenerConversacionesSoberanas(idLocal, "", false, false, false, null, "sort_date")
        }

        // --- 2. HILOS FILTRADOS POR SQL (Performance Elite) ---
        val hilosFiltradosFlow = combine(
            currentLocalIdFlow,
            beBusquedaMotor.consultaNormalizadaDebounced,
            _uiState.map { it.filtrosActivos }.distinctUntilChanged()
        ) { idLocal, query, activeFilters ->
            val soloNoLeidos = activeFilters.contains("filter_unread")
            val soloOnline = activeFilters.contains("filter_online")
            val soloVerificados = activeFilters.contains("filter_verified")
            val idCategoria = activeFilters.find { it.startsWith("cat_") }?.removePrefix("cat_")
            val orden = activeFilters.find { it.startsWith("sort_") } ?: "sort_date"

            chatRepository.obtenerConversacionesSoberanas(
                idLocal, query, soloNoLeidos, soloOnline, soloVerificados, idCategoria, orden
            )
        }.flatMapLatest { it }

        // --- 3. MAPEADO A DOMINIO ---
        val hilosFlow = hilosFiltradosFlow.map { list ->
            list.map { view ->
                val conv = view.conversacion
                ConversacionHiloMDominio(
                    idChat = conv.idChat,
                    idUsuarioRemoto = conv.idIdentidadRemota,
                    nombreVisible = view.nombreSoberano,
                    urlFoto = view.fotoSoberana,
                    urlMiniatura = view.miniaturaSoberana,
                    ultimoMensaje = ChatPreviewUtils.obtenerTextoVistaPrevia(conv.tipoUltimoMensaje, conv.ultimoMensaje),
                    marcaTiempoUltimo = conv.fechaUltimoMensaje,
                    estaOnline = view.estaOnlineSoberano,
                    estaVerificado = view.estaVerificadoSoberano,
                    idIdentidadLocal = conv.idIdentidadLocal,
                    idSucursalRemota = conv.idIdentidadRemota,
                    idCategoriaPrincipal = view.idsCategoriasSoberanas.firstOrNull(),
                    conteoNoLeidos = conv.contadorNoLeidos,
                    identidadCompleta = PrestadorDominio(
                        id = conv.idIdentidadRemota,
                        idCategorias = view.idsCategoriasSoberanas,
                        titulo = view.nombreSoberano,
                        urlFoto = view.fotoSoberana,
                        urlMiniatura = view.miniaturaSoberana,
                        estaOnline = view.estaOnlineSoberano,
                        estaVerificado = view.estaVerificadoSoberano
                    )
                )
            }
        }

        val categoryFlow = combine(
            baseConversacionesFlow,
            categoryRepository.todasLasCategorias
        ) { base, allCats ->
            // 🔥 [ELITE]: Extraemos los rubros directamente de las conversaciones soberanas
            val uniqueCategoryIds = base.flatMap { it.idsCategoriasSoberanas }.toSet()
            val categoryMap = allCats.associateBy { it.id }
            
            uniqueCategoryIds.mapNotNull { id ->
                val meta = categoryMap[id]
                if (meta != null) {
                    DropdownItemData(
                        id = "cat_$id", 
                        label = meta.nombre, 
                        section = "Rubros en uso", 
                        emoji = meta.icono
                    )
                } else null
            }.sortedBy { it.label }
        }

        viewModelScope.launch {
            combine(
                hilosFlow,
                currentLocalIdFlow.flatMapLatest { idLocal ->
                    if (idLocal.isEmpty()) flowOf(emptyList())
                    else chatRepository.obtenerConteosNoLeidos(idLocal)
                }.map { list -> list.associate { it.idChat to it.count.toInt() } },
                coordinator.idPerfilSeleccionado.map { it ?: "personal" },
                coordinator.estaMultiseleccionActiva,
                categoryFlow
            ) { hilos, unread, profile, multi, categories ->
                _uiState.update { s ->
                    s.copy(
                        hilos = mapOf("" to hilos),
                        conteoNoLeidos = unread,
                        idPerfilSeleccionado = profile,
                        modoMultiseleccion = multi,
                        totalItems = hilos.size,
                        itemsCategoria = categories
                    )
                }
            }.collect()
        }
    }

    val chattingThreads = uiState.map { it.hilos }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val unreadCountsMap = uiState.map { it.conteoNoLeidos }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val selectedPerfilId = uiState.map { it.idPerfilSeleccionado }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "personal")
    val filteredChatsCount = uiState.map { it.totalItems }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val isMultiSelectionActive = uiState.map { it.modoMultiseleccion }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val categoryDropdownItems = uiState.map { it.itemsCategoria }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filterDropdownItems = uiState.map { it.itemsFiltro }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sortDropdownItems = uiState.map { it.itemsOrden }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filtrosActivos = uiState.map { it.filtrosActivos }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val selectedChatIds = uiState.map { it.idsChatsSeleccionados }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val isRefreshing = uiState.map { it.estaRefrescando }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun selectBranch(id: String?) {
        // [SOBERANÍA]: Selección de sucursal delegada al coordinador
        id?.let { coordinator.seleccionarPerfil(it) }
    }

    fun selectPerfil(id: String) {
        coordinator.seleccionarPerfil(if (id == "personal") null else id)
    }

    fun toggleFilter(id: String) {
        _uiState.update { s ->
            val current = s.filtrosActivos
            val next = when {
                id == "CLEAR_ALL" -> emptySet()
                id.startsWith("sort_") -> {
                    // Reemplazar orden anterior por el nuevo
                    current.filter { !it.startsWith("sort_") }.toSet() + id
                }
                current.contains(id) -> current - id
                else -> current + id
            }
            s.copy(filtrosActivos = next)
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(estaRefrescando = true) }
            coordinator.mostrarToast("Actualizando chats...", TipoBeToast.PROCESANDO, 0L)
            delay(1000.milliseconds)
            _uiState.update { it.copy(estaRefrescando = false) }
            coordinator.mostrarToast("Actualizado", TipoBeToast.EXITO, 3000L)
        }
    }

    fun deleteSelectedChats() {
        viewModelScope.launch {
            _uiState.value.idsChatsSeleccionados.forEach { chatRepository.eliminarConversacion(it) }
            updateMultiSelection(false)
        }
    }

    fun deleteChatById(id: String) {
        viewModelScope.launch {
            chatRepository.eliminarConversacion(id)
        }
    }

    fun updateMultiSelection(active: Boolean) {
        coordinator.actualizarMultiseleccion(active)
        if (!active) _uiState.update { it.copy(idsChatsSeleccionados = emptySet()) }
    }

    fun toggleSelection(id: String) {
        _uiState.update { s ->
            val current = s.idsChatsSeleccionados.toMutableSet()
            if (!current.remove(id)) current.add(id)
            
            if (current.isEmpty()) {
                coordinator.actualizarMultiseleccion(false)
                coordinator.actualizarTodoSeleccionado(false)
            } else {
                if (!coordinator.estaMultiseleccionActiva.value) {
                    coordinator.actualizarMultiseleccion(true)
                }
                val total = s.hilos.values.flatten().size
                coordinator.actualizarTodoSeleccionado(current.size >= total)
            }
            
            s.copy(idsChatsSeleccionados = current)
        }
    }

    fun seleccionarTodo() {
        val todosIds = chattingThreads.value.values.flatten().map { it.idChat }.toSet()
        if (_uiState.value.idsChatsSeleccionados.size >= todosIds.size && todosIds.isNotEmpty()) {
            deseleccionarTodo()
        } else {
            _uiState.update { it.copy(idsChatsSeleccionados = todosIds, modoMultiseleccion = true) }
            coordinator.actualizarMultiseleccion(true)
            coordinator.actualizarTodoSeleccionado(true)
        }
    }

    fun deseleccionarTodo() {
        _uiState.update { it.copy(idsChatsSeleccionados = emptySet()) }
        // 🔥 [v2026.ELITE]: Mantenemos multiselección activa pero informamos que no hay nada seleccionado
        coordinator.actualizarTodoSeleccionado(false)
    }

    fun agregarSeleccionadasAFavoritos() {
        viewModelScope.launch {
            val idsChats = _uiState.value.idsChatsSeleccionados
            val hilos = chattingThreads.value.values.flatten()
            
            idsChats.forEach { idChat ->
                val hilo = hilos.find { it.idChat == idChat }
                hilo?.let {
                    accesoDirectoRepositorio.agregarShortcut("home", it.idUsuarioRemoto, "provider", it.nombreVisible, it.urlMiniatura?.toString())
                }
            }
            updateMultiSelection(false)
            coordinator.emitirFeedbackVisual("Chats añadidos a favoritos ❤️", com.example.myapplication.ui.componentes.be.modelos.EmocionBe.FELIZ)
        }
    }

    fun quitarSeleccionadasDeFavoritos() {
        viewModelScope.launch {
            val idsChats = _uiState.value.idsChatsSeleccionados
            val hilos = chattingThreads.value.values.flatten()
            
            idsChats.forEach { idChat ->
                val hilo = hilos.find { it.idChat == idChat }
                hilo?.let {
                    accesoDirectoRepositorio.eliminarShortcut("home", it.idUsuarioRemoto)
                }
            }
            updateMultiSelection(false)
            coordinator.emitirFeedbackVisual("Chats eliminados de favoritos 💔", com.example.myapplication.ui.componentes.be.modelos.EmocionBe.TRISTE)
        }
    }
}
