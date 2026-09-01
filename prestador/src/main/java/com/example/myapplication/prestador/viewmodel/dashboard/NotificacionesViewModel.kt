package com.example.myapplication.prestador.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.myapplication.core.datos.repositorios.*
import com.example.myapplication.core.datos.local.dao.CategoriaDao
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.mapeadores.ConcursoMappers
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.datos.local.entidades.ProductoFinalEntity
import com.example.myapplication.core.datos.local.entidades.TipoProductoFinal
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import com.example.myapplication.prestador.datos.repositorios.PrestadorPresupuestoRepositorio
import com.example.myapplication.prestador.datos.repositorios.ConsultasPrestadorRepositorio
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE NOTIFICACIONES Y MERCADO (ELITE v2026) ---
 */
@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val repositorio: NotificacionRepositorio,
    private val presupuestoRepositorio: PrestadorPresupuestoRepositorio,
    private val concursoRepositorio: ConcursoPublicoRepositorio,
    private val consultasRepo: ConsultasPrestadorRepositorio,
    private val categoriaDao: CategoriaDao,
    private val auth: FirebaseAuth
) : ViewModel() {

    // --- SECCIÓN: NOTIFICACIONES ---
    private val _filtroTipo = MutableStateFlow<TipoNotificacion?>(null)
    val filtroTipo: StateFlow<TipoNotificacion?> = _filtroTipo.asStateFlow()

    private val _soloNoLeidas = MutableStateFlow(false)
    val soloNoLeidas: StateFlow<Boolean> = _soloNoLeidas.asStateFlow()

    val notificaciones: StateFlow<List<ElementoNotificacion>> = combine(_filtroTipo, _soloNoLeidas) { tipo, soloNoLeidas ->
        tipo to soloNoLeidas
    }.flatMapLatest { (tipo, soloNoLeidas) ->
        if (tipo != null) {
            repositorio.obtenerPorTipo(tipo)
        } else if (soloNoLeidas) {
            repositorio.obtenerNoLeidas()
        } else {
            repositorio.obtenerTodas()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repositorio.obtenerConteoNoLeidas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val conteoNoLeidas = unreadCount // Alias compatibilidad

    // --- SECCIÓN: MERCADO DE CONCURSOS ---
    private val _concursoSeleccionado = MutableStateFlow<ConcursoDominio?>(null)
    val concursoSeleccionado = _concursoSeleccionado.asStateFlow()

    private val _presupuestoEnVista = MutableStateFlow<PresupuestoConItems?>(null)
    val presupuestoEnVista = _presupuestoEnVista.asStateFlow()

    private val _estaCargandoConcurso = MutableStateFlow(false)
    val estaCargandoConcurso = _estaCargandoConcurso.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val mercadoConcursos: Flow<PagingData<ConcursoDominio>> = consultasRepo.obtenerPerfilPrestadorDeepFlujo(auth.currentUser?.uid ?: "")
        .flatMapLatest { maestro ->
            val cp = maestro?.prestador?.direcciones?.firstOrNull()?.codigoPostal ?: ""
            val cats = maestro?.prestador?.perfil?.idCategorias ?: emptyList()
            
            presupuestoRepositorio.obtenerMercadoPaginado(cp, cats).map { pagingData ->
                pagingData.map { entity ->
                    val categoria = categoriaDao.obtenerPorId(entity.idCategoria)
                    ConcursoMappers.aUiModel(entity, categoria?.nombre, categoria?.icono)
                }
            }
        }.cachedIn(viewModelScope)

    fun refrescarMercado() {
        // La actualización ocurre por el flujo reactivo o recarga de Paging
    }

    fun alHacerClickConcurso(id: String) {
        viewModelScope.launch {
            _estaCargandoConcurso.value = true
            val concurso = concursoRepositorio.obtenerConcursoPorId(id)
            if (concurso != null) {
                val categoria = categoriaDao.obtenerPorId(concurso.idCategoria)
                _concursoSeleccionado.value = ConcursoMappers.aUiModel(concurso, categoria?.nombre, categoria?.icono)
            }
            _estaCargandoConcurso.value = false
        }
    }

    fun alVerPresupuestoEnviado(idConcurso: String) {
        viewModelScope.launch {
            presupuestoRepositorio.todosLosPresupuestosFinales.first().find { it.idConcurso == idConcurso }?.let { p ->
                presupuestoRepositorio.obtenerPresupuestoCocinaConItems(p.idPresupuesto).first()?.let { cocina ->
                     _presupuestoEnVista.value = com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems(
                         cabecera = p,
                         lineas = cocina.items.map { it.aProductoFinal(p.idPresupuesto) },
                         finanzas = emptyList()
                     )
                }
            }
        }
    }

    private fun com.example.myapplication.prestador.datos.local.entidades.ProductoEntity.aProductoFinal(idPresupuesto: String) = 
        com.example.myapplication.core.datos.local.entidades.ProductoFinalEntity(
            idPresupuesto = idPresupuesto,
            idOriginal = id,
            nombreCopiado = nombre,
            cantidad = cantidad,
            precioSnapshot = precioVenta,
            tipoItem = when(tipo) {
                com.example.myapplication.core.datos.local.entidades.TipoProducto.PRODUCTO -> com.example.myapplication.core.datos.local.entidades.TipoProductoFinal.PRODUCTO
                com.example.myapplication.core.datos.local.entidades.TipoProducto.SERVICIO -> com.example.myapplication.core.datos.local.entidades.TipoProductoFinal.SERVICIO
                else -> com.example.myapplication.core.datos.local.entidades.TipoProductoFinal.GASTO
            }
        )

    fun cerrarHojaConcurso() { _concursoSeleccionado.value = null }
    fun cerrarVistaPreviaPresupuesto() { _presupuestoEnVista.value = null }

    // --- ACCIONES NOTIFICACIONES ---
    fun setFiltroTipo(tipo: TipoNotificacion?) { _filtroTipo.value = tipo }
    fun setSoloNoLeidas(solo: Boolean) { _soloNoLeidas.value = solo }

    fun marcarComoLeida(id: Long) {
        viewModelScope.launch { repositorio.marcarComoLeida(id) }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch { repositorio.marcarTodasComoLeidas() }
    }

    fun eliminarNotificacion(id: Long) {
        viewModelScope.launch { repositorio.eliminarPorId(id) }
    }
}
